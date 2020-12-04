package com.data_labeling_system.util;


import java.io.*;

import com.data_labeling_system.model.Dataset;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;


import org.json.JSONObject;

import com.data_labeling_system.model.Dataset;



public class IOManager {
    private final Logger logger;

    public IOManager() {
        logger = Logger.getLogger(IOManager.class);
    }
    
    public String readInputFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }


    public void printFinalDataset(Dataset dataset, String outputFileName) throws JsonGenerationException, JsonMappingException, IOException {

        
       ObjectMapper mapper = new ObjectMapper();
       ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
       writer.writeValue(new File(outputFileName),dataset );
       logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
       


    }
}
