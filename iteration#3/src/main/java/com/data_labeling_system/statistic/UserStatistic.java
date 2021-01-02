package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonPropertyOrder({"number of datasets assigned", "dataset completeness percentages", "total number of instances labeled",
        "total number of unique instances labeled", "dataset consistency percentages",
        "average time spent in labeling (ns)", "standard deviation of time spent in labeling (ns)"})
@JsonIgnoreProperties("userAssignmentsForDatasets")
public class UserStatistic extends Statistic {
    // For calculating purposes
    private final Set<Dataset> datasetsAssigned;
    private final Map<Dataset, List<Assignment>> userAssignmentsForDatasets;

    // Required metrics
    @JsonProperty("number of datasets assigned")
    private int numOfDatasetsAssigned;

    @JsonProperty("total number of instances labeled")
    private int numOfAssignments;

    @JsonProperty("total number of unique instances labeled")
    private int numOfUniqueInstanceAssignments;

    @JsonProperty("dataset completeness percentages")
    private final Map<Dataset, Double> datasetCompletenessPercentages;

    @JsonProperty("dataset consistency percentages")
    private final Map<Dataset, Double> datasetConsistencyPercentages;

    @JsonProperty("average time spent in labeling (ns)")
    private double avgTimeSpentInLabeling;

    @JsonProperty("standard deviation of time spent in labeling (ns)")
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
                timesSpentForLabeling.add(assignment.getTimeSpentInNanos());
                totalTimeSpent += assignment.getTimeSpentInNanos();
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

    public void addDataset(Dataset dataset) {
        if (!datasetsAssigned.contains(dataset)) {
            datasetsAssigned.add(dataset);
            numOfDatasetsAssigned++;
        }
    }

    @JsonGetter("dataset completeness percentages")
    private HashMap<String, String> serializeCustomDatasetCompleteness() {
        return serializeParsable(datasetCompletenessPercentages);
    }

    @JsonGetter("dataset consistency percentages")
    private HashMap<String, String> serializeCustomDatasetConsistency() {
        return serializeParsable(datasetConsistencyPercentages);
    }

    public Map<Dataset, Double> getDatasetCompletenessPercentages() {
        return datasetCompletenessPercentages;
    }

    public Map<Dataset, Double> getDatasetConsistencyPercentages() {
        return datasetConsistencyPercentages;
    }
}