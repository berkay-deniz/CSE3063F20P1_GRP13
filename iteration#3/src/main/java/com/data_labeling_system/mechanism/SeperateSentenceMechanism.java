package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.util.*;


public class SeperateSentenceMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        String[] sentences = instance.divideIntoSentences();
        int maxLabelFrequency=0;
        ArrayList<Label> maxLabels=new ArrayList<>();
        HashMap<Label, Integer> labelFrequency = new HashMap<>();
        for (String sentence : sentences) {
            System.out.print("Sentence to be labeled: ");
            System.out.println(sentence);

            // Show labels to the user
            System.out.println("Labels that you can assign are: ");

            showLabelsToUser(labels);

            System.out.println("You can assign at most " + maxNumOfLabels + " labels to this sentence");
            System.out.println("If you want to assign more than 1 label to this sentence put space between two labels");
            Scanner scan = new Scanner(System.in);
            String allLabels = scan.nextLine();

            String[] labelIndexes = allLabels.split(" ");
            int numOfAssignedLabels = labelIndexes.length;
            while (numOfAssignedLabels > maxNumOfLabels) {
                System.out.println("You entered too much labels! Enter again: ");
                allLabels = scan.nextLine();
                labelIndexes = allLabels.split(" ");
                numOfAssignedLabels = labelIndexes.length;
            }
            for (String labelIndexStr : labelIndexes) {
                int labelIndex = Integer.parseInt(labelIndexStr);
                Label currentLabel = labels.get(labelIndex);
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
        int counter=0;
        for(Label label:maxLabels){
            if(counter>=maxNumOfLabels){
                break;
            }
            counter++;
            assignedLabels.add(label);
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
