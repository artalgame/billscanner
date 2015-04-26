package com.artal.checkscanner;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public final static String DEBUG_TAG = MainActivity.class.getName();
	private Camera camera;

	private int backFacingCameraID = -1;
	private int frontFacingCameraID = -1;

	private int cameraId = 0;
	private CameraPreview cameraPreview;
	private ImageButton makePhotoButton;
	private ImageButton changeCameraButton;
	private ImageButton flashButton;
	private boolean flashON;
	private boolean isFrontCamera = false;

	private int selectedTemplateID;
	protected Bitmap templateImageViewBitmap;
	private ImageView templateImageView;
	private FrameLayout preview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_make_photo_layout);

		templateImageView = (ImageView) findViewById(R.id.templateImageView);

		if (getActionBar() != null) {
			getActionBar().hide();
		}

		setupMakePhotoButton();
		setupChangeCameraButton();
		setupFlashButton();
	}

	private void setupFlashButton() {
		flashButton = (ImageButton) findViewById(R.id.flashCameraButton);
		flashButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (camera != null) {
					try {
						Parameters params = camera.getParameters();
						if (flashON) {
							flashON = false;
							params.setFlashMode(Parameters.FLASH_MODE_OFF);
						} else {
							flashON = true;
							boolean findFlashOn = false;
							for (String mode : params.getSupportedFlashModes()) {
								if (mode == Parameters.FLASH_MODE_ON) {
									params.setFlashMode(Parameters.FLASH_MODE_ON);
									findFlashOn = true;
									break;
								}
							}
							if (!findFlashOn) {
								params.setFlashMode(Parameters.FLASH_MODE_TORCH);
							}
						}
						camera.setParameters(params);
					} catch (Exception ex) {
						ex.printStackTrace();
						Toast.makeText(MainActivity.this,
								"Не удалось включить вспышку",
								Toast.LENGTH_SHORT).show();
					}
				}
			}

		});

	}

	private void setupChangeCameraButton() {
		changeCameraButton = (ImageButton) findViewById(R.id.changeCameraButton);
		changeCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (backFacingCameraID > -1 && frontFacingCameraID > -1) {
					int prefareCameraId = -1;
					camera.stopPreview();
					camera.release();
					if (cameraId == backFacingCameraID) {
						prefareCameraId = frontFacingCameraID;
						isFrontCamera = true;
					} else {
						prefareCameraId = backFacingCameraID;
						isFrontCamera = false;
					}
					if (prefareCameraId != -1) {
						camera = loadCamera(prefareCameraId);
						cameraPreview.setNewCamera(camera);
					}
				}
			}
		});

	}

	private void setupMakePhotoButton() {
		makePhotoButton = (ImageButton) findViewById(R.id.makePhotoButton);
		makePhotoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Map<String, String> articleParams = new HashMap<String, String>();

				articleParams.put("TEMPLATE_NUMBER",
						String.valueOf(selectedTemplateID));

				camera.takePicture(null, null, new PhotoHandler(
						MainActivity.this, selectedTemplateID, isFrontCamera));
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		preview = (FrameLayout) findViewById(R.id.camera_preview);

		// FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
		// preview.getLayoutParams();

		/*
		 * params.height = getResources().getDisplayMetrics().heightPixels;
		 * params.width = (int)(params.height *
		 * ProjectConstants.TEMPLATE_ASPECT_RATIO); params.leftMargin =
		 * (getResources().getDisplayMetrics().widthPixels - params.width)/2;
		 * preview.setLayoutParams(params);
		 */

		// params.width = getResources().getDisplayMetrics().widthPixels;
		// params.height = (int)(params.width *
		// 1/ProjectConstants.TEMPLATE_ASPECT_RATIO);
		// params.topMargin = (getResources().getDisplayMetrics().heightPixels -
		// params.height)/2;
		// preview.setLayoutParams(params);
		//

		/*
		 * FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams)
		 * templateImageView.getLayoutParams(); imageParams.height =
		 * getResources().getDisplayMetrics().heightPixels; imageParams.width =
		 * (int)(imageParams.height * ProjectConstants.TEMPLATE_ASPECT_RATIO);
		 * imageParams.leftMargin =
		 * (getResources().getDisplayMetrics().widthPixels -
		 * imageParams.width)/2; templateImageView.setLayoutParams(imageParams);
		 */

		// FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams)
		// templateImageView.getLayoutParams();
		// imageParams.width = getResources().getDisplayMetrics().widthPixels;
		// imageParams.height = (int)(imageParams.width * 1/
		// ProjectConstants.TEMPLATE_ASPECT_RATIO);
		// imageParams.topMargin =
		// (getResources().getDisplayMetrics().heightPixels -
		// imageParams.height)/2;
		// templateImageView.setLayoutParams(imageParams);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// int prefareCameraId = getIntent().getIntExtra(
			// ProjectConstants.PREFARED_CAMERA_INTENT, -1);
			int prefareCameraId = 0;
			camera = loadCamera(prefareCameraId);

			try {
				camera.unlock();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				camera.reconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (camera != null) {
				// Create our Preview view and set it as the content of our
				// activity.
				cameraPreview = new CameraPreview(this, camera);
				preview.removeAllViews();
				preview.addView(cameraPreview);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private Camera loadCamera(int prefareCameraId) {
		if (prefareCameraId == -1) {
			findCameras();
			if (!getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA)) {
				Toast.makeText(this, "No camera on this device",
						Toast.LENGTH_LONG).show();
			} else {
				if (backFacingCameraID > -1) {
					cameraId = backFacingCameraID;
				} else {
					if (frontFacingCameraID > -1) {
						cameraId = frontFacingCameraID;
					} else {
						finish();
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
					}
				}
			}
		} else {
			cameraId = prefareCameraId;
		}

		if (cameraId == frontFacingCameraID) {
			isFrontCamera = true;
		} else {
			isFrontCamera = false;
		}
		Camera camera = Camera.open(cameraId);
		Camera.Parameters parameters = camera.getParameters();
		parameters.set("orientation", "portrait");
		parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(parameters);
		camera.setDisplayOrientation(90);

		 //setupRightPreviewAndPictureSizes(camera);

		return camera;
	}

//	 private void setupRightPreviewAndPictureSizes(Camera camera) {
//	 if(camera!=null){
//	 Parameters params = camera.getParameters();
//	 //Find right preview size
//	//this size should be as TEMPLATE_ASPECT_RATIO and as more similar to screen coordinates as can.
//	 if((Math.abs(
//	 (float)params.getPreviewSize().height/params.getPreviewSize().width) -
//	 1 / ProjectConstants.TEMPLATE_ASPECT_RATIO)>0.01)
//	 {
//	findRightPreviewSize(camera);
//	 }
//	
//	// //Setup right output camera picture size
//	// //setup TEMPLATE_ASPECT_RATIO the nearest to templateBitmap size
//	setupRightPictureSize(camera);
//	 }
//	 }

	// private void setupRightPictureSize(Camera camera) {
	// Point templateBitmapSize = BitmapHelper.getTemplateBitmapSize(this,
	// selectedTemplateID);
	//
	// Parameters params = camera.getParameters();
	// for(Size pictureSize: params.getSupportedPictureSizes()){
	// if((Math.abs((float)pictureSize.height/pictureSize.width) - 1 /
	// ProjectConstants.TEMPLATE_ASPECT_RATIO)<0.01){
	// if(pictureSize.height<=templateBitmapSize.y){
	// params.setPictureSize(pictureSize.width, pictureSize.height);
	// camera.setParameters(params);
	// return;
	// }
	// }
	// }
	// }

	// private void findRightPreviewSize(Camera camera) {
	// Parameters params = camera.getParameters();
	//
	// for(Size previewSize: params.getSupportedPreviewSizes()){
	// if(Math.abs((float)previewSize.height)/previewSize.width -
	// 1/ProjectConstants.TEMPLATE_ASPECT_RATIO < 0.01){
	// params.setPreviewSize(previewSize.width, previewSize.height);
	// camera.setParameters(params);
	// return;
	// }
	// }
	// }

	private void findCameras() {
		int numberOfCameras = Camera.getNumberOfCameras();

		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);

			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				frontFacingCameraID = i;
				Log.d(DEBUG_TAG, "Front camera found");
			} else {
				if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
					backFacingCameraID = i;
					Log.d(DEBUG_TAG, "Back camera found");
				}
			}
		}
	}

	@Override
	protected void onPause() {
		if (camera != null) {
			try {
				camera.stopPreview();
				camera.release();
				camera = null;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		preview.removeAllViews();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		super.onBackPressed();

	}

	protected void onPostExecute(Void params) {
		if (templateImageView != null && templateImageViewBitmap != null) {
			templateImageView.setImageBitmap(templateImageViewBitmap);
			templateImageView.setVisibility(View.VISIBLE);
		}
	}

}
