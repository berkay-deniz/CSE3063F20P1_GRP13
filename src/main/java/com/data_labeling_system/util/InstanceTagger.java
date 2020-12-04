package com.data_labeling_system.util;
import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.User;

import java.util.List;



public class InstanceTagger {

	private Dataset dataset;
	
	private List<User> users;
	
	
	
	public void assignLabels() {
		
		users=this.dataset.getUsers();
		
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
}
