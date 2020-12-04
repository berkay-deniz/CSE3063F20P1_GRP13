package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.List;

public abstract class LabelingMechanism {
    public abstract Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels);
}
