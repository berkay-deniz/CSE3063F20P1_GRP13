package com.data_labeling_system.mechanism;

import org.apache.log4j.Logger;

// This class is used to generate new mechanisms
public class LabelingMechanismFactory {
    private final Logger logger;

    public LabelingMechanismFactory() {
        logger = Logger.getLogger(LabelingMechanismFactory.class);
    }

    public LabelingMechanism makeLabelingMechanism(String type) {
        // Create Mechanism according to received information from user type
    	if (type.equals("RandomBot")) {
            logger.info("Random labeling mechanism generated successfully.");
            // Crete Random Labeling Mechanism
            return new RandomLabelingMechanism();
        } else {
        	// If the user type is unregistered, mechanism don't create
            logger.error("'" + type + "' is not a valid mechanism type.");
            return null;
        }
    }
}
