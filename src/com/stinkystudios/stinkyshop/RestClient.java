package com.stinkystudios.stinkyshop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.stinkystudios.stinkyshop.LoginHelper.LoginDelegate;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RestClient {
	private static String URL_BASE = "http://backshop.herokuapp.com/";
//	private static String URL_BASE = "http://10.186.32.106:3000/";
	private final static int FORTY_FIVE_SECONDS = 45 * 1000;
	
	public static void logIn(final Activity activity, 
			final LoginDelegate loginDelegate, final UserModel user) {
		RequestParams params = new RequestParams();
  	params.put("token", user.getFbtoken());
  	
  	AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String content) {
      	try {
      		JSONObject ret = new JSONObject(content);
    			String points = ret.getString("score");
    			String name = ret.getString("name");
    			
      		Crouton.showText(activity, "Welcome " + name + 
      				" - score:" + points, Style.INFO);
      		
      	} catch(Exception e) {
      			Toast.makeText(activity, "Failure parsing server response...", 
      				Toast.LENGTH_LONG).show();
				}
      	loginDelegate.handleUserStinkyLogin(user);
      	RestClient.getNextTopic(activity, loginDelegate, user);
      }

      @Override
      public void onFailure(Throwable error, String content) {
        Toast.makeText(activity, "Failure: " + content, Toast.LENGTH_LONG).show();
     	}
  	};

  	AsyncHttpClient client = new AsyncHttpClient();
  	client.setTimeout(FORTY_FIVE_SECONDS);
  	client.post(URL_BASE + "login.json", params, responseHandler);
  	
	}
	
	public static void submitPhoto(final Activity activity, 
			final LoginDelegate loginDelegate, Bitmap bitmap) {
		GlobalVars gv = (GlobalVars)activity.getApplication();
		UserModel user = gv.getUser();
		TopicModel topic = gv.getTopic();
  	RequestParams params = new RequestParams();
  	ByteArrayOutputStream stream = new ByteArrayOutputStream();
  	bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
  	byte[] byteArray = stream.toByteArray();
  	params.put("image", new ByteArrayInputStream(byteArray));
  	params.put("token", user.getFbtoken());
  	params.put("topic_id", topic.getId());
  	
  	AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
  	        @Override
  	        public void onSuccess(String content) {
              Toast.makeText(activity, "Photo Uploaded!", Toast.LENGTH_SHORT).show();
              loginDelegate.onPhotoUploadSuccess();
  	        }

  	        @Override
  	        public void onFailure(Throwable error, String content) {
              Toast.makeText(activity, "Failure: " + content, Toast.LENGTH_LONG).show();
              loginDelegate.onPhotoUploadFailure();
  	        }

  	        @Override
  	        public void onProgress(int position, int length) {
              // e.g. Update a ProgressBar:
	        		Toast.makeText(activity, "Uploading now " + 
	        				position + " out of " + length, Toast.LENGTH_SHORT).show();
  	        }
  	};

  	AsyncHttpClient client = new AsyncHttpClient();
  	client.setTimeout(FORTY_FIVE_SECONDS * 2); // Uploading pictures can be slowww.

  	client.post(URL_BASE + "submissions.json", params, responseHandler);
  	loginDelegate.onPhotoUploadStart();
	}
	
	public static void getNextTopic(final Activity activity, final LoginDelegate loginDelegate, UserModel user) {
		AsyncHttpClient client = new AsyncHttpClient();
  	client.setTimeout(FORTY_FIVE_SECONDS);

  	loginDelegate.setTopicLoading();
		
  	RequestParams params = new RequestParams();
  	params.put("token", user.getFbtoken());

		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String content) {
        // Set the text.
      	// Enable the camera button.
      	loginDelegate.showNextTopic(new TopicModel(content));
      }
      @Override
      public void onFailure(Throwable error, String content) {
        Toast.makeText(activity, "Failure fetching topic: " + content, Toast.LENGTH_LONG).show();
      }
		};		
		client.get(URL_BASE + "topics/next", params, responseHandler);
	}

}
