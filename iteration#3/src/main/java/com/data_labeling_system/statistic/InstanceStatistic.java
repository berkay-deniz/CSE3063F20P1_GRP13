package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Label;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonPropertyOrder({"total number of label assignments", "number of unique label assignment", "number of unique users",
        "most frequent class label and its percentage", "class labels and percentages", "entropy"})
@JsonIgnoreProperties({"labelOccurrences"})
public class InstanceStatistic extends Statistic {
    // For calculating purposes
    private final Map<Label, Integer> labelOccurrences;

    // Required metrics
    @JsonProperty("total number of label assignments")
    private int numOfAssignedLabels;

    @JsonProperty("number of unique label assignment")
    private int numOfAssignedUniqueLabels;

    @JsonProperty("number of unique users")
    private int numOfUniqueUsers;

    @JsonProperty("most frequent class label and its percentage")
    private Map.Entry<Label, Double> mostFrequentLabelAndPercentage;

    @JsonProperty("class labels and percentages")
    private final Map<Label, Double> labelDistributionPercentages;
    private double entropy;

    public InstanceStatistic() {
        labelOccurrences = new HashMap<>();
        labelDistributionPercentages = new HashMap<>();
    }

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
            Label mostFrequentLabel = mostFrequentLabels.get(random);
            mostFrequentLabelAndPercentage = new AbstractMap.SimpleEntry<>(mostFrequentLabel,
                    labelDistributionPercentages.get(mostFrequentLabel));
        }
    }

    public void addAssignedLabel(Label label) {
        Integer occurrence = labelOccurrences.get(label);
        labelOccurrences.put(label, occurrence == null ? 1 : occurrence + 1);
        numOfAssignedLabels++;
    }

    @JsonGetter("class labels and percentages")
    private HashMap<String, String> serializeCustomLabelDistributionPercentages() {
        return serializePercentageMap(labelDistributionPercentages);
    }

    @JsonGetter("most frequent class label and its percentage")
    private Map.Entry<String, String> serializeCustomMostFrequentLabelAndPercentage() {
        if (mostFrequentLabelAndPercentage == null)
            return null;
        String label = mostFrequentLabelAndPercentage.getKey().getText();
        double percentage = ((int) (mostFrequentLabelAndPercentage.getValue() * 10000)) / 100.0;
        return new AbstractMap.SimpleEntry<>(label, ("%" + percentage));
    }

    public void setNumOfUniqueUsers(int numOfUniqueUsers) {
        this.numOfUniqueUsers = numOfUniqueUsers;
    }

    public Map.Entry<Label, Double> getMostFrequentLabelAndPercentage() {
        return mostFrequentLabelAndPercentage;
    }

    public void setNumOfAssignedUniqueLabels(int numOfAssignedUniqueLabels) {
        this.numOfAssignedUniqueLabels = numOfAssignedUniqueLabels;
    }
}
