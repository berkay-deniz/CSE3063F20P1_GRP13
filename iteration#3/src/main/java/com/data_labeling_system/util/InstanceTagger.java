package com.data_labeling_system.util;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.model.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class InstanceTagger {
    private final Logger logger;

    private Dataset dataset;
    private List<User> users;

    public InstanceTagger() {
        logger = Logger.getLogger(Instance.class);
    }



    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
