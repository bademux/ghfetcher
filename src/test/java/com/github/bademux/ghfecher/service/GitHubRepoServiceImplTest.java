package com.github.bademux.ghfecher.service;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GitHubRepoServiceImplTest {

    @Test
    public void testGetRepositoriesRateLimit() throws Exception {

        GitHubRepoServiceImpl service = new GitHubRepoServiceImpl(
                userName -> Flux.error(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE)));
        //test
        Throwable throwable = catchThrowable(() ->
                service.findByUserName("test", Collections.emptyList(), Collections.emptyList())
                        .blockFirst()
        );

        //verify
        assertThat(throwable)
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("503 SERVICE_UNAVAILABLE");

    }
}
