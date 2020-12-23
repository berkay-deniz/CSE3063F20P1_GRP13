package com.data_labeling_system.model;

import com.data_labeling_system.statistic.InstanceStatistic;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class Instance implements Parsable {
    private int id;
    private String instance;
    private InstanceStatistic statistic;
    private Label finalLabel;

    public Instance(String json) {
        parse(json);
        statistic = new InstanceStatistic();
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

    public InstanceStatistic getStatistic() {
        return statistic;
    }

    public void setFinalLabel(List<Assignment> assignments) {
        int max = 0;
        Label finalLabel = null;
        HashMap<Label, Integer> occurrenceOfLabels = new HashMap<>();
        for (Assignment assignment : assignments) {
            if (assignment.getInstance().getId() == this.id) {
                for (Label label : assignment.getLabels()) {
                    if (!occurrenceOfLabels.containsKey(label)) {
                        occurrenceOfLabels.put(label, 1);
                        if (max == 0) {
                            max = 1;
                            finalLabel = label;
                        }
                    } else {
                        int occurrence = occurrenceOfLabels.get(label);
                        occurrence++;
                        occurrenceOfLabels.put(label, occurrence);
                        if (occurrence > max) {
                            max = occurrence;
                            finalLabel = label;
                        }
                    }
                }

            }
        }
        this.finalLabel = finalLabel;
    }
}
