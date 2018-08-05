package com.github.bademux.ghfecher.service;

import com.github.bademux.ghfecher.model.Filter;
import com.github.bademux.ghfecher.model.GitHubRepo;
import com.github.bademux.ghfecher.model.Sorter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface GitHubRepoService {

    Flux<GitHubRepo> findByUserName(String userName, List<Filter> filters, List<Sorter> sorters);

}
