package com.data_labeling_system.statistic;

import com.data_labeling_system.model.*;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonIgnoreProperties("userAssignmentsForDatasets")
public class UserStatistic {
    // For calculating purposes
    private final Set<Dataset> datasetsAssigned;
    private final Map<Dataset, List<Assignment>> userAssignmentsForDatasets;

    // Required metrics
    private int numOfDatasetsAssigned;
    private int numOfAssignments;
    private int numOfUniqueInstanceAssignments;

    @JsonProperty("dataset completeness percentages")
    private final Map<Dataset, Double> datasetCompletenessPercentages;

    @JsonProperty("dataset consistency percentages")
    private final Map<Dataset, Double> datasetConsistencyPercentages;
    private double avgTimeSpentInLabeling;
    private double stdDevOfTimeInLabeling;

    public UserStatistic() {
        userAssignmentsForDatasets = new HashMap<>();
        datasetCompletenessPercentages = new HashMap<>();
        datasetConsistencyPercentages = new HashMap<>();
        datasetsAssigned = new HashSet<>();
    }

    public void calculateMetrics() {
        stdDevOfTimeInLabeling = 0;
        numOfUniqueInstanceAssignments = 0;
        Set<Instance> uniqueInstances = new HashSet<>();
        List<Long> timesSpentForLabeling = new ArrayList<>();
        double totalTimeSpent = 0;

        for (Map.Entry<Dataset, List<Assignment>> entry : userAssignmentsForDatasets.entrySet()) {
            Dataset dataset = entry.getKey();
            List<Assignment> assignments = entry.getValue();
            Map<Instance, List<Label>> assignedLabelsForInstances = new HashMap<>();

            int numOfUniqueDatasetAssignments = 0;
            for (Assignment assignment : assignments) {
                Instance instance = assignment.getInstance();

                // To calculate unique instance assignments
                if (!uniqueInstances.contains(instance)) {
                    uniqueInstances.add(instance);
                    numOfUniqueDatasetAssignments++;
                }

                // To calculate dataset consistency percentage
                List<Label> assignedLabels = assignedLabelsForInstances.get(instance);
                if (assignedLabels == null) {
                    assignedLabels = new ArrayList<>();
                    assignedLabelsForInstances.put(instance, assignedLabels);
                }
                assignedLabels.addAll(assignment.getLabels());

                // To calculate average & standard deviation of time spent in labeling
                timesSpentForLabeling.add(assignment.getTimeSpentInMillis());
                totalTimeSpent += assignment.getTimeSpentInMillis();
            }
            numOfUniqueInstanceAssignments += numOfUniqueDatasetAssignments;

            // Calculate dataset completeness percentage
            double completeness = (double) (numOfUniqueDatasetAssignments) / dataset.getInstances().size();
            datasetCompletenessPercentages.put(dataset, completeness);

            // Calculate consistency percentage
            double consistency = calculateConsistencyPercentage(assignedLabelsForInstances);
            datasetConsistencyPercentages.put(dataset, consistency);
        }

        avgTimeSpentInLabeling = totalTimeSpent / timesSpentForLabeling.size();

        for (double timeSpent : timesSpentForLabeling) {
            stdDevOfTimeInLabeling += Math.pow(timeSpent - avgTimeSpentInLabeling, 2);
        }
        stdDevOfTimeInLabeling = Math.sqrt(stdDevOfTimeInLabeling / timesSpentForLabeling.size());
    }

    private double calculateConsistencyPercentage(Map<Instance, List<Label>> assignedLabelsForInstances) {
        double sumOfConsistencies = 0;

        for (List<Label> labels : assignedLabelsForInstances.values()) {
            Map<Label, Integer> labelOccurrences = new HashMap<>();
            for (Label label : labels) {
                Integer occurrence = labelOccurrences.get(label);
                labelOccurrences.put(label, occurrence == null ? 1 : occurrence + 1);
            }

            Map.Entry<Label, Integer> mostRecurrent = null;
            for (Map.Entry<Label, Integer> e : labelOccurrences.entrySet()) {
                if (mostRecurrent == null || e.getValue() > mostRecurrent.getValue())
                    mostRecurrent = e;
            }

            assert mostRecurrent != null;
            sumOfConsistencies += (double) mostRecurrent.getValue() / labels.size();
        }

        return sumOfConsistencies / assignedLabelsForInstances.size();
    }

    public void addAssignment(Dataset dataset, Assignment assignment) {
        List<Assignment> assignments = userAssignmentsForDatasets.get(dataset);
        if (assignments == null) {
            assignments = new ArrayList<>();
            userAssignmentsForDatasets.put(dataset, assignments);
        }
        assignments.add(assignment);
        numOfAssignments++;
    }

    @JsonGetter("dataset completeness percentages")
    public HashMap<String, Double> getCustomDatasetCompleteness() {
        return mapDatasetToDatasetId(datasetCompletenessPercentages);
    }

    @JsonGetter("dataset consistency percentages")
    public HashMap<String, Double> getCustomDatasetConsistency() {
        return mapDatasetToDatasetId(datasetConsistencyPercentages);
    }

    private HashMap<String, Double> mapDatasetToDatasetId(Map<Dataset, Double> percentages) {
        HashMap<String, Double> customDatasetJson = new HashMap<>();
        for (Map.Entry<Dataset, Double> entry : percentages.entrySet()) {
            String dataset = "dataset" + entry.getKey().getId();
            double completeness = entry.getValue();
            customDatasetJson.put(dataset, completeness);
        }
        return customDatasetJson;
    }

    public void addDataset(Dataset dataset) {
        if (!datasetsAssigned.contains(dataset)) {
            datasetsAssigned.add(dataset);
            numOfDatasetsAssigned++;
        }
    }

    public Map<Dataset, List<Assignment>> getUserAssignmentsForDatasets() {
        return userAssignmentsForDatasets;
    }

    public int getNumOfDatasetsAssigned() {
        return numOfDatasetsAssigned;
    }

    public int getNumOfAssignments() {
        return numOfAssignments;
    }

    public int getNumOfUniqueInstanceAssignments() {
        return numOfUniqueInstanceAssignments;
    }

    public Map<Dataset, Double> getDatasetCompletenessPercentages() {
        return datasetCompletenessPercentages;
    }

    public Map<Dataset, Double> getDatasetConsistencyPercentages() {
        return datasetConsistencyPercentages;
    }

    public double getAvgTimeSpentInLabeling() {
        return avgTimeSpentInLabeling;
    }

    public double getStdDevOfTimeInLabeling() {
        return stdDevOfTimeInLabeling;
    }
}
