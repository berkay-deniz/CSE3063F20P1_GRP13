package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.User;
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
            // Create Random Labeling Mechanism
            return new RandomLabelingMechanism();
        }
        if (type.equals("MachineLearningBot")) {
            logger.info("Machine learning mechanism generated successfully.");
            // Create Machine Labeling Mechanism
            return new MachineLearningMechanism();

        }
        if (type.equals("SimpleSearch")) {
            logger.info("Simple Search mechanism generated successfully.");
            // Create Simple Search Labeling Mechanism
            return new SimpleSearchLabelingMechanism();

        }
        if (type.equals("HumanUser")) {
            logger.info("UserInterfaceLabelingMechanism mechanism generated successfully.");
            // Create User Interface Labeling Mechanism
            return new UserInterfaceLabelingMechanism();

        }
        if (type.equals("PartialLabelingHumanUser")) {
            logger.info("Seperate sentence mechanism generated successfully.");
            // Create Seperate sentence mechanism
            return new SeperateSentenceMechanism();

        } else {
            // If the user type is unregistered, mechanism don't create
            logger.error("'" + type + "' is not a valid mechanism type.");
            return null;
        }
    }
}
