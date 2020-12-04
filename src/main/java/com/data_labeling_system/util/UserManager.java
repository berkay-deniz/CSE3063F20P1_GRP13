package com.data_labeling_system.util;

import java.util.ArrayList;
import java.util.List;
import org.json.*;
import com.data_labeling_system.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserManager {

	private List<User> users;

	public void createUsers(String json) {
		JSONObject object = new JSONObject(json);
		
		JSONArray userArray = object.getJSONArray("users");
		this.users = new ArrayList<>();
		
		for (int i = 0; i < userArray.length(); i++) {
			this.users.add(new User(userArray.getJSONObject(i).toString()));
		}
		
		

	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<User> getUsers() {
		return users;
	}

}
