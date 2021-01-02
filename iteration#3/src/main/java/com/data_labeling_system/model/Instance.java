package com.data_labeling_system.model;

import com.data_labeling_system.DataLabelingSystem;
import com.data_labeling_system.statistic.InstanceStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;

@JsonIgnoreProperties({"statistic"})
public class Instance implements Parsable {
    private int id;
    private String instance;
    private InstanceStatistic statistic;
    private Label finalLabel;
    private final Logger logger;

    public Instance(String json) {
        logger = Logger.getLogger(DataLabelingSystem.class);
        statistic = new InstanceStatistic();
        parse(json);
    }

    @Override
    public void parse(String json) {
        // Parse the Instance json using org.json library
        JSONObject object = new JSONObject(json);
        id = object.getInt("id");
        instance = object.getString("instance");
    }

    public void printInstance() {
        System.out.println(this.instance);
    }

    public void updateFinalLabel() {
        Map.Entry<Label, Double> mostFrequentLabelAndPercentage = statistic.getMostFrequentLabelAndPercentage();
        if (mostFrequentLabelAndPercentage != null)
            finalLabel = mostFrequentLabelAndPercentage.getKey();
    }

    public String[] divideIntoWords() {
        return this.instance.split(" ");
    }

    public void printMetrics(int datasetId) {
        statistic.printMetrics("metrics/instances/dataset" + datasetId + "/instance" + id + ".json");
    }

    public void logAssignmentInfo(int userId, String userName, String classLabels) {
        logger.info("user id: " + userId + " " + userName + " tagged instance id: " +
                id + " with class labels: [" + classLabels + "]" + ", instance: \"" +
                instance + "\"");
    }

    public int getId() {
        return id;
    }

    public InstanceStatistic getStatistic() {
        return statistic;
    }

    public void resetStatistic() {
        statistic = new InstanceStatistic();
    }

    public Label getFinalLabel() {
        return finalLabel;
    }
}
