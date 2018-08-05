package com.github.bademux.ghfecher.client;

import com.github.bademux.ghfecher.model.GitHubRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.cache.CacheFlux;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class CacheGitHubClient implements GitHubClient {

    private final Map<String, List> cache;
    private final GitHubClient delegate;

    @Override
    public Flux<GitHubRepo> getAllRepositories(String userName) {
        log.debug("getting all repo for user '{}' ", userName);
        return CacheFlux
                .lookup(cache, userName, GitHubRepo.class)
                .onCacheMissResume(() -> delegate.getAllRepositories(userName));
    }

}
