package com.data_labeling_system.model;

import org.json.JSONObject;

public class Instance implements Parsable {
    private int id;
    private String content;

    public Instance(String json) {
        parse(json);
    }

    @Override
    public void parse(String json) {
        JSONObject object = new JSONObject(json);
        id = object.getInt("id");
        content = object.getString("content");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
