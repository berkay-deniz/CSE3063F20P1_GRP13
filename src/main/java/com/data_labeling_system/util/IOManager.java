package com.data_labeling_system.util;


import com.data_labeling_system.model.Dataset;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOManager {
    private final Logger logger;

    public IOManager() {
        logger = Logger.getLogger(IOManager.class);
    }

    public String readInputFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File(outputFileName), dataset);
        logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
    }
}
