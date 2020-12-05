package com.data_labeling_system.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.json.JSONObject;

@JsonPropertyOrder({"label id", "label text"})
public class Label implements Parsable {
    @JsonProperty("label id")
    private int id;
    @JsonProperty("label text")
    private String text;

    public Label(String json) {
        parse(json);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void parse(String json) {
        // Parse the Label json using org.json library
        JSONObject object = new JSONObject(json);
        this.id = object.getInt("label id");
        this.text = object.getString("label text");
    }
}
