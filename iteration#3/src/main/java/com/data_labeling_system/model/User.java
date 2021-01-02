package com.data_labeling_system.model;

import com.data_labeling_system.DataLabelingSystem;
import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.mechanism.LabelingMechanismFactory;
import com.data_labeling_system.statistic.UserStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Map;


@JsonIgnoreProperties({"mechanism", "statistic"})
@JsonPropertyOrder({"user id", "user name", "user type"})
public class User implements Parsable {
    @JsonProperty("user id")
    private int id;
    @JsonProperty("user name")
    private String name;
    @JsonProperty("user type")
    private String type;
    private final UserStatistic statistic;

    private double consistencyCheckProbability;

    private final LabelingMechanism mechanism;
    private final Logger logger;

    public User(String json) {
        statistic = new UserStatistic();
        LabelingMechanismFactory labelingMechanismFactory = new LabelingMechanismFactory();
        this.parse(json);
        this.mechanism = labelingMechanismFactory.makeLabelingMechanism(this.type);
        logger = Logger.getLogger(DataLabelingSystem.class);
        logger.info("Created '" + name + "' as '" + type + "'.");
    }

    @Override
    public void parse(String json) {
        // Parse the User json using org.json library
        JSONObject object = new JSONObject(json);
        this.id = object.getInt("user id");
        this.name = object.getString("user name");
        this.type = object.getString("user type");
        this.consistencyCheckProbability = object.getDouble("consistencyCheckProbability");
    }

    public int getId() {
        return id;
    }

    public UserStatistic getStatistic() {
        return statistic;
    }

    public void printMetrics() {
        statistic.printMetrics("metrics/users/user" + id + ".json");
    }

    public void logAssignmentInfo(Assignment assignment, String classLabels) {
        assignment.logAssignmentInfo(id, name, classLabels);
    }

    public Assignment assign(Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {
        return mechanism.assign(this, instance, labels, maxNumOfLabels);
    }

    public int chooseInstanceToBeLabelled(int nextInstanceToBeLabelled) {
        int randomNumber = (int) ((Math.random() * 100) + 1);
        return ((randomNumber <= consistencyCheckProbability * 100)) ?
                (int) (Math.random() * (nextInstanceToBeLabelled - 1) + 1) : nextInstanceToBeLabelled;
    }
}
