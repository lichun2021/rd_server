package com.hawk.game.util;

import java.util.function.Predicate;

/**
 * 
 * @author luwentao
 *
 */
public final class Predicates {
    private Predicates() {
    }

    public static <T> Predicate<T> of(Predicate<T> predicate) {
        return predicate;
    }

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return t -> !predicate.test(t);
    }

    public static <T> Predicate<T> and(Predicate<? super T> first, Predicate<? super T> second) {
        return (t) -> first.test(t) && second.test(t);
    }

    public static <T> Predicate<T> or(Predicate<? super T> first, Predicate<? super T> second) {
        return (t) -> first.test(t) || second.test(t);
    }
}
