package com.data_labeling_system.mechanism;

public class LabelingMechanismFactory {
    public LabelingMechanism makeLabelingMechanism(String type) {
        if (type.equals("RandomLabelingMechanism")) {
            return new RandomLabelingMechanism();
        } else
            return null;
    }
}
