package com.pipes.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic utility for in-memory filtering and sorting of lists (R2, R3, R4).
 *
 * Type bound {@code <T extends Comparable<T>>} demonstrates bounded generics (R2).
 * Lambda Predicate and Comparator parameters satisfy R3.
 * Stream pipeline inside satisfies R4.
 *
 * @param <T> element type; must be self-comparable for the default sort to work
 */
public class FilteredList<T extends Comparable<T>> {

    private final List<T> source;

    public FilteredList(List<T> source) {
        this.source = source;
    }

    /**
     * Return a new list containing elements that pass {@code predicate},
     * sorted by {@code comparator}.
     *
     * @param predicate  lambda filter  (R3 — Predicate functional interface)
     * @param comparator lambda sorter  (R3 — Comparator functional interface)
     * @return filtered + sorted list
     */
    public List<T> query(Predicate<T> predicate, Comparator<T> comparator) {
        return source.stream()          // R4 — Stream
                .filter(predicate)      // intermediate: Predicate (R3)
                .sorted(comparator)     // intermediate: Comparator (R3)
                .collect(Collectors.toList()); // terminal
    }

    /**
     * Return a filtered list using natural ordering (requires T extends Comparable).
     */
    public List<T> filter(Predicate<T> predicate) {
        return source.stream()
                .filter(predicate)
                .sorted()
                .collect(Collectors.toList());
    }
}
