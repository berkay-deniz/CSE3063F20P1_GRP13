package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeperateSentenceMechanism extends LabelingMechanism {

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) throws IOException {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        // Create Arraylist to keep copying of labels
        ArrayList<Label> tempLabels = new ArrayList<>(labels);
        String str = instance.getInstance();
        String strInstance[] = str.replaceAll("\\.", " ").split("\\s+");

        for (int i = 0; i < strInstance.length; i++) {
            System.out.println(strInstance[i]);
            for (int j = 0; i < maxNumOfLabels; j++) {

            }
        }

        return null;

    }
}
