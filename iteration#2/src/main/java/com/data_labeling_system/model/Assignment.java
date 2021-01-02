package com.data_labeling_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties({"instance", "labels", "user", "timeSpentInMillis"})
@JsonPropertyOrder({"instance id", "class label ids", "user id", "dateTime"})
public class Assignment {
    private Instance instance;
    private final List<Label> labels;
    private User user;
    private long timeSpentInNanos;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    private Date dateTime;

    public Assignment(Instance instance, List<Label> labels, User user, Date dateTime) {
        this.dateTime = dateTime;
        this.instance = instance;
        this.labels = labels;
        this.user = user;
    }

    @JsonGetter("user id")
    public int getUserId() {
        return user.getId();
    }

    @JsonGetter("instance id")
    public int getInstanceId() {
        return instance.getId();
    }

    @JsonGetter("class label ids")
    public ArrayList<Integer> getLabelIds() {
        // Retrieve ID's of the labels from the List
        ArrayList<Integer> temp = new ArrayList<>();

        for (Label label : this.labels) {
            temp.add(label.getId());
        }

        return temp;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTimeSpentInNanos() {
        return timeSpentInNanos;
    }

    public void setTimeSpentInNanos(long timeSpentInNanos) {
        this.timeSpentInNanos = timeSpentInNanos;
    }

}
