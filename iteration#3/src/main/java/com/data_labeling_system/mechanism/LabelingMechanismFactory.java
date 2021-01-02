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
        switch (type) {
            case "RandomBot":
                logger.info("Random labeling mechanism generated successfully.");
                // Create Random Labeling Mechanism
                return new RandomLabelingMechanism();
            case "MachineLearningBot":
                logger.info("Machine learning mechanism generated successfully.");
                // Create Machine Labeling Mechanism
                return new MachineLearningMechanism();
            case "SimpleSearch":
                logger.info("Simple Search mechanism generated successfully.");
                // Create Simple Search Labeling Mechanism
                return new SimpleSearchLabelingMechanism();
            case "HumanUser":
                logger.info("UserInterfaceLabelingMechanism mechanism generated successfully.");
                // Create User Interface Labeling Mechanism
                return new UserInterfaceLabelingMechanism();
            case "SentenceLabelingHumanUser":
                logger.info("Separate sentence mechanism generated successfully.");
                // Create separate sentence mechanism
                return new SeparateSentenceMechanism();
            default:
                // If the user type is unregistered, mechanism don't create
                logger.error("'" + type + "' is not a valid mechanism type.");
                return null;
        }
    }
}
