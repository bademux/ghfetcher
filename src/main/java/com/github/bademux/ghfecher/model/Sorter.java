package com.github.bademux.ghfecher.model;

import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

@Setter
public final class Sorter {
    private static final String FIELD_NAME = "name";

    @NotNull
    @NotEmpty
    private String field;

    @NotNull
    private Direction direction = Direction.DESC;

    public boolean isAsc() {
        return direction == Direction.ASC;
    }

    public boolean isDesc() {
        return direction == Sorter.Direction.DESC;
    }

    //TODO: replace switch with inheritance using this.field as discriminator
    public Comparator<GitHubRepo> getComparator() {
        Comparator<GitHubRepo> comparator = getRawComparator(field);
        return isDesc() ? comparator : comparator.reversed();
    }

    private static Comparator<GitHubRepo> getRawComparator(String field) {
        switch (field) {
            case FIELD_NAME:
                return GitHubRepo.createComparatorByName();
            default:
        }
        throw new IllegalStateException("unknow field '" + field + "' to sort by");
    }

    public enum Direction {
        ASC, DESC
    }

}
