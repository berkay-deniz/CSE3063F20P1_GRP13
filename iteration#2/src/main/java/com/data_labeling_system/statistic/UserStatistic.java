package com.data_labeling_system.statistic;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;

import java.util.*;

public class UserStatistic {
    private final Map<Dataset, List<Assignment>> userAssignmentsForDatasets;

    private int numOfDatasetsAssigned;
    private int numOfAssignments;
    private int numOfUniqueInstanceAssignments;
    private final Map<Dataset, Double> datasetCompletenessPercentages;
    private final Map<Dataset, Double> datasetConsistencyPercentages;
    private double avgTimeSpentInLabeling;
    private double stdDevOfTimeInLabeling;

    public UserStatistic() {
        userAssignmentsForDatasets = new HashMap<>();
        datasetCompletenessPercentages = new HashMap<>();
        datasetConsistencyPercentages = new HashMap<>();
    }

    public void calculateMetrics() {
        Set<Instance> uniqueInstances = new HashSet<>();
        List<Double> timesSpentForLabeling = new ArrayList<>();
        double totalTimeSpent = 0;

        for (Map.Entry<Dataset, List<Assignment>> entry : userAssignmentsForDatasets.entrySet()) {
            Dataset dataset = entry.getKey();
            List<Assignment> assignments = entry.getValue();
            Map<Instance, List<Label>> assignedLabelsForInstances = new HashMap<>();

            int numOfUniqueDatasetAssignments = 0;
            for (Assignment assignment: assignments) {
                Instance instance = assignment.getInstance();

                // To calculate unique instance assignments
                if (!uniqueInstances.contains(instance)) {
                    uniqueInstances.add(instance);
                    numOfUniqueDatasetAssignments++;
                }

                // To calculate dataset consistency percentage
                assignedLabelsForInstances.put(instance, assignment.getLabels());

                // To calculate average & standard deviation of time spent in labeling
                timesSpentForLabeling.add(assignment.getTimeSpent());
                totalTimeSpent += assignment.getTimeSpent();
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

        for (double timeSpent: timesSpentForLabeling) {
            stdDevOfTimeInLabeling += Math.pow(timeSpent - avgTimeSpentInLabeling, 2);
        }
        stdDevOfTimeInLabeling = Math.sqrt(stdDevOfTimeInLabeling / timesSpentForLabeling.size());
    }

    private double calculateConsistencyPercentage(Map<Instance, List<Label>> assignedLabelsForInstances) {
        double sumOfConsistencies = 0;

        for (List<Label> labels : assignedLabelsForInstances.values()) {
            Map<Label, Integer> labelOccurrences = new HashMap<>();
            for (Label label : labels) {
                Integer val = labelOccurrences.get(label);
                labelOccurrences.put(label, val == null ? 1 : val + 1);
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
        if (assignment == null) {
            assignments = new ArrayList<>();
            numOfDatasetsAssigned++;
        }
        assignments.add(assignment);
        numOfAssignments++;
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
