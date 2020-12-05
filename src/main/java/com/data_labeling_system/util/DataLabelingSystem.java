package com.data_labeling_system.util;

import com.data_labeling_system.model.Dataset;

import java.io.IOException;

import org.apache.log4j.Logger;

public class DataLabelingSystem {
	private final Logger logger;
    private Dataset dataset;
    private IOManager ioManager;
    private UserManager userManager;
    private InstanceTagger instanceTagger;

    public DataLabelingSystem() {
    	logger = Logger.getLogger(DataLabelingSystem.class);
    	this.ioManager = new IOManager();
        this.userManager = new UserManager();
        this.instanceTagger = new InstanceTagger();

    }

    public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
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
        String datasetJson = this.ioManager.readInputFile("input.json");
        // Create dataset and user objects using the json strings
        dataset = new Dataset(datasetJson);
        userManager.createUsers(configJson);
        // Assign users using the UserManager class
        this.dataset.setUsers(userManager.getUsers());
        // Assign updated objects to the instanceTagger object
        this.instanceTagger.setDataset(this.dataset);
        this.instanceTagger.setUsers(this.userManager.getUsers());
        // Assign label to instances
        this.instanceTagger.assignLabels();
        // Take final dataset and write as json file
        this.dataset = this.instanceTagger.getDataset();
        this.ioManager.printFinalDataset(this.dataset, "output.json");
    }
}
