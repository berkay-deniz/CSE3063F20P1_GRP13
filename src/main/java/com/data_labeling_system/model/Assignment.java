package com.data_labeling_system.model;

import java.util.Date;
import java.util.List;

public class Assignment {
    private Instance instance;
    private List<Label> labels;
    private User user;
    private Date dateTime;
    
    public Assignment(Instance instance, List<Label> labels, User user, Date dateTime) {
    	this.dateTime =dateTime;
    	this.instance =instance;
    	this.labels = labels;
    	this.user = user;
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

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
