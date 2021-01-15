package com.data_labeling_system.statistic;

import com.data_labeling_system.model.*;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonPropertyOrder({"completeness percentage", "class distribution based on final instance labels", "number of unique instances for each class label",
        "number of users assigned", "users assigned and their completeness percentages", "users assigned and their consistency percentages"})
public class DatasetStatistic extends Statistic {
    // Required metrics
    @JsonProperty("number of users assigned")
    private int numOfUsers;

    @JsonProperty("completeness percentage")
    private double completenessPercentage;

    @JsonProperty("class distribution based on final instance labels")
    private Map<Label, Double> labelDistributionPercentages;

    @JsonProperty("number of unique instances for each class label")
    private final Map<Label, Integer> numOfUniqueInstancesForLabels;

    @JsonProperty("users assigned and their completeness percentages")
    private final Map<User, Double> userCompletenessPercentages;

    @JsonProperty("users assigned and their consistency percentages")
    private final Map<User, Double> userConsistencyPercentages;

    public DatasetStatistic() {
        labelDistributionPercentages = new HashMap<>();
        numOfUniqueInstancesForLabels = new HashMap<>();
        userCompletenessPercentages = new HashMap<>();
        userConsistencyPercentages = new HashMap<>();
    }

    public void calculateMetrics(Dataset dataset, Map<Integer, Instance> instances,
                                 List<Assignment> assignments, Map<Integer, User> users) {
        Set<Instance> uniqueInstances = new HashSet<>();
        Map<Instance, Set<Label>> uniqueLabelsForInstances = new HashMap<>();
        Map<Instance, Set<User>> uniqueUsersForInstances = new HashMap<>();
        Map<Label, Set<Instance>> uniqueInstancesForLabels = new HashMap<>();

        for (Instance instance : instances.values()) {
            instance.resetStatistic();
        }

        for (Assignment assignment : assignments) {
            assignment.calculateDatasetStatistic(uniqueInstances, uniqueLabelsForInstances, uniqueUsersForInstances, uniqueInstancesForLabels);
        }

        // Calculate completeness percentage
        completenessPercentage = (double) uniqueInstances.size() / instances.size();

        // Calculate number of unique instances for labels
        for (Map.Entry<Label, Set<Instance>> entry : uniqueInstancesForLabels.entrySet()) {
            Label label = entry.getKey();
            Set<Instance> instanceSet = entry.getValue();
            numOfUniqueInstancesForLabels.put(label, instanceSet.size());
        }

        for (User user : users.values()) {
            Double completenessPercentage = user.getStatistic().getDatasetCompletenessPercentages().get(dataset);
            userCompletenessPercentages.put(user, completenessPercentage == null ? 0 : completenessPercentage);
            Double consistencyPercentage = user.getStatistic().getDatasetConsistencyPercentages().get(dataset);
            userConsistencyPercentages.put(user, consistencyPercentage == null ? 0 : consistencyPercentage);
        }

        numOfUsers = users.size();

        // Instance statistic calculations
        for (Map.Entry<Instance, Set<Label>> entry : uniqueLabelsForInstances.entrySet()) {
            Instance instance = entry.getKey();
            Set<Label> labelSet = entry.getValue();
            instance.getStatistic().setNumOfAssignedUniqueLabels(labelSet.size());
        }

        for (Map.Entry<Instance, Set<User>> entry : uniqueUsersForInstances.entrySet()) {
            Instance instance = entry.getKey();
            Set<User> userSet = entry.getValue();
            instance.getStatistic().setNumOfUniqueUsers(userSet.size());
        }

        Map<Label, Integer> finalLabelOccurrences = new HashMap<>();
        int numOfFinalLabels = 0;
        for (Instance instance : instances.values()) {
            instance.getStatistic().calculateMetrics();
            instance.updateFinalLabel();
            // For calculating label distribution percentages
            Label finalLabel = instance.getFinalLabel();
            if (finalLabel != null) {
                Integer occurrence = finalLabelOccurrences.get(finalLabel);
                finalLabelOccurrences.put(finalLabel, occurrence == null ? 1 : occurrence + 1);
                numOfFinalLabels++;
            }
        }

        labelDistributionPercentages = new HashMap<>();
        // Calculate label distribution percentages
        for (Map.Entry<Label, Integer> entry : finalLabelOccurrences.entrySet()) {
            Label label = entry.getKey();
            int occurrence = entry.getValue();
            labelDistributionPercentages.put(label, (double) occurrence / numOfFinalLabels);
        }
    }

    @JsonGetter("class distribution based on final instance labels")
    private HashMap<String, String> serializeCustomLabelDistributionPercentages() {
        return serializePercentageMap(labelDistributionPercentages);
    }

    @JsonGetter("number of unique instances for each class label")
    private HashMap<String, Integer> serializeCustomNumOfUniqueInstancesForLabels() {
        HashMap<String, Integer> getCustomLabelDistributionJson = new HashMap<>();
        for (Map.Entry<Label, Integer> entry : numOfUniqueInstancesForLabels.entrySet()) {
            String dataset = entry.getKey().getText();
            Integer completeness = entry.getValue();
            getCustomLabelDistributionJson.put(dataset, completeness);
        }
        return getCustomLabelDistributionJson;
    }

    @JsonGetter("users assigned and their completeness percentages")
    private HashMap<String, String> serializeCustomUserCompleteness() {
        return serializeParsable(userCompletenessPercentages);
    }

    @JsonGetter("users assigned and their consistency percentages")
    private HashMap<String, String> serializeCustomUserConsistency() {
        return serializeParsable(userConsistencyPercentages);
    }

    @JsonGetter("completeness percentage")
    private String getCustomCompletenessPercentage() {
        return "%" + ((int) (completenessPercentage * 10000)) / 100.0;
    }
}