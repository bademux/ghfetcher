package com.github.bademux.ghfecher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

/**
 * TODO: extract Controller DTO and WebClient DTO. Sample request data flow: ControllerDTO->GitHubRepo->WebClientDto
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepo {

    static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss[.SSS]X"; // hackfix fo java8 DateTime formatter

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO8601)
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO8601)
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
    @JsonProperty("language")
    private String language;

    public boolean isRecentlyModified(ZonedDateTime now) {
        ZonedDateTime beforeDate = now.minus(3, ChronoUnit.MONTHS)
                .minus(1, ChronoUnit.MILLIS);
        return updatedAt.isAfter(beforeDate);
    }

    public static Comparator<GitHubRepo> createComparatorByName() {
        return GitHubRepo::sortByName;
    }

    protected static int sortByName(GitHubRepo left, GitHubRepo right) {
        return left.getName().compareTo(right.getName());
    }

}
