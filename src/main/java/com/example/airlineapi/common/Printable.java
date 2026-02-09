package com.example.airlineapi.common;

public interface Printable {
    void print();

    // Default interface method
    default String printableSummary() {
        return getClass().getSimpleName();
    }

    // Static interface method
    static void printLine(String text) {
        System.out.println(text);
    }
}
