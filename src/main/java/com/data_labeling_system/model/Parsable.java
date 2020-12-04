package com.data_labeling_system.model;

public interface Parsable {
    void parse(String json);
    String stringify(Parsable parsable);
}
