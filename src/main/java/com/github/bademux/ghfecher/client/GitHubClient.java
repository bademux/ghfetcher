package com.github.bademux.ghfecher.client;

import com.github.bademux.ghfecher.model.GitHubRepo;
import reactor.core.publisher.Flux;

public interface GitHubClient {

    Flux<GitHubRepo> getAllRepositories(String userName);

}
