package com.data_labeling_system.statistic;

import com.data_labeling_system.model.*;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class DatasetStatistic {
    private int numOfUsers;
    private double completenessPercentage;
    @JsonProperty("label distribution percentages")
    private final Map<Label, Double> labelDistributionPercentages;

    @JsonProperty("number of unique instances for labels")
    private final Map<Label, Integer> numOfUniqueInstancesForLabels;

    @JsonProperty("user completeness percentages")
    private final Map<User, Double> userCompletenessPercentages;

    @JsonProperty("user consistency percentages")
    private final Map<User, Double> userConsistencyPercentages;

    public DatasetStatistic() {
        labelDistributionPercentages = new HashMap<>();
        numOfUniqueInstancesForLabels = new HashMap<>();
        userCompletenessPercentages = new HashMap<>();
        userConsistencyPercentages = new HashMap<>();
    }

    public void calculateMetrics(Dataset dataset) {
        int totalLabelsAssigned = 0;
        Set<Instance> uniqueInstances = new HashSet<>();
        Map<Label, Set<Instance>> uniqueInstancesForLabels = new HashMap<>();
        Map<Label, Integer> labelOccurrences = new HashMap<>();

        Map<Instance, Set<Label>> uniqueLabelsForInstances = new HashMap<>();
        Map<Instance, Set<User>> uniqueUsersForInstances = new HashMap<>();

        for (Instance instance : dataset.getInstances()) {
            instance.resetStatistic();
        }

        for (Assignment assignment : dataset.getAssignments()) {
            Instance instance = assignment.getInstance();
            uniqueInstances.add(instance);

            for (Label label : assignment.getLabels()) {
                Set<Instance> instanceSet = uniqueInstancesForLabels.get(label);
                if (instanceSet == null) {
                    instanceSet = new HashSet<>();
                    uniqueInstancesForLabels.put(label, instanceSet);
                }
                instanceSet.add(instance);

                Integer occurrence = labelOccurrences.get(label);
                labelOccurrences.put(label, occurrence == null ? 1 : occurrence + 1);
                totalLabelsAssigned++;

                // Instance statistic calculations
                Set<Label> labelSet = uniqueLabelsForInstances.get(instance);
                if (labelSet == null) {
                    labelSet = new HashSet<>();
                    uniqueLabelsForInstances.put(instance, labelSet);
                }
                labelSet.add(label);

                Set<User> userSet = uniqueUsersForInstances.get(instance);
                if (userSet == null) {
                    userSet = new HashSet<>();
                    uniqueUsersForInstances.put(instance, userSet);
                }
                userSet.add(assignment.getUser());

                instance.getStatistic().addAssignedLabel(label);
            }
        }

        // Calculate completeness percentage
        completenessPercentage = (double) uniqueInstances.size() / dataset.getInstances().size();

        // Calculate label distribution percentages
        for (Map.Entry<Label, Integer> entry : labelOccurrences.entrySet()) {
            Label label = entry.getKey();
            int occurrence = entry.getValue();

            labelDistributionPercentages.put(label, (double) occurrence / totalLabelsAssigned);
        }

        // Calculate number of unique instances for labels
        for (Map.Entry<Label, Set<Instance>> entry : uniqueInstancesForLabels.entrySet()) {
            Label label = entry.getKey();
            Set<Instance> instanceSet = entry.getValue();

            numOfUniqueInstancesForLabels.put(label, instanceSet.size());
        }

        numOfUsers = dataset.getUsers().size();

        for (User user : dataset.getUsers()) {
            Double completenessPercentage = user.getStatistic().getDatasetCompletenessPercentages().get(dataset);
            userCompletenessPercentages.put(user, completenessPercentage == null ? 0 : completenessPercentage);
            Double consistencyPercentage = user.getStatistic().getDatasetConsistencyPercentages().get(dataset);
            userConsistencyPercentages.put(user, consistencyPercentage == null ? 0 : consistencyPercentage);
        }

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

        for (Instance instance : dataset.getInstances()) {
            instance.getStatistic().calculateMetrics();
            instance.setFinalLabel();
        }
    }

    @JsonGetter("label distribution percentages")
    private HashMap<String, String> getCustomLabelDistributionPercentages() {
        HashMap<String, String> getCustomLabelDistributionJson = new HashMap<>();
        for (Map.Entry<Label, Double> entry : labelDistributionPercentages.entrySet()) {
            String dataset = "label" + entry.getKey().getId();
            String completeness = "%" + (int) (entry.getValue() * 100);
            getCustomLabelDistributionJson.put(dataset, completeness);
        }
        return getCustomLabelDistributionJson;
    }

    @JsonGetter("number of unique instances for labels")
    private HashMap<String, Integer> getCustomNumOfUniqueInstancesForLabels() {
        HashMap<String, Integer> getCustomLabelDistributionJson = new HashMap<>();
        for (Map.Entry<Label, Integer> entry : numOfUniqueInstancesForLabels.entrySet()) {
            String dataset = "label" + entry.getKey().getId();
            Integer completeness = entry.getValue();
            getCustomLabelDistributionJson.put(dataset, completeness);
        }
        return getCustomLabelDistributionJson;
    }

    @JsonProperty("user completeness percentages")
    private HashMap<String, String> getCustomUserCompleteness() {
        return mapUserToUserId(userCompletenessPercentages);
    }

    @JsonGetter("user consistency percentages")
    private HashMap<String, String> getCustomUserConsistency() {
        return mapUserToUserId(userConsistencyPercentages);
    }

    private HashMap<String, String> mapUserToUserId(Map<User, Double> percentages) {
        HashMap<String, String> customUserJson = new HashMap<>();
        for (Map.Entry<User, Double> entry : percentages.entrySet()) {
            String user = "user" + entry.getKey().getId();
            String percentage = "%" + (int) (entry.getValue() * 100);
            customUserJson.put(user, percentage);
        }
        return customUserJson;
    }

    public double getCompletenessPercentage() {
        return completenessPercentage;
    }

    public Map<Label, Double> getLabelDistributionPercentages() {
        return labelDistributionPercentages;
    }

    public Map<Label, Integer> getNumOfUniqueInstancesForLabels() {
        return numOfUniqueInstancesForLabels;
    }

    public int getNumOfUsers() {
        return numOfUsers;
    }

    public Map<User, Double> getUserCompletenessPercentages() {
        return userCompletenessPercentages;
    }

    public Map<User, Double> getUserConsistencyPercentages() {
        return userConsistencyPercentages;
    }
}
