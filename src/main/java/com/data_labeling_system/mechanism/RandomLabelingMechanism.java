package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RandomLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) {
        ArrayList<Label> assignedLabels = new ArrayList<Label>();
        ArrayList<Label> tempLabels = new ArrayList<Label>(labels);
        int numOfLabels = (int) (Math.random() * (maxNumOfLabels) + 1);
        for (int i = 0; i < numOfLabels; i++) {
            if (tempLabels.isEmpty())
                break;
            int randomNumber = (int) (Math.random() * (tempLabels.size()));
            assignedLabels.add(tempLabels.get(randomNumber));
            tempLabels.remove(randomNumber);
        }
        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
