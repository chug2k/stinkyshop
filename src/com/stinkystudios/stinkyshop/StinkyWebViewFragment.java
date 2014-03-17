package com.stinkystudios.stinkyshop;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class StinkyWebViewFragment extends Fragment {

	private StinkyWebView mWebView;
	private ProgressBar mProgressBar;
	private boolean mIsWebViewAvailable;

	public static final String URL_TO_LOAD_KEY = "url_to_load_key";

	/**
	 * Creates a new fragment which loads the supplied url as soon as it can
	 * 
	 * @param url
	 *          the url to load once initialised
	 */

	static StinkyWebViewFragment newInstance(String url) {
		StinkyWebViewFragment f = new StinkyWebViewFragment();
		Bundle args = new Bundle();

		args.putString(URL_TO_LOAD_KEY, url);
		f.setArguments(args);

		return (f);
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	/**
	 * Called to instantiate the view. Creates and returns the WebView.
	 */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View viewWrapper = inflater.inflate(R.layout.webview_progressbar, null);
		mProgressBar = (ProgressBar) viewWrapper.findViewById(R.id.progressBar1);
		
		if (mWebView != null) {
			mWebView.destroy();
		}
		mWebView = (StinkyWebView) viewWrapper.findViewById(R.id.webView1);
		mWebView.setWebViewClient(new InnerWebViewClient()); // forces it to open in
																													// app
		mWebView.loadUrl(getArguments().getString(URL_TO_LOAD_KEY));
		mIsWebViewAvailable = true;
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		
		// http://stackoverflow.com/questions/14514709/singlecursorhandlertouchevent-geteditablesupport-fasle-bug
		viewWrapper.setOnTouchListener(new View.OnTouchListener() {
	    @Override
	    public boolean onTouch(View view, MotionEvent event) {
	        if(event.getAction() == MotionEvent.ACTION_DOWN && !view.hasFocus()) {
	            mWebView.requestFocus();
	        }
	        return false;
	    }
	});

		return viewWrapper;
	}

	/**
	 * Convenience method for loading a url. Will fail if {@link View} is not
	 * initialised (but won't throw an {@link Exception})
	 * 
	 * @param url
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void loadUrl(String url) {
		if (mIsWebViewAvailable)
			getWebView().loadUrl(getArguments().getString(URL_TO_LOAD_KEY));
		else
			Log.w("ImprovedWebViewFragment",
					"WebView cannot be found. Check the view and fragment have been loaded.");
	}

	/**
	 * Called when the fragment is visible to the user and actively running.
	 * Resumes the WebView.
	 */
	@Override
	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}

	/**
	 * Called when the fragment is no longer resumed. Pauses the WebView.
	 */
	@Override
	public void onResume() {
		mWebView.onResume();
		super.onResume();
	}

	/**
	 * Called when the WebView has been detached from the fragment. The WebView is
	 * no longer available after this time.
	 */
	@Override
	public void onDestroyView() {
		mIsWebViewAvailable = false;
		super.onDestroyView();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setupActionBar();
		super.onCreate(savedInstanceState);
	}
	
	private void setupActionBar() {
//		ActionBar ab = getActivity().getActionBar();
//		LayoutInflater li = LayoutInflater.from(getActivity());
//		View customView = li.inflate(R.layout.action_bar_webview, null);
//		ab.setCustomView(customView);
//		ab.hide();
	}
	
	/**
	 * Called when the fragment is no longer in use. Destroys the internal state
	 * of the WebView.
	 */
	@Override
	public void onDestroy() {
		if (mWebView != null) {
			mWebView.destroy();
			mWebView = null;
		}
		super.onDestroy();
	}

	/**
	 * Gets the WebView.
	 */
	public StinkyWebView getWebView() {
		return mIsWebViewAvailable ? mWebView : null;
	}

	/* To ensure links open within the application */
	private class InnerWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {

			super.onPageStarted(view, url, favicon);
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			mProgressBar.setVisibility(View.GONE);
		}

	}
}