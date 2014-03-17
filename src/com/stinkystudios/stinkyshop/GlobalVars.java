package com.stinkystudios.stinkyshop;

import android.app.Application;
import android.util.Log;

public class GlobalVars extends Application {

  private UserModel mUser;
  private TopicModel mTopic;
  private boolean mFFCMode;

	public UserModel getUser() {
		return mUser;
	}

	public void setUser(UserModel user) {
		Log.d("Login", "Logged in with user fbuid: " + user.getFbuid());
		this.mUser = user;
	}

	public void setTopic(TopicModel topic) {
		this.mTopic = topic;
		
	}

	public TopicModel getTopic() {
		return mTopic;
	}

	public void setFFCMode(boolean isUsingFFC) {
		mFFCMode = isUsingFFC;
	}
	public boolean getFFCMode() {
		return mFFCMode;
	}

}