package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;
import org.apache.log4j.Logger;

import java.util.Map;

// All mechanisms must implement the methods of this class
public abstract class LabelingMechanism {
    protected Logger logger;

    public LabelingMechanism() {
        logger = Logger.getLogger(LabelingMechanism.class);
    }

    // For the Assignment method; user, instance, labels and instance can have the maximum number of labels are sent
    public abstract Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels);

    public void showLabelsToUser(Map<Integer, Label> labels) {
        for (Map.Entry<Integer, Label> entry : labels.entrySet()) {
            int id = entry.getKey();
            Label label = entry.getValue();
            label.printLabel();
            System.out.println(" Type " + id + "to assign this label");
        }
    }
}
