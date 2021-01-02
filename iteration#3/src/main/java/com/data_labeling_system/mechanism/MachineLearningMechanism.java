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

public class MachineLearningMechanism extends LabelingMechanism {
    @Override
    public Assignment assign(User user, Instance instance, List<Label> labels, int maxNumOfLabels) throws IOException {

        // Create Arraylist to keep labels assigned to instances
        ArrayList<Label> assignedLabels = new ArrayList<>();
        // Create Arraylist to keep copying of labels
        ArrayList<Label> tempLabels = new ArrayList<>(labels);

        Path path = Paths.get("data");
        FastTextClassifier classifier = FastTextClassifier.load(path);

        String s = instance.getInstance();


        // String processed = String.join(" ", TurkishTokenizer.DEFAULT.tokenizeToStrings(s));
        // processed = processed.toLowerCase(Turkish.LOCALE);
        List<ScoredItem<String>> res = classifier.predict(s, 1);
        StringBuffer modifiedLabel = new StringBuffer(res.get(0).item);
        modifiedLabel.delete(0,9);
        for (int i = 0; i < tempLabels.size(); i++) {

            if (tempLabels.get(i).getText().equals(modifiedLabel.toString())) {
                assignedLabels.add(tempLabels.get(i));
                tempLabels.remove(i);
                break;

            }

        }


        return new Assignment(instance, assignedLabels, user, new Date());
    }
}
