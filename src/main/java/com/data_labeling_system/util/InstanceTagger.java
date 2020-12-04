package com.data_labeling_system.util;

import com.data_labeling_system.mechanism.LabelingMechanism;
import com.data_labeling_system.model.Assignment;
import com.data_labeling_system.model.Dataset;
import com.data_labeling_system.model.User;
import java.util.ArrayList;
import java.util.List;



public class InstanceTagger {

	private Dataset dataset;
	
	private List<User> users;
	
	

	public InstanceTagger() {
	}
	
	
	public void assignLabels() {
		
		ArrayList<Assignment> assignments = new ArrayList<>();
		
		for(int i=0 ; i < dataset.getInstances().size(); i++) {
			for (User user: users) {
				LabelingMechanism labelingMechanism= user.getMechanism();
				assignments.add(labelingMechanism.assign(user, dataset.getInstances().get(i), 
						dataset.getLabels(), dataset.getMaxNumOfLabels()));
			}
		}
		
		
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
