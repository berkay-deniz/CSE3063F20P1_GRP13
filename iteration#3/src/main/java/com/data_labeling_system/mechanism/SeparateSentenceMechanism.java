package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SeparateSentenceMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        String[] sentences = instance.divideIntoSentences();
        int maxLabelFrequency = 0;
        ArrayList<Label> maxLabels = new ArrayList<>();
        HashMap<Label, Integer> labelFrequency = new HashMap<>();
        for (String sentence : sentences) {
            System.out.print("Sentence to be labeled: ");
            System.out.println(sentence);

            String[] tokens = getLabelsFromUser(labels, maxNumOfLabels);
            for (String token : tokens) {
                Label currentLabel = getValidLabelFromInput(token, labels);
                if (currentLabel == null) break;

                if (!labelFrequency.containsKey(currentLabel)) {
                    labelFrequency.put(currentLabel, 1);
                    if (maxLabelFrequency == 0) {
                        maxLabelFrequency = 1;
                        maxLabels.add(currentLabel);
                    }
                } else {
                    int currentFrequency = labelFrequency.get(currentLabel);
                    currentFrequency++;
                    labelFrequency.put(currentLabel, currentFrequency);
                    if (currentFrequency > maxLabelFrequency) {
                        maxLabelFrequency = currentFrequency;
                        maxLabels.clear();
                        maxLabels.add(currentLabel);
                    } else if (currentFrequency == maxLabelFrequency) {
                        maxLabels.add(currentLabel);
                    }
                }
            }

        }
        int counter = 0;
        for (Label label : maxLabels) {
            if (counter >= maxNumOfLabels) {
                break;
            }
            counter++;
            assignedLabels.add(label);
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
