package com.data_labeling_system;

import com.data_labeling_system.util.DataLabelingSystem;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        // Create a DataLabelingSystem instance to start system
    	DataLabelingSystem dataLabelingSystem = new DataLabelingSystem();
        dataLabelingSystem.startSystem();
    }
}
