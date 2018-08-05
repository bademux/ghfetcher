package com.github.bademux.ghfecher.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Getter
public enum Filter {
    RECENT_REPO_ONLY(repo -> repo.isRecentlyModified(ZonedDateTime.now()));

    private final Predicate<GitHubRepo> predicate;

}
