package no.tiger.gtfs.filter.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

class Functions {

    static <S,T> Set<S> setOf(Collection<T> entries, Function<T, S> map) {
        return entries.stream().map(map).collect(Collectors.toSet());
    }

    static <S,T> Map<S, Integer> mapCount(Collection<T> entries, Function<T, S> map) {
        return entries.stream().collect(groupingBy(map, summingInt(it -> 1)));
    }

    static <T> Predicate<T> noMatch(Function<T, String> map, String ... includes) {
        final List<String> includeList = Arrays.asList(includes);
        return (T t) -> !includeList.contains(map.apply(t));
    }

    static <T> Predicate<T> match(Function<T, String> map, String ... includes) {
        final List<String> includeList = Arrays.asList(includes);
        return (T t) -> includeList.contains(map.apply(t));
    }
}
