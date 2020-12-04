package com.data_labeling_system.model;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.mechanism.LabelingMechanismFactory;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;

import org.json.*;

@JsonIgnoreProperties({"mechanism"})
@JsonPropertyOrder({ "user id", "user name", "user type" })
public class User implements Parsable {
	@JsonProperty("user id")
    private int id;
	@JsonProperty("user name")
    private String name;
	@JsonProperty("user type")
    private String type;
    
    private LabelingMechanism mechanism;

    public User(String json) {
        LabelingMechanismFactory labelingMechanismFactory = new LabelingMechanismFactory();
        this.parse(json);
        this.mechanism = labelingMechanismFactory.makeLabelingMechanism(this.type);
    }

    @Override
    public void parse(String json) {
        JSONObject object = new JSONObject(json);
        this.id = object.getInt("user id");
        this.name = object.getString("user name");
        this.type = object.getString("user type");
    }

    public String stringify(Parsable parsable) {
        return "";
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LabelingMechanism getMechanism() {
        return mechanism;
    }

    public void setMechanism(LabelingMechanism mechanism) {
        this.mechanism = mechanism;
    }
}
