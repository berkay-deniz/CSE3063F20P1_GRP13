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
}
