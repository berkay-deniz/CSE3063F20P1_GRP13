package com.data_labeling_system.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

import org.json.JSONObject;

import com.data_labeling_system.model.Dataset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IOManager {

    public IOManager() {

    }

    public String readInputFile(String fileName) throws IOException {
        String inputJSON = new String(Files.readAllBytes(Paths.get(fileName)));

        return inputJSON;
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) throws JsonGenerationException, JsonMappingException, IOException {

        
       ObjectMapper mapper = new ObjectMapper();
       mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, dataset);
       ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
       writer.writeValue(new File("output.json"),dataset );
        

       

    }
}
