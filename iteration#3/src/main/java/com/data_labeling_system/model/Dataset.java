package com.data_labeling_system.model;

import com.data_labeling_system.statistic.DatasetStatistic;
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
    private final Map<Integer, Label> labels;

    @JsonProperty("instances")
    private final Map<Integer, Instance> instances;

    @JsonProperty("class label assignments")
    private final List<Assignment> assignments;

    private final Map<Integer, User> users;

    @JsonProperty("next instances to be labelled")
    private final HashMap<User, Integer> nextInstancesToBeLabelled;
    private final DatasetStatistic statistic;
    private final Logger logger;

    public Dataset(String json, Map<Integer, User> users) {
        statistic = new DatasetStatistic();
        logger = Logger.getLogger(Dataset.class);
        nextInstancesToBeLabelled = new HashMap<>();
        assignments = new ArrayList<>();
        labels = new LinkedHashMap<>();
        instances = new LinkedHashMap<>();
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

        JSONArray labelArray = object.getJSONArray("class labels");
        for (int i = 0; i < labelArray.length(); i++) {
            JSONObject labelObject = labelArray.getJSONObject(i);
            int labelId = labelObject.getInt("label id");
            labels.put(labelId, new Label(labelObject.toString()));
        }

        JSONArray instanceArray = object.getJSONArray("instances");
        for (int i = 0; i < instanceArray.length(); i++) {
            JSONObject instanceObject = instanceArray.getJSONObject(i);
            int instanceId = instanceObject.getInt("id");
            instances.put(instanceId, new Instance(instanceObject.toString()));
        }

        if (object.has("class label assignments")) {
            JSONArray assignmentArray = object.getJSONArray("class label assignments");
            for (int i = 0; i < assignmentArray.length(); i++) {
                JSONObject assignmentObject = assignmentArray.getJSONObject(i);
                Instance instance = instances.get(assignmentObject.getInt("instance id"));
                User user = users.get(assignmentObject.getInt("user id"));
                String dateString = assignmentObject.getString("dateTime");
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }

                JSONArray labelIds = assignmentObject.getJSONArray("class label ids");
                List<Label> assignedLabels = new ArrayList<>();
                for (int j = 0; j < labelIds.length(); j++) {
                    assignedLabels.add(labels.get(labelIds.getInt(j)));
                }

                Assignment assignment = new Assignment(instance, assignedLabels, user, date);
                assignments.add(assignment);
                // Store assignment in UserStatistic for future metric calculations
                user.getStatistic().addAssignment(this, assignment);
            }
        }

        if (object.has("next instances to be labelled")) {
            try {
                Map<String, Integer> nextInstancesJson = new ObjectMapper().readValue(object.getJSONObject("next " +
                        "instances to be labelled").toString(), HashMap.class);
                for (Map.Entry<String, Integer> entry : nextInstancesJson.entrySet()) {
                    int userId = Integer.parseInt(entry.getKey());
                    int nextInstance = entry.getValue();
                    this.nextInstancesToBeLabelled.put(users.get(userId), nextInstance);
                }
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage(), e);

            }
        }
    }

    public void assignLabels(User loggedUser) {
        //  Using the labeling mechanism the user has; assign user, instance and labels values into assignments
        List<User> activeUsers = new ArrayList<>(users.values());
        while (!activeUsers.isEmpty()) {
            for (Iterator<User> it = activeUsers.iterator(); it.hasNext(); ) {
                User currentUser = it.next();
                if (loggedUser != null && currentUser instanceof BotUser) {
                    it.remove();
                    continue;
                }
                if (loggedUser == null && currentUser instanceof HumanUser) {
                    it.remove();
                    continue;
                }
                long startTime = System.nanoTime();
                Integer value = nextInstancesToBeLabelled.get(currentUser);
                int nextInstanceToBeLabelled = value == null ? 1 : value;

                // If the user has completed all the labellings in current dataset
                if (instances.size() < nextInstanceToBeLabelled) {
                    it.remove();
                    continue;
                }

                int currentInstanceToBeLabelled = currentUser.chooseInstanceToBeLabelled(nextInstanceToBeLabelled);

                Assignment assignment = currentUser.assign(instances.get(currentInstanceToBeLabelled), labels,
                        maxNumOfLabels);
                assignments.add(assignment);

                logAssignmentInfo(assignment, currentUser);

                if (currentInstanceToBeLabelled == nextInstanceToBeLabelled)
                    nextInstancesToBeLabelled.put(currentUser, ++nextInstanceToBeLabelled);

                // Determine time spent in assignment
                long finishTime = System.nanoTime();
                assignment.setTimeSpentInNanos(finishTime - startTime);

                // Calculate updated metrics
                currentUser.getStatistic().addAssignment(this, assignment);
                currentUser.getStatistic().calculateMetrics();
                logger.info("Metrics are calculated for User with UserId: " + currentUser.getId());
                calculateMetrics();
                logger.info("Metrics are calculated for dataset with DatasetId: " + id);
                // Print output dataset and metric calculations
                printOutputAndMetrics();
            }
        }
    }

    private void logAssignmentInfo(Assignment assignment, User user) {
        List<Label> labels = assignment.getLabels();
        StringBuilder classLabels = new StringBuilder();
        for (int j = 0; j < labels.size(); j++) {
            Label label = labels.get(j);
            String classLabel = label.getId() + ": " + label.getText();
            classLabels.append(classLabel);
            if (j < labels.size() - 1)
                classLabels.append(", ");
        }

        user.logAssignmentInfo(assignment, classLabels.toString());
    }

    private void printOutputAndMetrics() {
        statistic.printMetrics("metrics/datasets/dataset" + id + ".json");
        for (User user : users.values()) {
            user.printMetrics();
        }
        for (Instance instance : instances.values()) {
            instance.printMetrics(id);
        }
        logger.info("Statistic metrics printed to the 'metrics' folder successfully.");
        printFinalDataset("outputs/output" + id + ".json");
        logger.info("Final dataset with DatasetId: " + id + " is printed to output.json.");
    }

    private void printFinalDataset(String outputFileName) {
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

    public void calculateMetrics() {
        statistic.calculateMetrics(this, instances, assignments, users);
    }

    public int getId() {
        return id;
    }

    public Map<Integer, Instance> getInstances() {
        return instances;
    }

    @JsonGetter("next instances to be labelled")
    public HashMap<Integer, Integer> serializeNextInstanceIndexes() {
        HashMap<Integer, Integer> nextInstanceIndexes = new HashMap<>();
        for (Map.Entry<User, Integer> entry : nextInstancesToBeLabelled.entrySet()) {
            int userId = entry.getKey().getId();
            int nextInstances = entry.getValue();
            nextInstanceIndexes.put(userId, nextInstances);
        }
        return nextInstanceIndexes;
    }

    @JsonGetter("class labels")
    public Collection<Label> serializeLabels() {
        return labels.values();
    }

    @JsonGetter("instances")
    public Collection<Instance> serializeInstances() {
        return instances.values();
    }

    @JsonGetter("users")
    public Collection<User> serializeUsers() {
        return users.values();
    }
}
