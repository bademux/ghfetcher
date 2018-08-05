package com.github.bademux.ghfecher.client;

import com.github.bademux.ghfecher.model.GitHubRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.client.PageLinks;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static java.lang.String.format;

@Slf4j
public class GitHubClientImpl implements GitHubClient {

    private static final int MAXITEMS_PER_REPO_PAGE = 100;
    private static final String PARAM_NAME_PAGE = "page";
    private static final int FIRST_PAGE_INDEX = 1;

    private final WebClient webClient;

    public GitHubClientImpl(URI baseUri) {
        webClient = createWebClient(baseUri);
    }

    @Override
    public Flux<GitHubRepo> getAllRepositories(String userName) {
        log.debug("getting all repo for user '{}' ", userName);
        return new GitHubClientForUserHelper(userName).getAllRepositories();
    }

    protected Mono<ClientResponse> getRepositories(String userName, int pageNumber) {
        log.debug("getting all repo for user '{}' page #{}", userName, pageNumber);
        return webClient.get()
                .uri(uriBuilder -> createUriForPageNumber(uriBuilder, userName, pageNumber))
                .exchange()
                .flatMap(this::handleResponse);
    }

    private Mono<ClientResponse> handleResponse(ClientResponse response) {
        HttpStatus httpStatus = response.statusCode();
        if (httpStatus.is4xxClientError()) {
            return Mono.error(createClientException(response, httpStatus));
        } else if (httpStatus.is5xxServerError()) {
            return Mono.error(createServerException(response, httpStatus));
        } else if (httpStatus.is3xxRedirection()) {
            return handleRedirect(response);
        }
        return Mono.just(response);
    }

    private HttpServerErrorException createServerException(ClientResponse response, HttpStatus httpStatus) {
        return new HttpServerErrorException(httpStatus, httpStatus.getReasonPhrase(), response.headers().asHttpHeaders(), null, null);
    }

    private HttpClientErrorException createClientException(ClientResponse response, HttpStatus httpStatus) {
        return new HttpClientErrorException(httpStatus, httpStatus.getReasonPhrase(), response.headers().asHttpHeaders(), null, null);
    }

    /**
     * TODO: remove once Spring will support it natively
     *
     * @param clientResponse
     * @return
     */
    @Deprecated
    private Mono<ClientResponse> handleRedirect(ClientResponse clientResponse) {
        List<String> locations = clientResponse.headers().header(HttpHeaders.LOCATION);
        if (locations.isEmpty()) {
            throw new IllegalStateException("unhandled redirect, no locations in header");
        }
        return webClient
                .get()
                .uri(locations.get(0))
                .exchange()
                .map(this::checkIsRedirectAndReturn);
    }

    private ClientResponse checkIsRedirectAndReturn(ClientResponse response) {
        if (response.statusCode().is3xxRedirection()) {
            throw new IllegalStateException("nested redirects aren't are disabled");
        }
        return response;
    }

    static int parsePageNumberFromUri(String uri) {
        String pageNumber = UriComponentsBuilder.fromUriString(uri).build()
                .getQueryParams()
                .getFirst(PARAM_NAME_PAGE);
        if (pageNumber == null) {
            throw new IllegalArgumentException(format("Given uri '%s' contain bad '%s' query param", uri, PARAM_NAME_PAGE));
        }
        return Integer.valueOf(pageNumber);
    }

    private static WebClient createWebClient(URI baseUri) {
        return WebClient.builder()
                .uriBuilderFactory(createUriBuilderFactory(baseUri))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "ghfecher")
                .build();
    }

    private static UriBuilderFactory createUriBuilderFactory(URI baseUri) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(baseUri)
                .path("/users/{user}/repos")
                .queryParam("per_page", MAXITEMS_PER_REPO_PAGE);
        return new DefaultUriBuilderFactory(uriComponentsBuilder);
    }

    private URI createUriForPageNumber(UriBuilder uriBuilder, String userName, int pageNumber) {
        return uriBuilder.queryParam(PARAM_NAME_PAGE, pageNumber).build(userName);
    }

    private Flux<GitHubRepo> getGitHubRepoFlux(ClientResponse response) {
        return response.bodyToFlux(GitHubRepo.class);
    }

    private int parseLastPageNumberFromResponse(ClientResponse clientResponse) {
        return clientResponse.headers()
                .header(HttpHeaders.LINK)
                .stream()
                .findFirst()
                .map(PageLinks::new)
                .map(PageLinks::getLast)
                .map(GitHubClientImpl::parsePageNumberFromUri)
                .orElse(1);
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    private class GitHubClientForUserHelper {

        private final String userName;

        Flux<GitHubRepo> getAllRepositories() {
            return getRepositories(userName, FIRST_PAGE_INDEX)
                    .flatMapMany(this::processResponse);
        }

        Publisher<GitHubRepo> processResponse(ClientResponse response) {
            Flux<GitHubRepo> firstPage = getGitHubRepoFlux(response);
            int totalPages = parseLastPageNumberFromResponse(response);
            if (totalPages == 1) {
                return firstPage;
            }
            return firstPage.mergeWith(getTheRestPagesForUser(totalPages));
        }

        Flux<GitHubRepo> getRepositoriesForPageNumber(int pageNumber) {
            return getRepositories(userName, pageNumber)
                    .flatMapMany(GitHubClientImpl.this::getGitHubRepoFlux);
        }

        Flux<GitHubRepo> getTheRestPagesForUser(int to) {
            int start = FIRST_PAGE_INDEX + 1;
            int count = to - 1;
            return Flux.range(start, count)
                    .flatMap(this::getRepositoriesForPageNumber);
        }

    }

}
