package com.data_labeling_system.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Dataset implements Parsable {
    private int id;
    private String name;
    private int maxNumOfLabels;
    private List<Label> labels;
    private List<Instance> instances;
    private List<Assignment> assignments;
    private List<User> users;

    public Dataset(String json) {
        parse(json);
    }

    @Override
    public void parse(String json) {
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
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxNumOfLabels() {
        return maxNumOfLabels;
    }

    public void setMaxNumOfLabels(int maxNumOfLabels) {
        this.maxNumOfLabels = maxNumOfLabels;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
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

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
