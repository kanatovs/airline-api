package com.example.airlineapi.common;

public interface Printable {
    void print();

    // Default interface method (доп. фича из списка)
    default String printableSummary() {
        return getClass().getSimpleName();
    }

    // Static interface method (доп. фича из списка)
    static void printLine(String text) {
        System.out.println(text);
    }
}
