package com.data_labeling_system.mechanism;

import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Instance;
import com.data_labeling_system.model.Label;
import com.data_labeling_system.model.User;
import zemberek.classification.FastTextClassifier;
import zemberek.core.ScoredItem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MachineLearningMechanism extends LabelingMechanism {
    @Override
    public Assignment assign(User user, Instance instance, Map<Integer, Label> labels, int maxNumOfLabels) {

        // Create Arraylist to keep labels assigned to instances
        List<Label> assignedLabels = new ArrayList<>();
        // Create Arraylist to keep copying of labels
        List<Label> tempLabels = new ArrayList<>(labels.values());

        Path path = Paths.get("MachineLearningData");
        FastTextClassifier classifier;
        try {
            classifier = FastTextClassifier.load(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        String s = instance.getInstance();

        List<ScoredItem<String>> res = classifier.predict(s, 1);
        if (res.isEmpty()) {
            assignedLabels = assignRandomly(labels, maxNumOfLabels);
        } else {
            StringBuilder modifiedLabel = new StringBuilder(res.get(0).item);
            modifiedLabel.delete(0, 9);
            for (int i = 0; i < tempLabels.size(); i++) {
                if (tempLabels.get(i).getText().toLowerCase().equals(modifiedLabel.toString())) {
                    assignedLabels.add(tempLabels.get(i));
                    tempLabels.remove(i);
                    break;
                }
            }
        }

        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
