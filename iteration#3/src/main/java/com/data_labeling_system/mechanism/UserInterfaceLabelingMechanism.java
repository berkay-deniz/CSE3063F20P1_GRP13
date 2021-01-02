package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class UserInterfaceLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();

        // Inform user about instance to be labeled
        System.out.print("Instance to be labeled: ");
        instance.printInstance();

        String[] tokens = getLabelsFromUser(labels, maxNumOfLabels);

        for (String token : tokens) {
            Label currentLabel = getValidLabelFromInput(token, labels);
            if (currentLabel == null) break;

            assignedLabels.add(currentLabel);
        }

        return new Assignment(instance, assignedLabels, user, new Date());

    }
}
