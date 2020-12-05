package com.data_labeling_system.mechanism;

import org.apache.log4j.Logger;

public class LabelingMechanismFactory { // Generates new Mechanism
    private final Logger logger;

    public LabelingMechanismFactory() {
        logger = Logger.getLogger(LabelingMechanismFactory.class);
    }

    public LabelingMechanism makeLabelingMechanism(String type) {
        // Create Mechanism according to received information from user type
    	if (type.equals("RandomBot")) {
            logger.info("Random labeling mechanism generated successfully.");
            return new RandomLabelingMechanism();// Crete Random Labeling Mechanism
        } else {
        	// If the user type is unregistered, mechanism don't create
            logger.error("'" + type + "' is not a valid mechanism type.");
            return null;
        }
    }
}
