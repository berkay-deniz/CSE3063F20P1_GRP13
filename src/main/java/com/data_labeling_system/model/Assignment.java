package com.data_labeling_system.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.Expose;

import io.gsonfire.gson.ExclusionByValueStrategy;
@JsonIgnoreProperties({"instance", "labels","user"})
@JsonPropertyOrder({ "instance id", "class label ids", "user id", "dateTime"})
public class Assignment {
	
    private Instance instance;
	
    private List<Label> labels;
	
    private User user;
    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    private Date dateTime;
    
    public Assignment(Instance instance, List<Label> labels, User user, Date dateTime) {
    	this.dateTime =dateTime;
    	this.instance =instance;
    	this.labels = labels;
    	this.user = user;
    }
    
    @JsonGetter("user id")
    public int getUserId () {
    	return user.getId();
    	
    }
    @JsonGetter("instance id")
    public  int getInstanceId() {
    	
    	return instance.getId();
    }
    @JsonGetter("class label ids")
    public ArrayList<Integer> getLabelIds() {
    	
    	ArrayList<Integer> temp = new ArrayList<>();
    	
    	for(int i = 0 ; i<this.labels.size();i++) {
    		
    		temp.add(this.labels.get(i).getId());
    	}
    	
    	return temp ;
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
