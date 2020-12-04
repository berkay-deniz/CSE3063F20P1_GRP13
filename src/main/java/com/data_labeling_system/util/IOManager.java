package com.data_labeling_system.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.data_labeling_system.model.Dataset;

public class IOManager {

	public IOManager(){

	}

	public String readInputFile(String fileName) throws IOException {
		String inputJSON = new String(Files.readAllBytes(Paths.get(fileName)));

		return inputJSON;
	}

	public void printFinalDataset(Dataset datasetJSON, String outputFileName) {
		System.out.println(datasetJSON);
		JSONObject j = new JSONObject();

		String fileName = "output.json";

		try (FileWriter file = new FileWriter(fileName)) {

			file.write(j.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
