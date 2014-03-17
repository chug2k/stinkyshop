package com.stinkystudios.stinkyshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidzeitgeist.mustache.listener.CameraFragmentListener;
import com.stinkystudios.stinkyshop.LoginHelper.LoginDelegate;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends FragmentActivity implements
		CameraFragmentListener, LoginDelegate {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
	 * keep every loaded fragment in memory. If this becomes too memory intensive,
	 * it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private StinkyCameraFragment mCameraFragment;

	private LoginHelper mLoginHelper;

	private Bitmap mPhotoBitmap;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stinky_shop_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
				
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(mSectionsPagerAdapter);
//		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			@Override
//			public void onPageSelected(int arg0) {
//			}
//			@Override
//			public void onPageScrolled(int arg0, float arg1, int arg2) {
//			}
//			
//			@Override
//			public void onPageScrollStateChanged(int arg0) {				
//			}
//		});
		
		mCameraFragment = new StinkyCameraFragment();
		this.addViewPagerPage(mCameraFragment);
		getActionBar().hide(); // Shown once we load a topic.
		
		mLoginHelper = new LoginHelper(this, this);
		mLoginHelper.startLogin();

	}
	public void addViewPagerPage(Fragment f) {
		mSectionsPagerAdapter.addTab(f);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stinky_shop_main, menu);
		return true;
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
	 * of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter 
	implements ViewPager.OnPageChangeListener {

		private List<Fragment> mFragments = new ArrayList<Fragment>();

		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			//TODO(Charles): Hacky.
		}
		

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.			
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
		public void addTab(Fragment fragment) {
			mFragments.add(fragment);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}


		@Override
		public void onPageSelected(int pageNum) {
			if(pageNum != 0) {
				MainActivity.this.getActionBar().hide();
			} else {
				MainActivity.this.getActionBar().show();
				UserModel user = ((GlobalVars) getApplication()).getUser();
				RestClient.getNextTopic(MainActivity.this, MainActivity.this, user);
			}
		}
	}

	@Override
	public void onCameraError() {
		Toast.makeText(this, "Something went wrong. :(", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPictureTaken(Bitmap bitmap) {
		// Freeze preview, and unhide buttons.
		swapButtons(true);
		mCameraFragment.stopCameraPreview();
		mPhotoBitmap = bitmap;
	}
	private void swapButtons(boolean toEditMode) {
		int editButtonsVisibility = View.VISIBLE;
		int takeButtonsVisibility = View.GONE;
		if(!toEditMode) {
			editButtonsVisibility = View.GONE;
			takeButtonsVisibility = View.VISIBLE;
		}
		findViewById(R.id.cancel_button).setVisibility(editButtonsVisibility);
		findViewById(R.id.upload_button).setVisibility(editButtonsVisibility);
		findViewById(R.id.take_button).setVisibility(takeButtonsVisibility);
		findViewById(R.id.flip_button).setVisibility(takeButtonsVisibility);
	}
	private void hideButtons() {
		findViewById(R.id.cancel_button).setVisibility(View.GONE);
		findViewById(R.id.upload_button).setVisibility(View.GONE);
		findViewById(R.id.take_button).setVisibility(View.GONE);
		findViewById(R.id.flip_button).setVisibility(View.GONE);
	}
	
	public void onClickTakeButton(View v) {
		mCameraFragment.takePhotoFromPreview();
	}
	
	public void onClickUploadButton(View v) {
  	RestClient.submitPhoto(this, this, mPhotoBitmap);		
	}
	public void onClickCancelButton(View v) {
		swapButtons(false);
		mPhotoBitmap = null;
		mCameraFragment.startCameraPreview();
	}
	public void onClickFlipButton(View v) {
		mCameraFragment.swapCamera();
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// We probably re-gained focus after Facebook permission dialog stuff ..
		mLoginHelper.finishLogin(requestCode, resultCode, data);
	}
	/**
	 * LoginHelper Callback Methods
	 */
	@Override
	public void handleUserLogIn(UserModel player) {
		if (player != null) {
			((GlobalVars) getApplication()).setUser(player);
			// Register with GCM and all that, but do it silently.

			// TODO(charles): Don't register every time. The example
			// has me save it in SharedPrefs; I think we should just store on server.

			// new GCMRegistrationTask(mContext, player).execute();
			// refreshListOfGames(); //Hack(charles): The API doesn't return the
			// games, so grab them.
			RestClient.logIn(this, this, player);
		} else {
			Crouton.cancelAllCroutons();
			Crouton.showText(this,
					"\n\n\nLogin failed. Please check your connection", Style.ALERT);
		}
	}
	
	@Override
	public void handleUserStinkyLogin(UserModel user) {
		this.addViewPagerPage(StinkyWebViewFragment
				.newInstance("http://backshop.herokuapp.com/votes/new?fbuid="
						+ user.getFbuid()));
		
		this.addViewPagerPage(StinkyWebViewFragment
				.newInstance("http://backshop.herokuapp.com/leaderboards?fbuid="
						+ user.getFbuid()));
	}
	@Override
	public void setTopicLoading() {
		ActionBar ab = getActionBar();
    ab.setTitle("Loading topic...");		
	}
	@Override
	public void showNextTopic(TopicModel nextTopic) {
		ActionBar ab = getActionBar();
		((GlobalVars)getApplication()).setTopic(nextTopic);
		int titleId = Resources.getSystem()
        .getIdentifier("action_bar_title", "id", "android");
		if (titleId > 0) {
	    TextView title = (TextView) findViewById(titleId);
	    title.setSingleLine(false);
	    title.setMaxLines(2);
	    title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			ab.setTitle(nextTopic.getName());							
			getActionBar().show();
		}
	}
	@Override
	public void onPhotoUploadStart() {
		this.onClickCancelButton(null);
		this.hideButtons();
	}
	@Override
	public void onPhotoUploadSuccess() {
		this.swapButtons(false);
		RestClient.getNextTopic(this, this,
				((GlobalVars) getApplication()).getUser());
	}
	@Override
	public void onPhotoUploadFailure() {
		this.swapButtons(false);
		RestClient.getNextTopic(this, this,
				((GlobalVars) getApplication()).getUser());
	}
}

