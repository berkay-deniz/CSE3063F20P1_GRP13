package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.List;

// All mechanisms must use the function of this class.
public abstract class LabelingMechanism {
	//For the Assignment function, user, instance, labels and instance can have the maximum number of labels  are sent.
    public abstract Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels);
}
