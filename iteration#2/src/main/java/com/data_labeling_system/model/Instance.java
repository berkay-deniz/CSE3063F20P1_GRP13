package com.data_labeling_system.model;

import org.json.JSONObject;

public class Instance implements Parsable {
    private int id;
    private String instance;

    public Instance(String json) {
        parse(json);
    }

    @Override
    public void parse(String json) {
        // Parse the Instance json using org.json library
        JSONObject object = new JSONObject(json);
        id = object.getInt("id");
        instance = object.getString("instance");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
