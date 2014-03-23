package com.androidzeitgeist.mustache.fragment;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.androidzeitgeist.mustache.listener.CameraFragmentListener;
import com.androidzeitgeist.mustache.listener.CameraOrientationListener;
import com.androidzeitgeist.mustache.view.CameraPreview;

/**
 * Fragment for displaying the camera preview.
 *
 * @author Sebastian Kaspari <sebastian@androidzeitgeist.com>
 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {
    public static final String TAG = "Mustache/CameraFragment";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    protected int cameraId = CameraInfo.CAMERA_FACING_BACK;
    protected Camera camera;
    private SurfaceHolder surfaceHolder;
    private CameraFragmentListener listener;
    private int displayOrientation;
    private int layoutOrientation;

    protected CameraOrientationListener orientationListener;
		private SurfaceView mPreviewView;

    /**
     * On activity getting attached.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException(
                "Activity has to implement CameraFragmentListener interface"
            );
        }

        listener = (CameraFragmentListener) activity;

        orientationListener = new CameraOrientationListener(activity);
    }

    /**
     * On creating view for fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPreviewView = new CameraPreview(getActivity());

        mPreviewView.getHolder().addCallback(this);

        return mPreviewView;
    }

    /**
     * On fragment getting resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        orientationListener.enable();

        try {
        	// This is truly atrocious. Sometimes, for some reason,
        	// the camera preview turns black onResume. There's something
        	// going on here, but the surfacecallbacks don't seem to be
        	// the right place to be. Calling stopCamera here, without
        	// a delay, doesn't seem to do anything. WHY ANDROID WHY.
        	final Handler handler = new Handler();
        	handler.postDelayed(new Runnable() {
        	    @Override
        	    public void run() {
        	    		if(camera != null) {
          	       	stopCamera();
          	       	startCamera();        	    			
        	    		}
        	    }
        	}, 500);
        	startCamera();
        } catch (Exception exception) {
            Log.e(TAG, "Can't open camera with id " + cameraId, exception);

            listener.onCameraError();
            return;
        }
    }

    /**
     * On fragment getting paused.
     */
    @Override
    public void onPause() {
        super.onPause();

        orientationListener.disable();

        stopCamera();
    }

    /**
     * Start the camera preview.
     */
    public synchronized void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception exception) {
            Log.e(TAG, "Can't start camera preview due to Exception", exception);

            listener.onCameraError();
        }
    }

    /**
     * Stop the camera preview.
     */
    public synchronized void stopCameraPreview() {
        try {
            camera.stopPreview();
        } catch (Exception exception) {
            Log.i(TAG, "Exception during stopping camera preview");
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly.
     */
    public void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int displayOrientation;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        this.displayOrientation = displayOrientation;
        this.layoutOrientation  = degrees;

        camera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters.
     */
    public void setupCamera() {
        Camera.Parameters parameters = camera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        camera.setParameters(parameters);

        setFlashButtonState();
    }

    protected Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    protected Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
    }

    protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;

        for (Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) {
            listener.onCameraError();

            return sizes.get(0);
        }

        return bestSize;
    }

    /**
     * Take a picture and notify the listener once the picture is taken.
     */
    public void takePicture() {
        orientationListener.rememberOrientation();

        camera.takePicture(null, null, this);
    }

    public void swapCamera() {
        if (Camera.getNumberOfCameras() > 1 && cameraId < Camera.getNumberOfCameras() - 1) {
            cameraId = cameraId + 1;
        } else {
            cameraId = CameraInfo.CAMERA_FACING_BACK;
        }
        startCamera();
    }

    public void swapFlash() {
        Camera.Parameters params = camera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes == null || flashModes.size() == 0) {
            return;
        }

        String currentFlashMode = params.getFlashMode();
        String newFlashMode = currentFlashMode;
        if (currentFlashMode.equals(Camera.Parameters.FLASH_MODE_OFF) && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        }
        else if(currentFlashMode.equals(Camera.Parameters.FLASH_MODE_OFF) && flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_ON;
        }
        else if(currentFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO) && flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_ON;
        }
        else if(currentFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO) && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_OFF;
        }
        else if(currentFlashMode.equals(Camera.Parameters.FLASH_MODE_ON) && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_OFF;
        }
        else if(currentFlashMode.equals(Camera.Parameters.FLASH_MODE_ON) && flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            newFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        }

        params.setFlashMode(newFlashMode);
        camera.setParameters(params);
        setFlashButtonState();
    }

    /**
     * A picture has been taken.
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        int rotation = (
            displayOrientation
            + orientationListener.getRememberedOrientation()
            + layoutOrientation
        ) % 360;
        
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        	rotation = (rotation + 180) % 360;
        }
        
        if (rotation != 0) {
            Bitmap oldBitmap = bitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
            );

            oldBitmap.recycle();
        }

        bitmap = cropBitmapToSquare(bitmap);

        listener.onPictureTaken(bitmap);
    }

    private Bitmap cropBitmapToSquare(Bitmap source)
    {
        Bitmap cropped;
        int h = source.getHeight();
        int w = source.getWidth();
        if (w >= h){
            cropped = Bitmap.createBitmap(source, w/2 - h/2, 0, h, h);
        }else{
            cropped = Bitmap.createBitmap(source,0, h/2 - w/2, w, w);
        }
        return cropped;
    }

    /**
     * On camera preview surface created.
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;

        startCameraPreview();
    }

    /**
     * On camera preview surface changed.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // The interface forces us to have this method but we don't need it
        // up to now.
    }

    /**
     * On camera preview surface getting destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // We don't need to handle this case as the fragment takes care of
        // releasing the camera when needed.
    }


    protected void startCamera() {
        if (camera != null) {
            stopCamera();
        }
        camera = Camera.open(cameraId);
        startCameraPreview();
    }

    protected void stopCamera(){
      	mPreviewView.getHolder().removeCallback(this);
        stopCameraPreview();
        camera.release();
        camera = null;
    }

    private void setFlashButtonState()
    {
//        List<String> flashModes = camera.getParameters().getSupportedFlashModes();
//        ImageButton flashModeButton = (ImageButton)getActivity().findViewById(R.id.flash_mode_button);
//        if (null == flashModes || flashModes.size() == 0) {
//            flashModeButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            flashModeButton.setVisibility(View.VISIBLE);
//            if (camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
//                flashModeButton.setImageResource(R.drawable.action_bar_glyph_flash_off);
//            }
//            if (camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON)) {
//                flashModeButton.setImageResource(R.drawable.action_bar_glyph_flash_on);
//            }
//            if (camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
//                flashModeButton.setImageResource(R.drawable.action_bar_glyph_flash_auto);
//            }
//        }
    }
}
