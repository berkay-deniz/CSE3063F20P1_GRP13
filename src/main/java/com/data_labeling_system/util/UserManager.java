package com.data_labeling_system.util;
import java.util.ArrayList;
import java.util.List;
import org.json.*;
import com.data_labeling_system.model.User;

public class UserManager {
	
	private List<User> users;
	
	public  void createUsers(String json) {
		JSONObject object = new JSONObject(json);
		JSONArray userArray = object.getJSONArray("users");
        users = new ArrayList<>();
        for (int i = 0; i < userArray.length(); i++) {
            users.add(new User(userArray.getJSONObject(i).toString()));
        }
		
	}

}
