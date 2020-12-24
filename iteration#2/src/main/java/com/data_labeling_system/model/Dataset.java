package com.data_labeling_system.model;

import com.data_labeling_system.statistic.DatasetStatistic;
import com.data_labeling_system.util.DataLabelingSystem;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public int getId() {
        return id;
    }

    public int getMaxNumOfLabels() {
        return maxNumOfLabels;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public List<User> getUsers() {
        return users;
    }

    public HashMap<User, Integer> getNextInstancesToBeLabelled() {
        return nextInstancesToBeLabelled;
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
