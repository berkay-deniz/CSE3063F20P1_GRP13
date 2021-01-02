package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// This class randomly chooses the labels to be assigned to instances
public class RandomLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        // Create Arraylist to keep copying of labels
        ArrayList<Label> tempLabels = new ArrayList<>(labels);
        // The number of labels to be assigned is determined randomly
        int numOfLabels = (int) (Math.random() * (maxNumOfLabels) + 1);
        for (int i = 0; i < numOfLabels; i++) {
            // Finish choosing if labels are over
            if (tempLabels.isEmpty())
                break;
            // Choose a random Label
            int randomNumber = (int) (Math.random() * (tempLabels.size()));
            // Add label that used from mechanism into Arraylist  
            assignedLabels.add(tempLabels.get(randomNumber));
            // Remove the label from the list to not use it again
            tempLabels.remove(randomNumber);
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
