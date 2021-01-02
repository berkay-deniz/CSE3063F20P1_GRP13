package com.data_labeling_system.model;

import com.data_labeling_system.DataLabelingSystem;
import com.data_labeling_system.statistic.InstanceStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

@JsonPropertyOrder({"id", "instance", "final label"})
@JsonIgnoreProperties({"statistic"})
public class Instance implements Parsable {
    private int id;
    @JsonProperty("instance")
    private String instance;
    private InstanceStatistic statistic;
    @JsonProperty("final label")
    private Label finalLabel;
    private final Logger logger;

    public Instance(String json) {
        logger = Logger.getLogger(Instance.class);
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

    public void printInstance(){
        System.out.println(this.instance);
    }

    public void updateFinalLabel() {
        Map.Entry<Label, Double> mostFrequentLabelAndPercentage = statistic.getMostFrequentLabelAndPercentage();
        if (mostFrequentLabelAndPercentage != null)
            finalLabel = mostFrequentLabelAndPercentage.getKey();
    }

    public void printMetrics(int datasetId) {
        String folderPath = "metrics/instances/dataset" + datasetId;
        File folder = new File(folderPath);
        if (folder.mkdir())
            logger.info("'" + folderPath + "' folder has been created.");
        statistic.printMetrics(folderPath + "/instance" + id + ".json");
    }

    public void logAssignmentInfo(int userId, String userName, String classLabels) {
        logger.info("user id: " + userId + " " + userName + " tagged instance id: " +
                id + " with class labels: [" + classLabels + "]" + ", instance: \"" +
                instance + "\"");
    }

    public void resetStatistic() {
        statistic = new InstanceStatistic();
    }

    public String[] divideIntoWords() {
        return this.instance.split(" ");
    }

    public String[] divideIntoSentences() {
        String str = this.instance;
        return str.split("[.?!]");
    }

    public int getId() {
        return id;
    }

    public InstanceStatistic getStatistic() {
        return statistic;
    }

    public Label getFinalLabel() {
        return finalLabel;
    }

    public String getInstance() {
        return instance;
    }
}
