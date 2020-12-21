package com.data_labeling_system.util;

import com.data_labeling_system.model.Dataset;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class IOManager {
    private final Logger logger;

    public IOManager() {
        logger = Logger.getLogger(IOManager.class);
    }

    public String readInputFile(String fileName) throws IOException {
        // Read json file and return string value
    	String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (NoSuchFileException e) {
            logger.error("Input file '" + fileName + "' not found.", e);
            System.exit(1);
        }
        return json;
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) throws IOException {
    	// Write the final dataset as jsonfile
    	ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File(outputFileName), dataset);
        logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
    }
}
