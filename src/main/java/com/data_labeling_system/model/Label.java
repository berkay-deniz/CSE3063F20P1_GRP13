package com.data_labeling_system.model;
import org.json.*;

public class Label implements Parsable {
    private int id;
    private String text;
    public Label(String json){
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
        JSONObject object = new JSONObject(json);
        this.id = object.getInt("label id");
        this.text = object.getString("label text");
    }
}
