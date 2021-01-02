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
            } else if (frequency > 0 && frequency == max) {
                mostFrequentLabels.add(label);
            }
        }
        List<Label> assignedLabels = new ArrayList<>();
        for (int i = 0; i < maxNumOfLabels; i++) {
            if (i >= mostFrequentLabels.size()) {
                break;
            }
            assignedLabels.add(mostFrequentLabels.get(i));
        }
        if (assignedLabels.isEmpty()) {
            assignedLabels = assignRandomly(labels, maxNumOfLabels);
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
