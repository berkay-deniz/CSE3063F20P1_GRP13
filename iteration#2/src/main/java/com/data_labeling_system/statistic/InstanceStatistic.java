package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Label;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties({"labelOccurrences"})
public class InstanceStatistic extends Statistic {
    // For calculating purposes
    private final Map<Label, Integer> labelOccurrences;

    // Required metrics
    private int numOfAssignedLabels;
    private int numOfAssignedUniqueLabels;
    private int numOfUniqueUsers;
    private Label mostFrequentLabel;
    @JsonProperty("label distribution percentages")
    private final Map<Label, Double> labelDistributionPercentages;
    private double entropy;

    public InstanceStatistic() {
        labelOccurrences = new HashMap<>();
        labelDistributionPercentages = new HashMap<>();
    }

    @Override
    public void calculateMetrics() {
        entropy = 0;
        List<Label> mostFrequentLabels = new ArrayList<>();
        int maxOccurrence = 0;

        int totalLabelAmount = 0;
        for (int occurrence : labelOccurrences.values()) {
            totalLabelAmount += occurrence;
        }

        for (Map.Entry<Label, Integer> entry : labelOccurrences.entrySet()) {
            Label label = entry.getKey();
            int occurrence = entry.getValue();

            if (occurrence > maxOccurrence) {
                mostFrequentLabels.clear();
            }

            if (mostFrequentLabels.isEmpty()) {
                mostFrequentLabels.add(label);
                maxOccurrence = occurrence;
            } else if (occurrence == maxOccurrence) {
                mostFrequentLabels.add(label);
            }

            double distributionPercentage = (double) occurrence / totalLabelAmount;
            labelDistributionPercentages.put(label, distributionPercentage);

            entropy -= distributionPercentage * (Math.log(distributionPercentage) / Math.log(numOfAssignedUniqueLabels));
        }

        if (!mostFrequentLabels.isEmpty()) {
            int random = (int) (Math.random() * mostFrequentLabels.size());
            mostFrequentLabel = mostFrequentLabels.get(random);
        }
    }

    public void addAssignedLabel(Label label) {
        Integer occurrence = labelOccurrences.get(label);
        labelOccurrences.put(label, occurrence == null ? 1 : occurrence + 1);
        numOfAssignedLabels++;
    }

    public void setNumOfAssignedUniqueLabels(int numOfAssignedUniqueLabels) {
        this.numOfAssignedUniqueLabels = numOfAssignedUniqueLabels;
    }

    @JsonGetter("label distribution percentages")
    private HashMap<String, String> getCustomLabelDistributionPercentages() {
        HashMap<String, String> customLabelJson = new HashMap<>();
        for (Map.Entry<Label, Double> entry : labelDistributionPercentages.entrySet()) {
            String label = entry.getKey().getText();
            String percentage = "%" + (int) (entry.getValue() * 100);
            customLabelJson.put(label, percentage);
        }
        return customLabelJson;
    }

    public void setNumOfUniqueUsers(int numOfUniqueUsers) {
        this.numOfUniqueUsers = numOfUniqueUsers;
    }

    public Map<Label, Integer> getLabelOccurrences() {
        return labelOccurrences;
    }

    public int getNumOfAssignedLabels() {
        return numOfAssignedLabels;
    }

    public int getNumOfAssignedUniqueLabels() {
        return numOfAssignedUniqueLabels;
    }

    public int getNumOfUniqueUsers() {
        return numOfUniqueUsers;
    }

    public Label getMostFrequentLabel() {
        return mostFrequentLabel;
    }

    public Map<Label, Double> getLabelDistributionPercentages() {
        return labelDistributionPercentages;
    }

    public double getEntropy() {
        return entropy;
    }
}
