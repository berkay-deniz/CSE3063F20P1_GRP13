package com.data_labeling_system.model;

import com.data_labeling_system.DataLabelingSystem;
import com.data_labeling_system.statistic.InstanceStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

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

    public void printInstance(){
        System.out.println(this.instance);
    }

    public void updateFinalLabel() {
        Map.Entry<Label, Double> mostFrequentLabelAndPercentage = statistic.getMostFrequentLabelAndPercentage();
        if (mostFrequentLabelAndPercentage != null)
            finalLabel = mostFrequentLabelAndPercentage.getKey();
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

    public String[] divideIntoWords() {
        String[] words = this.instance.split(" ");
        return words;
    }

    public String[] divideIntoSentences() {
        String str = this.instance;
        String strInstance[] = str.replaceAll("\\.", " ").split("\\s+");
        return strInstance;
    }
}
