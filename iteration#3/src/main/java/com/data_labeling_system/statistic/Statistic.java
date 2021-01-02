package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.Parsable;
import com.data_labeling_system.model.User;
import com.data_labeling_system.DataLabelingSystem;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Statistic {
    Logger logger;

    public Statistic() {
        logger = Logger.getLogger(DataLabelingSystem.class);
    }

    public void printMetrics(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(fileName), this);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    };

    protected HashMap<String, String> getStringStringHashMap(Map<Label, Double> labelDistributionPercentages) {
        HashMap<String, String> getCustomLabelDistributionJson = new HashMap<>();
        for (Map.Entry<Label, Double> entry : labelDistributionPercentages.entrySet()) {
            String dataset = entry.getKey().getText();
            String percentage = "%" + ((int) (entry.getValue() * 10000)) / 100.0;
            getCustomLabelDistributionJson.put(dataset, percentage);
        }
        return getCustomLabelDistributionJson;
    }

    protected HashMap<String, String> mapParsableToParsableId(Map<? extends Parsable, Double> percentages) {
        HashMap<String, String> customUserJson = new HashMap<>();
        for (Map.Entry<? extends Parsable, Double> entry : percentages.entrySet()) {
            String id = "user" + entry.getKey().getId();
            if (entry instanceof User)
                id = "user" + entry.getKey().getId();
            else if (entry instanceof Dataset)
                id = "dataset" + entry.getKey().getId();
            String percentage = "%" + ((int) (entry.getValue() * 10000)) / 100.0;
            customUserJson.put(id, percentage);
        }
        return customUserJson;
    }
}
