package com.github.bademux.ghfecher.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class GitHubRepoSearchRequest {
    @NotNull
    @NotEmpty
    private String userName;
    @NotNull
    private List<Filter> filters = new ArrayList<>();
    @NotNull
    private List<Sorter> sorters = new ArrayList<>();

}
