package com.data_labeling_system.util;

import com.data_labeling_system.model.User;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.json.*;
import com.data_labeling_system.model.User;




public class UserManager {
	private final Logger logger;

	private List<User> users;

	public UserManager() {
		logger = Logger.getLogger(UserManager.class);
	}

	public void createUsers(String json) {
		JSONObject object = new JSONObject(json);
		
		JSONArray userArray = object.getJSONArray("users");
		this.users = new ArrayList<>();

		for (int i = 0; i < userArray.length(); i++) {
			User user = new User(userArray.getJSONObject(i).toString());
			this.users.add(user);
			logger.info("Created '" + user.getName() + "' as '" + user.getType() + "'.");
		}
		
		

	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<User> getUsers() {
		return users;
	}

}
