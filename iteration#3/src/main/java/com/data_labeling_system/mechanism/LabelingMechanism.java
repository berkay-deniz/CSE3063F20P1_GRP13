package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// All mechanisms must implement the methods of this class
public abstract class LabelingMechanism {
    protected Logger logger;

    public LabelingMechanism() {
        logger = Logger.getLogger(LabelingMechanism.class);
    }

    // For the Assignment method; user, instance, labels and instance can have the maximum number of labels are sent
    public abstract Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels);

    private void showLabelsToUser(Map<Integer, Label> labels) {
        System.out.print("\nLabels that you can assign are: ");
        for (Map.Entry<Integer, Label> entry : labels.entrySet()) {
            int id = entry.getKey();
            Label label = entry.getValue();
            System.out.print("[" + id + "] ");
            label.printLabel();
            System.out.print(", ");
        }
        System.out.println("\n");
    }

    protected List<Label> assignRandomly(Map<Integer, Label> labels, int maxNumOfLabels) {
        List<Label> assignedLabels = new ArrayList<>();
        List<Label> tempLabels = new ArrayList<>(labels.values());
        // The number of labels to be assigned is determined randomly
        int numOfLabels = (int) (Math.random() * (maxNumOfLabels) + 1);
        for (int i = 0; i < numOfLabels; i++) {
            // Finish choosing if labels are over
            if (tempLabels.isEmpty())
                break;
            // Choose a random Label
            int randomNumber = (int) (Math.random() * (tempLabels.size()));
            // Add label that used from mechanism into ArrayList
            assignedLabels.add(tempLabels.get(randomNumber));
            // Remove the label from the list to not use it again
            tempLabels.remove(randomNumber);
        }
        return assignedLabels;
    }

    protected String[] readLabelsFromUser(Map<Integer, Label> labels, int maxNumOfLabels) {
        // Show labels to the user
        showLabelsToUser(labels);

        System.out.print("You can assign at most " + maxNumOfLabels + " labels to this instance." +
                " If you want to assign more than 1 label to this instance put space between two labels." +
                "\nEnter label IDs: ");

        Scanner scan = new Scanner(System.in);
        String allLabels = scan.nextLine();

        String[] tokens = allLabels.split(" ");

        int numOfAssignedLabels = tokens.length;
        while (numOfAssignedLabels > maxNumOfLabels) {
            System.out.print("You entered too much labels!\nEnter label IDs: ");
            allLabels = scan.nextLine();
            tokens = allLabels.split(" ");
            numOfAssignedLabels = tokens.length;
        }

        return tokens;
    }

    protected Label findValidLabelFromInput(String labelIndexStr, Map<Integer, Label> labels) {
        int labelIndex;
        try {
            labelIndex = Integer.parseInt(labelIndexStr);
        } catch (NumberFormatException e) {
            logger.error("You have entered an invalid label id.");
            return null;
        }
        Label currentLabel = labels.get(labelIndex);
        if (currentLabel == null) {
            logger.error("You have entered an invalid label id.");
            return null;
        }

        return currentLabel;
    }
}
