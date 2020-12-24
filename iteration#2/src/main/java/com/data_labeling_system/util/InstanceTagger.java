package com.data_labeling_system.util;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.model.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class InstanceTagger {
    private final Logger logger;

    private Dataset dataset;
    private List<User> users;

    public InstanceTagger() {
        logger = Logger.getLogger(Instance.class);
    }

    public void assignLabels(IOManager ioManager, UserManager userManager, List<Dataset> datasets) {
        List<Assignment> assignments = this.dataset.getAssignments();
        logger.info("The list of assigment was created successfully.");

        //  Using the labeling mechanism the user has; assign user, instance and labels values into assignments
        while (!this.users.isEmpty()) {
            for (int i = 0; i < this.users.size(); i++) {
                long startTime = System.nanoTime();
                User currentUser = this.users.get(i);
                Integer value = this.dataset.getNextInstancesToBeLabelled().get(currentUser);
                int nextInstanceToBeLabelled = value == null ? 0 : value;

                //If the user has completed all the labellings in current dataset
                if (this.dataset.getInstances().size() <= nextInstanceToBeLabelled) {
                    this.users.remove(i);
                    i--;
                    continue;
                }
                int randomNumber = (int) ((Math.random() * 100) + 1);
                int currentInstanceToBeLabelled =
                        ((randomNumber <= currentUser.getConsistencyCheckProbability() * 100)) ?
                                (int) (Math.random() * nextInstanceToBeLabelled) : nextInstanceToBeLabelled;

                LabelingMechanism labelingMechanism = currentUser.getMechanism();
                Assignment assignment = labelingMechanism.assign(currentUser, dataset.getInstances().get(currentInstanceToBeLabelled),
                        dataset.getLabels(), dataset.getMaxNumOfLabels());
                assignments.add(assignment);

                List<Label> labels = assignment.getLabels();
                StringBuilder classLabels = new StringBuilder();
                for (int j = 0; j < labels.size(); j++) {
                    Label label = labels.get(j);
                    String classLabel = label.getId() + ": " + label.getText();
                    classLabels.append(classLabel);
                    if (j < labels.size() - 1)
                        classLabels.append(", ");
                }

                logger.info("user id: " + currentUser.getId() + " " + currentUser.getName() + " tagged instance id: " +
                        assignment.getInstanceId() + " with class labels: [" + classLabels + "]" + ", instance: \"" +
                        assignment.getInstance().getInstance() + "\"");

                if (currentInstanceToBeLabelled == nextInstanceToBeLabelled)
                    this.dataset.getNextInstancesToBeLabelled().put(currentUser, ++nextInstanceToBeLabelled);

                // Determine time spent in assignment
                long finishTime = System.nanoTime();
                assignment.setTimeSpentInNanos(finishTime - startTime);

                // Calculate updated metrics
                currentUser.getStatistic().addAssignment(dataset, assignment);
                currentUser.getStatistic().calculateMetrics();
                logger.info("Metrics are calculated for User with UserId: " + currentUser.getId());
                dataset.getStatistic().calculateMetrics();
                logger.info("Metrics are calculated for dataset with DatasetId: " + dataset.getId());

                // Print output dataset and metric calculations
                ioManager.printMetrics(datasets, userManager.getUsers());
                logger.info("Metrics are printed to metric folder.");
                ioManager.printFinalDataset(dataset, "outputs/output" + dataset.getId() + ".json");
                logger.info("Final dataset with DatasetId: " + dataset.getId() + " is printed to output.json.");

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        this.dataset.setAssignments(assignments);
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
