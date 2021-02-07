package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserInterfaceLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Inform user about instance to be labeled
        System.out.println("---------------------------------------------------------------");
        System.out.print("Instance to be labeled: ");
        instance.printInstance();

        List<Label> assignedLabels = createAssignedLabels(labels, maxNumOfLabels);

        return new Assignment(instance, assignedLabels, user, new Date());
    }

    private List<Label> createAssignedLabels(Map<Integer, Label> labels, int maxNumOfLabels) {
        String[] tokens = readLabelsFromUser(labels, maxNumOfLabels);
        // Create Arraylist to keep labels assigned to instances
        List<Label> assignedLabels = new ArrayList<>();

        for (String token : tokens) {
            Label currentLabel = findValidLabelFromInput(token, labels);
            if (currentLabel == null) {
                return createAssignedLabels(labels, maxNumOfLabels);
            }

            assignedLabels.add(currentLabel);
        }

        return assignedLabels;
    }
}
