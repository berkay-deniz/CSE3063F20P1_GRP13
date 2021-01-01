package com.data_labeling_system.model;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.statistic.DatasetStatistic;
import com.data_labeling_system.util.DataLabelingSystem;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@JsonPropertyOrder({"dataset id", "dataset name", "maximum number of labels per instance", "class labels", "instances",
        "class label assignments", "users"})
@JsonIgnoreProperties({"statistic"})
public class Dataset implements Parsable {
    @JsonProperty("dataset id")
    private int id;

    @JsonProperty("dataset name")
    private String name;

    @JsonProperty("maximum number of labels per instance")
    private int maxNumOfLabels;

    @JsonProperty("class labels")
    private List<Label> labels;

    private List<Instance> instances;

    @JsonProperty("class label assignments")
    private List<Assignment> assignments;

    private final List<User> users;

    @JsonProperty("next instances to be labelled")
    private final HashMap<User, Integer> nextInstancesToBeLabelled;
    private final DatasetStatistic statistic;
    private final Logger logger;

    public Dataset(String json, List<User> users) {
        statistic = new DatasetStatistic(this);
        logger = Logger.getLogger(DataLabelingSystem.class);
        nextInstancesToBeLabelled = new HashMap<>();
        assignments = new ArrayList<>();
        this.users = users;
        parse(json);
    }

    @Override
    public void parse(String json) {
        // Parse the Dataset json using org.json library and create Lists
        JSONObject object = new JSONObject(json);
        id = object.getInt("dataset id");
        name = object.getString("dataset name");
        maxNumOfLabels = object.getInt("maximum number of labels per instance");

        JSONArray labelsJSON = object.getJSONArray("class labels");
        labels = new ArrayList<>();
        for (int i = 0; i < labelsJSON.length(); i++) {
            labels.add(new Label(labelsJSON.getJSONObject(i).toString()));
        }

        JSONArray instancesJSON = object.getJSONArray("instances");
        instances = new ArrayList<>();
        for (int i = 0; i < instancesJSON.length(); i++) {
            instances.add(new Instance(instancesJSON.getJSONObject(i).toString()));
        }

        if (object.has("class label assignments")) {
            JSONArray assignmentsJSON = object.getJSONArray("class label assignments");
            assignments = new ArrayList<>();
            for (int i = 0; i < assignmentsJSON.length(); i++) {
                JSONObject assignmentJSON = assignmentsJSON.getJSONObject(i);
                Instance instance = (Instance) findParsable(assignmentJSON.getInt("instance id"), this.instances);
                User user = (User) findParsable(assignmentJSON.getInt("user id"), this.users);
                String dateString = assignmentJSON.getString("dateTime");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }

                JSONArray labelIds = assignmentJSON.getJSONArray("class label ids");
                List<Label> assignedLabels = new ArrayList<>();
                for (int j = 0; j < labelIds.length(); j++) {
                    assignedLabels.add((Label) findParsable(labelIds.getInt(j), this.labels));
                }

                Assignment assignment = new Assignment(instance, assignedLabels, user, date);
                assignments.add(assignment);
                // Store assignment in UserStatistic for future metric calculations
                user.getStatistic().addAssignment(this, assignment);
            }
        }

        if (object.has("next instances to be labelled")) {
            try {
                HashMap<String, Integer> nextInstancesJson = new ObjectMapper().readValue(object.getJSONObject("next instances to be labelled").toString(), HashMap.class);
                for (Map.Entry<String, Integer> entry : nextInstancesJson.entrySet()) {
                    int userId = Integer.parseInt(entry.getKey());
                    int nextInstance = entry.getValue();
                    this.nextInstancesToBeLabelled.put((User) findParsable(userId, this.users), nextInstance);
                }
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage(), e);

            }
        }
    }

    public void assignLabels() throws IOException {
        logger.info("The list of assigment was created successfully.");

        //  Using the labeling mechanism the user has; assign user, instance and labels values into assignments
        while (!this.users.isEmpty()) {
            for (int i = 0; i < this.users.size(); i++) {
                long startTime = System.nanoTime();
                User currentUser = this.users.get(i);
                Integer value = nextInstancesToBeLabelled.get(currentUser);
                int nextInstanceToBeLabelled = value == null ? 0 : value;

                //If the user has completed all the labellings in current dataset
                if (instances.size() <= nextInstanceToBeLabelled) {
                    this.users.remove(i);
                    i--;
                    continue;
                }
                int randomNumber = (int) ((Math.random() * 100) + 1);
                int currentInstanceToBeLabelled =
                        ((randomNumber <= currentUser.getConsistencyCheckProbability() * 100)) ?
                                (int) (Math.random() * nextInstanceToBeLabelled) : nextInstanceToBeLabelled;

                LabelingMechanism labelingMechanism = currentUser.getMechanism();
                Assignment assignment = labelingMechanism.assign(currentUser, instances.get(currentInstanceToBeLabelled),
                        labels, maxNumOfLabels);
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
                    nextInstancesToBeLabelled.put(currentUser, ++nextInstanceToBeLabelled);

                // Determine time spent in assignment
                long finishTime = System.nanoTime();
                assignment.setTimeSpentInNanos(finishTime - startTime);

                // Calculate updated metrics
                currentUser.getStatistic().addAssignment(this, assignment);
                currentUser.getStatistic().calculateMetrics();
                logger.info("Metrics are calculated for User with UserId: " + currentUser.getId());
                statistic.calculateMetrics();
                logger.info("Metrics are calculated for dataset with DatasetId: " + id);

                // Print output dataset and metric calculations
                // TODO: Print metrics
                logger.info("Metrics are printed to metric folder.");
                printFinalDataset("outputs/output" + id + ".json");
                logger.info("Final dataset with DatasetId: " + id + " is printed to output.json.");

                try {
                    TimeUnit.SECONDS.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void printFinalDataset(String outputFileName) {
        // Write the final dataset as jsonfile
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(outputFileName), this);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Final dataset printed to '" + outputFileName + "' successfully.");
    }

    public int getId() {
        return id;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<User> getUsers() {
        return users;
    }

    @JsonGetter("next instances to be labelled")
    public HashMap<Integer, Integer> getNextInstanceIndexes() {
        HashMap<Integer, Integer> nextInstanceIndexes = new HashMap<>();
        for (Map.Entry<User, Integer> entry : nextInstancesToBeLabelled.entrySet()) {
            int userId = entry.getKey().getId();
            int nextInstances = entry.getValue();
            nextInstanceIndexes.put(userId, nextInstances);
        }
        return nextInstanceIndexes;
    }

    public DatasetStatistic getStatistic() {
        return statistic;
    }

    public Parsable findParsable(int id, List<? extends Parsable> list) {
        if (id <= list.size() && id > 0 && list.get(id - 1).getId() == id) {
            return list.get(id - 1);
        } else {
            for (Parsable parsable : list) {
                if (parsable.getId() == id)
                    return parsable;
            }
        }
        return null;
    }
}
