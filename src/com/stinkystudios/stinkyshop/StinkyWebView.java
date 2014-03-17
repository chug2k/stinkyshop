package com.stinkystudios.stinkyshop;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class StinkyWebView extends WebView {

	public StinkyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void onScrollChanged(int l, int t) {
		super.onScrollChanged(l, t, l, t);
	}
}
