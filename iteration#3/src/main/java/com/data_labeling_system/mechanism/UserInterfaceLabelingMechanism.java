package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class UserInterfaceLabelingMechanism extends LabelingMechanism{

    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) {
        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();

        // Inform user about instance to be labeled
        System.out.print("Instance to be labeled: ");
        instance.printInstance();

        // Show labels to the user
        System.out.println("Labels that you can assign are: ");

        for (int i = 0; i < labels.size(); i++){
            labels.get(i).printLabel();
            System.out.println(" Type " + i + "to assign this label" );
        }

        System.out.println("You can assign at most " + maxNumOfLabels + " labels to this instance");
        System.out.println("If you want to assign more than 1 label to this instance put space between two labels");

        Scanner scan = new Scanner(System.in);
        String allLabels = scan.nextLine();

        String[] tokens = allLabels.split(" ");

        int numOfAssignedLabels = tokens.length;
        while (numOfAssignedLabels > maxNumOfLabels){
            System.out.println("You entered too much labels! Enter again: ");
            allLabels = scan.nextLine();
            tokens = allLabels.split(" ");
            numOfAssignedLabels = tokens.length;
        }

        for (String token : tokens){
            int labelId =  Integer.parseInt(token);
            assignedLabels.add(labels.get(labelId));
        }

        return new Assignment(instance, assignedLabels, user, new Date());

    }
}
