package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.*;

import java.util.List;

public abstract class LabelingMechanism {
    public abstract Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels);
}
