package com.github.bademux.ghfecher.model;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHubRepoTest {

    static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern(GitHubRepo.ISO8601);

    @Test
    public void testIsRecentlyModifiedNow() {
        //prepare
        ZonedDateTime now = ZonedDateTime.parse("2018-07-30T02:27:25.00Z", PATTERN);
        GitHubRepo repo = new GitHubRepo();
        ZonedDateTime updatedAt = ZonedDateTime.parse("2018-07-30T02:27:25.00Z", PATTERN);
        repo.setUpdatedAt(updatedAt);
        //test
        boolean isRecentlyModified = repo.isRecentlyModified(now);
        //verify
        assertThat(isRecentlyModified).isTrue();
    }

    @Test
    public void testIsRecentlyModified3Month() {
        //prepare
        ZonedDateTime now = ZonedDateTime.parse("2018-07-30T02:27:25.00Z", PATTERN);
        GitHubRepo repo = new GitHubRepo();
        ZonedDateTime updatedAt = ZonedDateTime.parse("2018-04-30T02:27:25.00Z", PATTERN);
        repo.setUpdatedAt(updatedAt);
        //test
        boolean isRecentlyModified = repo.isRecentlyModified(now);
        //verify
        assertThat(isRecentlyModified).isTrue();
    }

    @Test
    public void testIsRecentlyModified3MonthAndOneMilliSecond() {
        //prepare
        ZonedDateTime now = ZonedDateTime.parse("2018-07-30T02:27:25.00Z", PATTERN);
        GitHubRepo repo = new GitHubRepo();
        ZonedDateTime updatedAt = ZonedDateTime.parse("2018-04-30T02:27:24.99Z", PATTERN);
        repo.setUpdatedAt(updatedAt);
        //test
        boolean isRecentlyModified = repo.isRecentlyModified(now);
        //verify
        assertThat(isRecentlyModified).isFalse();
    }
}