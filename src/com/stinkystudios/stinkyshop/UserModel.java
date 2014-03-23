package com.stinkystudios.stinkyshop;

public class UserModel {

	private String fbuid;
	private String fbtoken; // TODO(Charles): Maybe I don't need this. Just grab from Session.getActiveSession().getAccessToken()
	private String name;
	private int unseenCount;
	
	public String getFbuid() {
		return fbuid;
	}
	public void setFbuid(String fbuid) {
		this.fbuid = fbuid;
	}
	public String getFbtoken() {
		return fbtoken;
	}
	public void setFbtoken(String fbtoken) {
		this.fbtoken = fbtoken;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getUnseenCount() {
		return this.unseenCount;
	}
	public void setUnseenCount(int unseenCount) {
		this.unseenCount = unseenCount;
	}
}
