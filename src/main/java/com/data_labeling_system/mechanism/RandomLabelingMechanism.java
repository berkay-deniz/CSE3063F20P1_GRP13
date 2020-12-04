package com.data_labeling_system.mechanism;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.data_labeling_system.model.*;


public class RandomLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) {
        ArrayList<Label> AssignedLabelsList = new ArrayList<Label>();
        ArrayList<Label> tempLabels = new ArrayList<Label>(labels);
        int numOfLabels = (int) (Math.random() * (maxNumOfLabels) + 1);
        for (int i = 0; i < numOfLabels; i++) {
            if (tempLabels.isEmpty())
                break;
            int randomNumber = (int) (Math.random() * (tempLabels.size()));
            AssignedLabelsList.add(tempLabels.get(randomNumber));
            tempLabels.remove(randomNumber);
        }
        return new Assignment(instance, AssignedLabelsList, user, new Date());
    }


}
