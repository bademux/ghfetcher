package com.github.bademux.ghfecher.controller;

import com.github.bademux.ghfecher.model.GitHubRepo;
import com.github.bademux.ghfecher.model.GitHubRepoSearchRequest;
import com.github.bademux.ghfecher.service.GitHubRepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class GitHubRepoController {

    private final GitHubRepoService gitHubRepoService;

    @PostMapping("/search")
    Flux<GitHubRepo> search(@RequestBody @Validated GitHubRepoSearchRequest searchRequest) {
        log.debug("searching for request{0}", searchRequest);
        return gitHubRepoService.findByUserName(searchRequest.getUserName(), searchRequest.getFilters(), searchRequest.getSorters());
    }

}
