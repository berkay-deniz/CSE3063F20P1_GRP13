package com.data_labeling_system.model;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.mechanism.LabelingMechanismFactory;
import com.data_labeling_system.statistic.UserStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.json.JSONObject;


@JsonIgnoreProperties({"mechanism","statistic"})
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

    public User(String json) {
        statistic = new UserStatistic();
        LabelingMechanismFactory labelingMechanismFactory = new LabelingMechanismFactory();
        this.parse(json);
        this.mechanism = labelingMechanismFactory.makeLabelingMechanism(this.type);
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

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public LabelingMechanism getMechanism() {
        return mechanism;
    }

    public UserStatistic getStatistic() {
        return statistic;
    }

    public double getConsistencyCheckProbability() {
        return consistencyCheckProbability;
    }

}
