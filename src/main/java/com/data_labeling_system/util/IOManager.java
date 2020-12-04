package com.data_labeling_system.util;

import com.data_labeling_system.model.Dataset;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOManager {
    Logger logger;

    public IOManager() {
        logger = Logger.getLogger(IOManager.class);
    }
    
    public String readInputFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) {
        JSONObject j = new JSONObject(dataset);
        System.out.println(j.toString(1));

        try (FileWriter file = new FileWriter(outputFileName)) {
            file.write(j.toString(1));
            logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
