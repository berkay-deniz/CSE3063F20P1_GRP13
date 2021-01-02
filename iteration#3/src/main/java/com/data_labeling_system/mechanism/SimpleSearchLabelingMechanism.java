package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SimpleSearchLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        String[] words = instance.divideIntoWords();
        ArrayList<Label> mostFrequentLabels = new ArrayList<>();
        int max = 0;
        for (Label label : labels.values()) {
            int frequency = label.searchLabelInInstance(words);
            if (frequency > max) {
                max = frequency;
                mostFrequentLabels.clear();
                mostFrequentLabels.add(label);
            } else if (frequency == max) {
                mostFrequentLabels.add(label);
            }
        }
        ArrayList<Label> assignedLabels = new ArrayList<>();
        for (int i = 0; i < maxNumOfLabels; i++) {
            if (i >= mostFrequentLabels.size()) {
                break;
            }
            assignedLabels.add(mostFrequentLabels.get(i));
        }
        if (assignedLabels.size() == 0) {
            ArrayList<Label> tempLabels = new ArrayList<>(labels.values());
            // The number of labels to be assigned is determined randomly
            int numOfLabels = (int) (Math.random() * (maxNumOfLabels) + 1);
            for (int i = 0; i < numOfLabels; i++) {
                // Finish choosing if labels are over
                if (tempLabels.isEmpty())
                    break;
                // Choose a random Label
                int randomNumber = (int) (Math.random() * (tempLabels.size()));
                // Add label that used from mechanism into ArrayList
                assignedLabels.add(tempLabels.get(randomNumber));
                // Remove the label from the list to not use it again
                tempLabels.remove(randomNumber);
            }
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
