package com.stinkystudios.stinkyshop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.stinkystudios.stinkyshop.UserModel;

/*
 * Logs in & returns the current user (as PlayerModel).
 */
public class LoginHelper {
	
	private LoginDelegate mDelegate = null;
	private Activity mActivity = null;
	private UserModel mUser = null;
	
	public LoginHelper(LoginDelegate delegate, Activity activity) {
		mDelegate = delegate;
		mActivity = activity;
	}
	
	public static final String PROPERTY_USER_ID = "user_id";

	
	/*
	 * Called from Activity.onCreate
	 */
	public void startLogin() {
		
		Session session = Session.getActiveSession();
		
		if (session != null && !session.isOpened() && !session.isClosed()) 
		{
			// I've never seen this path getting executed. 
			// Charles had it in StinkyDaVinciActivity so I took it over. - Sinan
			
			Log.i("Login", "FB session found");
			OpenRequest openReq = new Session.OpenRequest(mActivity);
			openReq.setCallback(new FBSessionStatusCallback());
			
			session.openForRead(openReq);
		} 
		else 
		{
			Log.i("Login", "No FB session found");
			Session.openActiveSession(mActivity, true, new FBSessionStatusCallback());
		}
	}
	
	/*
	 * Called from Activity.onActivityResult
	 */
	public void finishLogin(int requestCode, int resultCode, Intent data) {
		Session.getActiveSession().onActivityResult(mActivity, requestCode, resultCode, data);
	}
	
	/*
	 * Called when user wants to logout
	 */
	public void deleteLogin() {
				
		// Also clear all values from shared prefs
		final SharedPreferences.Editor editor = mActivity.getSharedPreferences("Login", Context.MODE_PRIVATE).edit();
		
		editor.clear();
		editor.commit();
	}
	
	protected void getOrCreateUserFromFacebook(GraphUser graphUser) {
		
		if (graphUser == null) {
			Log.e("Login", "GraphUser is null");
			notifyDelegateOnUIThread(null);
			return;
		}
		mUser = new UserModel();
		mUser.setFbtoken(Session.getActiveSession().getAccessToken());
		mUser.setFbuid(graphUser.getId());
		mUser.setName(graphUser.getName());
		notifyDelegateOnUIThread(mUser);
	}
	
	protected void notifyDelegateOnUIThread(final UserModel player) {
		
		mActivity.runOnUiThread(new Runnable() { 
			@Override
			public void run() {
				mDelegate.handleUserLogIn(player);
			}
		});
	}

	
	private class FBSessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			
			if (session.isOpened()) {
				Log.d("Login", "FB Session is open");
				
				// If stored user credentials are available, 
				// we don't need to carry out the rest of the login process.
				if (mUser != null) {
					notifyDelegateOnUIThread(mUser);
					return;
				}
				
				Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser graphUser, Response response) {
						Log.d("Login", "FB Me Request complete");
						getOrCreateUserFromFacebook(graphUser);
					}
					
				});
			}
		}
	}
	
	/**
	 * Delegate definition
	 */
	public interface LoginDelegate {
		// User object will be empty when login fails for some reason
		public void handleUserLogIn(UserModel user);

		// So the first method should probably be called "facebook login"
		// the second one is fired after we login to *our* stinky servers.
		// so we'll have the topic, etc. 
		public void handleUserStinkyLogin(UserModel user);
		
		public void setTopicLoading();

		public void showNextTopic(TopicModel topicModel);

		public void onPhotoUploadStart();

		public void onPhotoUploadSuccess();

		public void onPhotoUploadFailure();
	}
}
