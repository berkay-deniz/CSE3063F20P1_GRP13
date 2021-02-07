package com.data_labeling_system.model;

import com.data_labeling_system.statistic.InstanceStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

import java.util.Map;

@JsonIgnoreProperties({"statistic"})
public class Instance implements Parsable {
    private int id;
    private String instance;
    private InstanceStatistic statistic;
    private Label finalLabel;

    public Instance(String json) {
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

    public int getId() {
        return id;
    }

    public String getInstance() {
        return instance;
    }

    public InstanceStatistic getStatistic() {
        return statistic;
    }

    public void resetStatistic() {
        statistic = new InstanceStatistic();
    }

    public void setFinalLabel() {
        Map.Entry<Label, Double> mostFrequentLabelAndPercentage = statistic.getMostFrequentLabelAndPercentage();
        if (mostFrequentLabelAndPercentage != null)
            finalLabel = mostFrequentLabelAndPercentage.getKey();
    }

    public Label getFinalLabel() {
        return finalLabel;
    }
}
