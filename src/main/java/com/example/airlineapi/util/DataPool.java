package com.example.airlineapi.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class DataPool<T> {
    private final List<T> data = new ArrayList<>();

    public void add(T item) {
        data.add(item);
    }

    public List<T> all() {
        return new ArrayList<>(data);
    }

    public List<T> filter(Predicate<T> predicate) {
        return data.stream().filter(predicate).toList();
    }

    public List<T> sorted(Comparator<T> comparator) {
        return data.stream().sorted(comparator).toList();
    }
}
