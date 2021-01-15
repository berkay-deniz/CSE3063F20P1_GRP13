package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

// This class randomly chooses the labels to be assigned to instances
public class RandomLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        List<Label> assignedLabels = assignRandomly(labels, maxNumOfLabels);
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
