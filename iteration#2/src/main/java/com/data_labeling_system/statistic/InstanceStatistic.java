package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Label;

import java.util.HashMap;
import java.util.Map;

public class InstanceStatistic {
    private final Map<Label, Integer> labelOccurrences;

    private int numOfAssignments;
    private int numOfUniqueAssignments;
    private int numOfUniqueUsers;
    private Label mostFrequentLabel;
    private final Map<Label, Double> labelDistributionPercentages;
    private double entropy;

    public InstanceStatistic() {
        labelOccurrences = new HashMap<>();
        labelDistributionPercentages = new HashMap<>();
    }

    public void calculateMetrics() {
        entropy = 0;
        Map.Entry<Label, Integer> mostRecurrent = null;
        for (Map.Entry<Label, Integer> entry : labelOccurrences.entrySet()) {
            Label label = entry.getKey();
            int occurrence = entry.getValue();

            if (mostRecurrent == null || occurrence > mostRecurrent.getValue())
                mostRecurrent = entry;

            double distributionPercentage = (double) occurrence / labelOccurrences.size();
            labelDistributionPercentages.put(label, distributionPercentage);

            entropy -= distributionPercentage * (Math.log(distributionPercentage) / Math.log(numOfUniqueAssignments));
        }

        assert mostRecurrent != null;
        mostFrequentLabel = mostRecurrent.getKey();
    }

    public void incrementLabelOccurrence(Label label) {
        Integer occurrence = labelOccurrences.get(label);
        labelOccurrences.put(label, occurrence == null ? 1 : occurrence + 1);
    }

    public void incrementNumOfAssignments() {
        numOfAssignments++;
    }

    public void setNumOfUniqueAssignments(int numOfUniqueAssignments) {
        this.numOfUniqueAssignments = numOfUniqueAssignments;
    }

    public void setNumOfUniqueUsers(int numOfUniqueUsers) {
        this.numOfUniqueUsers = numOfUniqueUsers;
    }

    public Map<Label, Integer> getLabelOccurrences() {
        return labelOccurrences;
    }

    public int getNumOfAssignments() {
        return numOfAssignments;
    }

    public int getNumOfUniqueAssignments() {
        return numOfUniqueAssignments;
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
