package com.data_labeling_system;

import java.io.IOException;

import com.data_labeling_system.util.IOManager;

public class App {
    public static void main(String[] args) throws IOException {
        IOManager ıoManager = new IOManager();
        String inputJSON=ıoManager.readInputFile("input1.json");
        ıoManager.printFinalDataset("DatasetJSON", "outputFileName");
    }
}
