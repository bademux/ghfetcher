package com.github.bademux.ghfecher.service;

import com.github.bademux.ghfecher.client.GitHubClient;
import com.github.bademux.ghfecher.model.Filter;
import com.github.bademux.ghfecher.model.GitHubRepo;
import com.github.bademux.ghfecher.model.Sorter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class GitHubRepoServiceImpl implements GitHubRepoService {

    private final GitHubClient client;

    @Override
    public Flux<GitHubRepo> findByUserName(String userName, List<Filter> filters, List<Sorter> sorters) {
        log.debug("find by '{}'  filters: '{}', sorters: '{}'", userName, filters, sorters);
        Flux<GitHubRepo> repositories = client.getAllRepositories(userName)
                .doOnError(this::handleError);
        applyRuntimeFilters(repositories, filters);
        applyRuntimeSorter(repositories, sorters);
        return repositories;
    }

    @SneakyThrows
    private void handleError(Throwable throwable) {
        throw throwable;
    }

    private void applyRuntimeFilters(Flux<GitHubRepo> repositories, List<Filter> filters) {
        filters.stream()
                .map(Filter::getPredicate)
                .forEachOrdered(repositories::filter);
    }

    private void applyRuntimeSorter(Flux<GitHubRepo> repositories, List<Sorter> sorters) {
        sorters.stream()
                .map(Sorter::getComparator)
                .forEachOrdered(repositories::sort);
    }

}
