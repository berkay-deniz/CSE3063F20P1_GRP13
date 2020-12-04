package com.data_labeling_system.model;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.mechanism.LabelingMechanismFactory;
import org.json.*;

public class User implements Parsable {
    private int id;
    private String name;
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
