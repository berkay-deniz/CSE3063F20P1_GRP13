package com.data_labeling_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonIgnoreProperties({"instance", "labels", "user", "timeSpentInMillis"})
@JsonPropertyOrder({"instance id", "class label ids", "user id", "dateTime"})
public class Assignment {
    private final Instance instance;
    private final List<Label> labels;
    private final User user;
    private long timeSpentInNanos;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    private final Date dateTime;

    public Assignment(Instance instance, List<Label> labels, User user, Date dateTime) {
        this.dateTime = dateTime;
        this.instance = instance;
        this.labels = labels;
        this.user = user;
    }

    public void logAssignmentInfo(int userId, String userName, String classLabels) {
        instance.logAssignmentInfo(userId, userName, classLabels);
    }

    public void calculateDatasetStatistic(Set<Instance> uniqueInstances,
                                          Map<Instance, Set<Label>> uniqueLabelsForInstances,
                                          Map<Instance, Set<User>> uniqueUsersForInstances,
                                          Map<Label, Set<Instance>> uniqueInstancesForLabels) {
        uniqueInstances.add(instance);

        for (Label label : labels) {
            Set<Instance> instanceSet = uniqueInstancesForLabels.get(label);
            if (instanceSet == null) {
                instanceSet = new HashSet<>();
                uniqueInstancesForLabels.put(label, instanceSet);
            }
            instanceSet.add(instance);

            // Instance statistic calculations
            Set<Label> labelSet = uniqueLabelsForInstances.get(instance);
            if (labelSet == null) {
                labelSet = new HashSet<>();
                uniqueLabelsForInstances.put(instance, labelSet);
            }
            labelSet.add(label);

            Set<User> userSet = uniqueUsersForInstances.get(instance);
            if (userSet == null) {
                userSet = new HashSet<>();
                uniqueUsersForInstances.put(instance, userSet);
            }
            userSet.add(user);

            instance.getStatistic().addAssignedLabel(label);
        }
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
    public ArrayList<Integer> serializeLabelIds() {
        // Retrieve ID's of the labels from the List
        ArrayList<Integer> temp = new ArrayList<>();

        for (Label label : labels) {
            temp.add(label.getId());
        }

        return temp;
    }

    public Instance getInstance() {
        return instance;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public long getTimeSpentInNanos() {
        return timeSpentInNanos;
    }

    public void setTimeSpentInNanos(long timeSpentInNanos) {
        this.timeSpentInNanos = timeSpentInNanos;
    }
}
