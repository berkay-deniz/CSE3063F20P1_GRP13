package com.data_labeling_system.model;

import com.data_labeling_system.mechanism.LabelingMechanismFactory;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class BotUser extends User implements Parsable {

    public BotUser(String json) {
        parse(json);
        LabelingMechanismFactory labelingMechanismFactory = new LabelingMechanismFactory();
        mechanism = labelingMechanismFactory.makeLabelingMechanism(this.type);
        logger = Logger.getLogger(BotUser.class);
        logger.info("Created '" + name + "' as '" + type + "'.");
    }

    @Override
    public void parse(String json) {
        super.parse(json);
    }
}
