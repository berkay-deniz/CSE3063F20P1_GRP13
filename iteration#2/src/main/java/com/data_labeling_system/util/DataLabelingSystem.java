package com.data_labeling_system.util;

import com.data_labeling_system.model.Dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.data_labeling_system.model.User;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class DataLabelingSystem {
    private final Logger logger;
    private List<Dataset> datasets;
    private IOManager ioManager;
    private UserManager userManager;
    private InstanceTagger instanceTagger;

    public DataLabelingSystem() {
        logger = Logger.getLogger(DataLabelingSystem.class);
        this.ioManager = new IOManager();
        this.userManager = new UserManager();
        this.instanceTagger = new InstanceTagger();

    }

    public List<Dataset> getDataset() {
        return datasets ;
    }

    public void setDataset(List<Dataset> datasets) {
        this.datasets=datasets;
    }

    public IOManager getIoManager() {
        return ioManager;
    }

    public void setIoManager(IOManager ioManager) {
        this.ioManager = ioManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public InstanceTagger getInstanceTagger() {
        return instanceTagger;
    }

    public void setInstanceTagger(InstanceTagger instanceTagger) {
        this.instanceTagger = instanceTagger;
    }

    public void startSystem() throws IOException {
        logger.info("The system has started");
        // Read json files and keep as string
        String configJson = this.ioManager.readInputFile("config.json");
        userManager.createUsers(configJson);
        JSONObject configObject = new JSONObject(configJson);
        JSONArray datasetArray = configObject.getJSONArray("datasets");
        int datasetId = configObject.getInt("currentDatasetId");
        Dataset currentDataset = null;
        String inputFile = null;
        for (int i = 0; i < datasetArray.length(); i++) {
            JSONObject datasetObject = datasetArray.getJSONObject(i);
            int id = datasetObject.getInt("id");

            if (ioManager.doesFileExist("outputs/id.json")) {
                inputFile = "outputs/id.json";
            } else {
                inputFile = datasetObject.getString("filePath");
            }

            String datasetJson = this.ioManager.readInputFile(inputFile);
            JSONArray registeredUserIds = datasetObject.getJSONArray("users");

            Dataset dataset = new Dataset(datasetJson);
            datasets.add(dataset);
            List <User> configUsers = new ArrayList<User>();



            for(int j = 0 ; j<registeredUserIds.length();j++) {

                configUsers.add(userManager.findUser(registeredUserIds.getInt(j)));
            }
            dataset.setUsers(configUsers);


            if (id == datasetId) {
                currentDataset = dataset;
            }
        }



        // Assign updated objects to the instanceTagger object
        this.instanceTagger.setDataset(currentDataset);
        ArrayList<User> activeUsers = new ArrayList<>(currentDataset.getUsers());
        this.instanceTagger.setUsers(activeUsers);
        // Assign label to instances
        this.instanceTagger.assignLabels();
        // Take final dataset and write as json file
        currentDataset = this.instanceTagger.getDataset();
        this.ioManager.printFinalDataset(currentDataset, "outputs/id.json");
    }
}
