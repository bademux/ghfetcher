package com.github.bademux.ghfecher.client;

import com.github.bademux.ghfecher.model.GitHubRepo;
import com.github.bademux.ghfecher.utils.PagingAwareDispatcher;
import com.github.bademux.ghfecher.utils.Utils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GitHubClientTest {

    @Rule
    public MockWebServer server = new MockWebServer();
    private GitHubClientImpl client = new GitHubClientImpl(server.url("").uri());

    @Test
    public void testGetRepositories() throws Exception {
        //prepare
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"name\": \"test\"}"));
        //test
        GitHubRepo response = client.getRepositories("test", 1)
                .block()
                .bodyToFlux(GitHubRepo.class)
                .blockFirst();
        //verify
        assertThat(response)
                .matches(ghRepo -> "test".equals(ghRepo.getName()), "user name validation");

    }

    @Test
    public void testGetRepositoriesRateLimit() throws Exception {
        //prepare
        server.enqueue(new MockResponse().setResponseCode(503));

        //test
        Throwable throwable = catchThrowable(() ->
                client.getRepositories("test", 1)
                        .block()
                        .bodyToFlux(GitHubRepo.class)
                        .blockFirst()
        );

        //verify
        assertThat(throwable)
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("503 Service Unavailable");

    }

    @Test
    public void testGetAllRepositories() throws Exception {
        //prepare
        String userName = "test";
        GitHubClient clientForUser = new GitHubClientImpl(server.url("").uri());
        List<GitHubRepo> responses = Utils.createItems(299);
        server.setDispatcher(PagingAwareDispatcher.of(responses));

        int expectedRequestNumber = 3;

        //test
        List<GitHubRepo> response = clientForUser.getAllRepositories(userName)
                .collectList()
                .block();

        //verify
        Iterator<RecordedRequest> recordedRequests = getAndWaitForAllRequest(expectedRequestNumber);
        //verify requests
        assertThat(recordedRequests)
                .filteredOn(Objects::nonNull)
                .extracting(RecordedRequest::getRequestLine)
                .containsExactlyInAnyOrder(
                        "GET /users/test/repos?per_page=100&page=1 HTTP/1.1",
                        "GET /users/test/repos?per_page=100&page=2 HTTP/1.1",
                        "GET /users/test/repos?per_page=100&page=3 HTTP/1.1"
                );
        //verify responses
        String[] expected = IntStream.range(0, 299).mapToObj(value -> "test_name_" + value).toArray(String[]::new);
        assertThat(response)
                .extracting(GitHubRepo::getName)
                .containsExactlyInAnyOrder(expected);

    }

    private Iterator<RecordedRequest> getAndWaitForAllRequest(int expectedRequestNumber) throws InterruptedException {
        Stream.Builder<RecordedRequest> recordedRequestsBuilder = Stream.builder();
        while (expectedRequestNumber-- > 0) {
            recordedRequestsBuilder.add(server.takeRequest(1, TimeUnit.SECONDS));
        }
        return recordedRequestsBuilder.build()
                .iterator();
    }

}
