package com.github.bademux.ghfecher.client;

import com.github.bademux.ghfecher.model.GitHubRepo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheGitHubClientTest {

    @Rule
    public MockWebServer server = new MockWebServer();
    private CacheGitHubClient client = new CacheGitHubClient(new ConcurrentHashMap<>(), new GitHubClientImpl(server.url("").uri()));

    @Test
    public void testGetAllRepositoriesCache() throws Exception {
        //prepare
        String userName = "test";
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"name\": \"testCached\"}"));
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\"name\": \"testNotCached\"}"));

        //test
        GitHubRepo responseCached = client.getAllRepositories(userName)
                .blockFirst();
        GitHubRepo responseNotCached = client.getAllRepositories(userName)
                .blockFirst();

        //verify
        assertThat(responseCached)
                .matches(ghRepo -> "testCached".equals(ghRepo.getName()), "user name validation");
        assertThat(responseNotCached)
                .matches(ghRepo -> "testCached".equals(ghRepo.getName()), "user name validation");
        assertThat(server.getRequestCount()).isOne();

    }


}
