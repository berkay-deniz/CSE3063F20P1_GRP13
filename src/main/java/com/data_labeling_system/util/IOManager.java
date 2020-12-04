package com.data_labeling_system.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.data_labeling_system.App;

public class IOManager extends App{

	public IOManager(){
		
	}
	
	public String readInputFile(String fileName) throws IOException {
		String inputJSON = new String(Files.readAllBytes(Paths.get(fileName)));
	
         return inputJSON;
	} 
	public void printFinalDataset(String DatasetJSON, String outputFileName)
	{
		
		JSONObject j = new JSONObject();

        j.put("name","AFY");

        String fileName ="output.json";

        try(FileWriter file = new FileWriter(fileName)) {

            file.write(j.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		
		/*	String name="asdasdasdasdasd";
		JSONObject JSONObject = new JSONObject();
		JSONObject.put(name,true);
		
		
		try (FileWriter file = new FileWriter("output.json")) {
			 
            file.write(DatasetJSON.tojSONObject());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
		System.out.println(JSONObject);*/
	}
}
