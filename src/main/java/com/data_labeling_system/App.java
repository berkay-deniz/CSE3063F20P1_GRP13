package com.data_labeling_system;

import java.io.IOException;

import com.data_labeling_system.util.DataLabelingSystem;

public class App {
    public static void main(String[] args) throws IOException {
    	DataLabelingSystem dataLabelingSystem = new DataLabelingSystem();
    	dataLabelingSystem.startSystem();
    }
}
