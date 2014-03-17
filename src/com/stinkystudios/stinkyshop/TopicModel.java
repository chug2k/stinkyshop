package com.stinkystudios.stinkyshop;

import org.json.JSONException;
import org.json.JSONObject;

public class TopicModel {
	private String name;
	private String id;
	
	public TopicModel(String jsonContent) {
		try {
			JSONObject json = new JSONObject(jsonContent);
			this.name = json.getString("name");
			this.id = json.getString("id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getName() {
		return name;
	}
	public String getId() {
		return id;
	}
	
}
