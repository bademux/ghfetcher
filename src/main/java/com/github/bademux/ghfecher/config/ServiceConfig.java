package com.github.bademux.ghfecher.config;

import com.github.bademux.ghfecher.client.GitHubClient;
import com.github.bademux.ghfecher.controller.BasicAuthorizationFilter;
import com.github.bademux.ghfecher.controller.BasicAuthorizationFilter.User;
import com.github.bademux.ghfecher.service.GitHubRepoService;
import com.github.bademux.ghfecher.service.GitHubRepoServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@Configuration
public class ServiceConfig {

    @Bean
    public WebFilter basicAuthorizationFilter(@Value("${users}") String[] users) {
        return Stream.of(users)
                .distinct()
                .map(User::fromDelimited)
                .collect(collectingAndThen(toSet(), BasicAuthorizationFilter::new));
    }

    @Bean
    public GitHubRepoService githubRepoService(GitHubClient client) {
        return new GitHubRepoServiceImpl(client);
    }

}
