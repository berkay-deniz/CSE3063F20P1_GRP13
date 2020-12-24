package com.data_labeling_system.model;

import com.data_labeling_system.statistic.InstanceStatistic;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@JsonIgnoreProperties({"statistic"})
public class Instance implements Parsable {
    private int id;
    private String instance;
    private final InstanceStatistic statistic;
    private Label finalLabel;

    public Instance(String json) {
        statistic = new InstanceStatistic();
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

    public String getInstance() {
        return instance;
    }

    public InstanceStatistic getStatistic() {
        return statistic;
    }

    public void setFinalLabel(List<Assignment> assignments) {
        int max = 0;
        ArrayList<Label> finalLabels = new ArrayList<Label>();
        HashMap<Label, Integer> occurrenceOfLabels = new HashMap<>();
        for (Assignment assignment : assignments) {
            if (assignment.getInstance().getId() == this.id) {
                for (Label label : assignment.getLabels()) {
                    if (!occurrenceOfLabels.containsKey(label)) {
                        occurrenceOfLabels.put(label, 1);
                        if (max == 0) {
                            max = 1;
                            finalLabels.add(label);
                        }
                    }
                    else {
                        int occurrence = occurrenceOfLabels.get(label);
                        occurrence++;
                        occurrenceOfLabels.put(label, occurrence);
                        if (occurrence > max) {
                            max = occurrence;
                            finalLabels.clear();
                            finalLabels.add(label);
                        }
                        if (occurrence == max){
                            finalLabels.add(label);
                        }
                    }
                }

            }
        }
        int rand = (int)(Math.random() * finalLabels.size());
        this.finalLabel = finalLabels.get(rand);
    }

    public Label getFinalLabel() {
        return finalLabel;
    }
}
