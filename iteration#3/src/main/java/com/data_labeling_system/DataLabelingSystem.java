package com.data_labeling_system;

import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.User;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

public class DataLabelingSystem {
    private final Logger logger;
    private final List<Dataset> datasets;
    private final Map<Integer, User> users;
    private Dataset currentDataset;

    public DataLabelingSystem() {
        logger = Logger.getLogger(DataLabelingSystem.class);
        datasets = new ArrayList<>();
        users = new HashMap<>();
    }

    public void startSystem() throws IOException {
        logger.info("The system has started");
        // Create output folders to organize datasets and metrics
        createOutputFolders();
        // Read json files and keep as string
        String configJson = readInputFile("config.json");
        // Create users using config.json
        createUsers(configJson);
        logger.info("Users are created.");
        JSONObject configObject = new JSONObject(configJson);
        JSONArray datasetArray = configObject.getJSONArray("datasets");
        int currentDatasetId = configObject.getInt("currentDatasetId");
        for (int i = 0; i < datasetArray.length(); i++) {
            JSONObject datasetObject = datasetArray.getJSONObject(i);
            createDataset(datasetObject, currentDatasetId);
        }

        if (currentDataset == null) {
            logger.error("Current dataset is not defined.");
            return;
        }

        // Calculate metrics for User
        for (User user : users.values()) {
            user.getStatistic().calculateMetrics();
            logger.info("User statistics are calculated for user with UserId: " + user.getId());
        }

        // Calculate metrics for Dataset
        for (Dataset dataset : datasets) {
            dataset.calculateMetrics();
            logger.info("Dataset statistics are calculated for dataset with DatasetId: " + dataset.getId());
        }

        // Assign label to instances
        currentDataset.assignLabels();
    }

    private void createOutputFolders() {
        createFolder("outputs");
        createFolder("metrics");
        createFolder("metrics/users");
        createFolder("metrics/datasets");
        createFolder("metrics/instances");
    }

    private void createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.mkdir())
            logger.info("'" + folderPath + "' folder has been created.");
    }

    private void createDataset(JSONObject datasetObject, int currentDatasetId) {
        int datasetId = datasetObject.getInt("id");

        boolean doesFileExist = new File("./outputs/output" + datasetId + ".json").exists();
        // Check the existence of the output file and read input file accordingly.
        String inputFileName = doesFileExist ? "./outputs/output" + datasetId + ".json" : datasetObject.getString("filePath");
        String datasetJson = readInputFile(inputFileName);
        JSONArray registeredUserIds = datasetObject.getJSONArray("users");

        Map<Integer, User> registeredUsers = new LinkedHashMap<>();
        // Find users registered in the dataset and save them in the dataset.
        for (int i = 0; i < registeredUserIds.length(); i++) {
            int userId = registeredUserIds.getInt(i);
            registeredUsers.put(userId, users.get(userId));
        }
        // Create dataset object and save them in the datasets list.
        Dataset dataset = new Dataset(datasetJson, registeredUsers);
        datasets.add(dataset);
        logger.info("Dataset with DatasetId: " + dataset.getId() + " is created and added to Dataset list.");

        for (User user : registeredUsers.values()) {
            user.getStatistic().addDataset(dataset);
        }

        if (datasetId == currentDatasetId) {
            currentDataset = dataset;
        }
    }

    public void createUsers(String json) {
        // Create json object and keep users in a json array
        JSONObject object = new JSONObject(json);
        JSONArray userArray = object.getJSONArray("users");
        // Create user objects using the values we hold in json array into objects
        for (int i = 0; i < userArray.length(); i++) {
            JSONObject userObject = userArray.getJSONObject(i);
            User user = new User(userObject.toString());
            users.put(userObject.getInt("user id"), user);
            logger.info("Created '" + user.getName() + "' as '" + user.getType() + "'.");
        }
    }

    private String readInputFile(String fileName) {
        // Read json file and return string value
        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (NoSuchFileException e) {
            logger.error("Input file '" + fileName + "' not found.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        return json;
    }
}
