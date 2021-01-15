package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.*;


public class SeparateSentenceMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        String[] sentences = instance.divideIntoSentences();
        int maxLabelFrequency = 0;
        List<Label> maxLabels = new ArrayList<>();
        Map<Label, Integer> labelFrequencies = new HashMap<>();
        for (String sentence : sentences) {
            System.out.println("---------------------------------------------------------------");
            System.out.println("Sentence to be labeled: " + sentence);
            createMaxLabels(labels, labelFrequencies, maxNumOfLabels, maxLabelFrequency, maxLabels);
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

    private void createMaxLabels(Map<Integer, Label> labels, Map<Label, Integer> labelFrequencies,
                                 int maxNumOfLabels, int maxLabelFrequency, List<Label> maxLabels) {
        String[] tokens = readLabelsFromUser(labels, maxNumOfLabels);
        for (String token : tokens) {
            Label currentLabel = findValidLabelFromInput(token, labels);
            if (currentLabel == null) {
                createMaxLabels(labels, labelFrequencies, maxNumOfLabels, maxLabelFrequency, maxLabels);
                return;
            }

            if (!labelFrequencies.containsKey(currentLabel)) {
                labelFrequencies.put(currentLabel, 1);
                if (maxLabelFrequency == 0) {
                    maxLabelFrequency = 1;
                    maxLabels.add(currentLabel);
                }
            } else {
                int currentFrequency = labelFrequencies.get(currentLabel);
                currentFrequency++;
                labelFrequencies.put(currentLabel, currentFrequency);
                if (currentFrequency > maxLabelFrequency) {
                    maxLabelFrequency = currentFrequency;
                    maxLabels.clear();
                    maxLabels.add(currentLabel);
                } else if (currentFrequency > 0 && currentFrequency == maxLabelFrequency) {
                    maxLabels.add(currentLabel);
                }
            }
        }
    }
}
