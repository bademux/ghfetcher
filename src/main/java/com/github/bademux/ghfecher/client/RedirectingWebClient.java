package com.github.bademux.ghfecher.client;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public final class RedirectingWebClient {

    private static final int MAX_REDIRECTS = 5;
    private final WebClient webClient;

    public RedirectingWebClient(final String url) {
        webClient = WebClient.create(url);
    }

    public Mono<ClientResponse> get(final String uri) {
        return redirectingRequest(uri, 0);
    }

    private Mono<ClientResponse> redirectingRequest(final String uri, final int redirects) {
        Optional.of(redirects)
                .filter(red -> red <= MAX_REDIRECTS)
                .orElseThrow(() -> new IllegalStateException("too many redirects"));
        return webClient
                .get()
                .uri(uri)
                .exchange()
                .flatMap(response -> Optional.of(response)
                        .filter(r -> r.statusCode().is3xxRedirection())
                        .map(r -> redirect(r, redirects))
                        .orElse(Mono.just(response)));
    }

    private Mono<ClientResponse> redirect(final ClientResponse r, final int redirects) {

        String u = r.headers().header(HttpHeaders.LOCATION).get(0);
        return redirectingRequest(u, redirects + 1);
    }
}
