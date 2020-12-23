package com.data_labeling_system.util;

import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.User;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class IOManager {
    private final Logger logger;

    public IOManager() {
        logger = Logger.getLogger(IOManager.class);
    }

    public String readInputFile(String fileName) {
        // Read json file and return string value
        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (NoSuchFileException e) {
            logger.error("Input file '" + fileName + "' not found.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        return json;
    }

    public void printFinalDataset(Dataset dataset, String outputFileName) {
        // Write the final dataset as jsonfile
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(outputFileName), dataset);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
    }
    public void printMetrics(List<Dataset> datasets, List<User> users) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            for(User user : users) {

                writer.writeValue(new File("metrics/user"+user.getId()+".json"),user.getStatistic());
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
       // logger.info("Final u printed to '" + outputFileName + "' successfully.");


    }

}