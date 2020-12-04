package com.data_labeling_system.mechanism;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.data_labeling_system.model.*;


public class RandomLabelingMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) {
        ArrayList<Label> AssignedLabelsList = new ArrayList<Label>();

        Random random = new Random();
        int numOfLabels = random.nextInt(maxNumOfLabels) + 1;

        for (int i = 0; i < numOfLabels; i++) {

            int max = labels.size();
            int randomNumber = random.nextInt(max) + 1;

            AssignedLabelsList.add(labels.get(randomNumber));
            labels.remove(randomNumber);
        }
        return new Assignment(instance, AssignedLabelsList, user, new Date());
    }


}
