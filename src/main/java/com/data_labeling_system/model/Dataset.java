package com.data_labeling_system.model;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import java.util.ArrayList;
import java.util.List;
@JsonPropertyOrder({ "dataset id", "dataset name","maximum number of labels per instance", "class labels", "instances",
	"class label assignments","users" })
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
	
    private List<User> users;

    public Dataset(String json) {
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
