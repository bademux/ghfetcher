package com.github.bademux.ghfecher.config;

import com.github.bademux.ghfecher.client.CacheGitHubClient;
import com.github.bademux.ghfecher.client.GitHubClient;
import com.github.bademux.ghfecher.client.GitHubClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Configuration
public class WebClientConfig {

    @Bean
    public GitHubClient gitHubClient(Map<String, List> cache, @Value("${githabBaseUrl}") URI githubBaseUri) {
        return new CacheGitHubClient(cache, new GitHubClientImpl(githubBaseUri));
    }

}
