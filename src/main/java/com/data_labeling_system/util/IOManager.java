package com.data_labeling_system.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.data_labeling_system.model.Dataset;

public class IOManager {

    public IOManager() {

    }

    public String readInputFile(String fileName) throws IOException {
        String inputJSON = new String(Files.readAllBytes(Paths.get(fileName)));

        return inputJSON;
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) {

        JSONObject j = new JSONObject(dataset);
        System.out.println(j.toString(1));
        String fileName = "output.json";

        try (FileWriter file = new FileWriter(fileName)) {

            file.write(j.toString(1));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
