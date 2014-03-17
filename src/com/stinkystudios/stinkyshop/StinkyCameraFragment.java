package com.stinkystudios.stinkyshop;

import java.io.ByteArrayOutputStream;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidzeitgeist.mustache.fragment.CameraFragment;

public class StinkyCameraFragment extends CameraFragment {

	private Boolean mShouldTakePhoto = false;
	private Camera.PreviewCallback mPreview;
	
	
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View previewView = super.onCreateView(inflater, container, savedInstanceState);
//      container.addView(previewView);
      View myCameraWrapper = inflater.inflate(R.layout.camera_main, null);
  		ViewGroup frameLayout = (ViewGroup) myCameraWrapper
  				.findViewById(R.id.camera_container);
  		frameLayout.addView(previewView, 0);

      return myCameraWrapper;
  }
	
	@Override
	protected void startCamera() {
		if (camera != null) {
			stopCamera();
		}
		mPreview = new Camera.PreviewCallback() { 
			// Taken from https://code.google.com/p/android/issues/detail?id=823#c37
			public void onPreviewFrame(byte[] data, Camera arg1) {
				synchronized(mShouldTakePhoto) {
					if(!mShouldTakePhoto) {
						return;
					} 
					mShouldTakePhoto = false;
					YuvImage yuvimage = new YuvImage(data,ImageFormat.NV21,arg1.getParameters().getPreviewSize().width,arg1.getParameters().getPreviewSize().height,null);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					yuvimage.compressToJpeg(new Rect(0,0,arg1.getParameters().getPreviewSize().width,arg1.getParameters().getPreviewSize().height), 80, baos);
					onPictureTaken(baos.toByteArray(), camera);
//						outStream = new FileOutputStream(String.format("/%s/%d.jpg",
//								Environment.getExternalStorageDirectory().getPath(), 
//								System.currentTimeMillis()));	
//						outStream.write(baos.toByteArray());
//						outStream.close();
					Log.d(TAG, "onPreviewFrame - wrote bytes: " + data.length);
				}				
			}

		};  
		camera = Camera.open(cameraId);
		camera.setPreviewCallback(mPreview);
		startCameraPreview();
	}
	
	@Override
	protected void stopCamera() {
		camera.setPreviewCallback(null);
		super.stopCamera();
	}

	public void takePhotoFromPreview() {
		camera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				//Just take the photo.
				orientationListener.rememberOrientation();
				mShouldTakePhoto = true;
			}			 
		});
	}
}
