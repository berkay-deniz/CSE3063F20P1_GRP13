package com.data_labeling_system.mechanism;

import org.apache.log4j.Logger;

public class LabelingMechanismFactory {
    private final Logger logger;

    public LabelingMechanismFactory() {
        logger = Logger.getLogger(LabelingMechanismFactory.class);
    }

    public LabelingMechanism makeLabelingMechanism(String type) {
        if (type.equals("RandomBot")) {
            logger.info("Random labeling mechanism generated successfully.");
            return new RandomLabelingMechanism();
        } else {
            logger.error("'" + type + "' is not a valid mechanism type.");
            return null;
        }
    }
}
