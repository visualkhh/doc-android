/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.mixare.gui.PaintScreen;
import org.mixare.render.Matrix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MixView extends Activity implements SensorEventListener,LocationListener {

	CameraSurface camScreen;
	AugmentedView augScreen;

	static boolean isInited = false;
	static MixContext ctx;
	static PaintScreen dWindow;
	static DataView view;
	Thread downloadThread;

	float RTmp[] = new float[9];
	float R[] = new float[9];
	float I[] = new float[9];
	float grav[] = new float[3];
	float mag[] = new float[3];

	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;
	public static LocationManager locationMgr;
	boolean isGpsEnabled = false;

	int rHistIdx = 0;
	Matrix tempR = new Matrix();
	Matrix finalR = new Matrix();
	Matrix smoothR = new Matrix();
	Matrix histR[] = new Matrix[60];
	Matrix m1 = new Matrix();
	Matrix m2 = new Matrix();
	Matrix m3 = new Matrix();
	Matrix m4 = new Matrix();

	private SeekBar myZoomBar;
	private WakeLock mWakeLock;

	private boolean fError = false;
	private String fErrorTxt;
	private Exception fExeption;
	
	static boolean isZoombarVisible= false;
	static String zoomLevel;
	static int zoomProgress;
	static boolean zoomChanging=false;
	 //TAG for logging
	public static final String TAG = "Mixare";

	/*strings for GPS info assigned in Data View*/
	//public static String GPS_LOCATION;
	public static double GPS_LONGITUDE =0;
	public static double GPS_LATITUDE =0;
	public static float GPS_ACURRACY =0;
	public static String GPS_LAST_FIX="";
	public static double GPS_ALTITUDE=0;
	public static float GPS_SPEED=0;
	public static String GPS_ALL="";

	/*Vectors to store the titles and URLs got from Json for the alternative list view */
	public Vector<String> listDataVector;
	public Vector<String> listURL;

	/*string to name & access the preference file in the internal storage*/
    public static final String PREFS_NAME = "MyPrefsFileForMenuItems";
    	
	public void doError(Exception ex1) {
		if (!fError) {
			fError = true;
			fErrorTxt = ex1.toString();
			fExeption = ex1;
			
			setErrorDialog();
			
			ex1.printStackTrace();
			try {
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		try {
			augScreen.invalidate();
		} catch (Exception ignore) {

		}
	}

	public void killOnError() throws Exception {
		if (fError)
			throw new Exception();
	}
	
	public void repaint(){
		view = new DataView(ctx);
		dWindow = new PaintScreen();
		SetZoomLevel();
	}

	public void setErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(view.CONNECITON_ERROR_DIALOG_TEXT));
        builder.setCancelable(false);
        
        /*Retry*/
        builder.setPositiveButton(view.CONNECITON_ERROR_DIALOG_BUTTON1, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		fError=false;
               //TODO improve
            	try {
            		repaint();	       		
            	}
            	catch(Exception ex){
            		doError(ex);
            	}
            }
        });
        /*Open settings*/
        builder.setNeutralButton(view.CONNECITON_ERROR_DIALOG_BUTTON2, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS); 
				startActivityForResult(intent1, 42);

            	//TODO back to camera view after seeing settings
            }
        });
        /*Close application*/
        builder.setNegativeButton(view.CONNECITON_ERROR_DIALOG_BUTTON3, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
            	 /*leave the app but it will still exist in memory*/
            	 //finish();
            	 /*the app will be definitely killed*/
                 System.exit(0);
              }
        });
        AlertDialog alert = builder.create();
        alert.show();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
			locationMgr=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10, this);

			killOnError();
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			/*Get the preference file PREFS_NAME stored in the internal memory of the phone*/
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    SharedPreferences.Editor editor = settings.edit();

		    myZoomBar = new SeekBar(this);
			myZoomBar.setVisibility(View.INVISIBLE);
			myZoomBar.setMax(100);
			myZoomBar.setProgress(settings.getInt("zoomLevel", 65));
			myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
			myZoomBar.setVisibility(View.INVISIBLE);
			isZoombarVisible=false;

			FrameLayout FL = new FrameLayout(this);

			FL.setMinimumWidth(3000);
			FL.addView(myZoomBar);
			FL.setPadding(10, 0, 10, 10);

			
			//���� �߰� ��񺰷� ī�޶� ������ �ٸ���...
			//ī�޶����
	    	Display display = ((WindowManager) getApplicationContext().getSystemService(getApplicationContext().WINDOW_SERVICE)).getDefaultDisplay(); 
	    	Log.d("displayWidth", "w:" + display.getWidth());
	    	Log.d("displayHeight", "h:" + display.getHeight());
			 
			
			camScreen = new CameraSurface(this,display.getWidth(),display.getHeight());
			augScreen = new AugmentedView(this);
			setContentView(camScreen);

			addContentView(augScreen, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			addContentView(FL, new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM));
			
			if (!isInited) {
				ctx = new MixContext(this);

				ctx.downloadManager = new DownloadManager(ctx);
				
				 
				dWindow = new PaintScreen();
				view = new DataView(ctx);
				
				/*set the radius in data view to the last selected by the user*/
				SetZoomLevel(); 
				isInited = true;		
			}
			
		    /*check if the application is launched for the first time*/
		    if(settings.getBoolean("firstAccess",false)==false){
		    	//if FALSE it is the first time and the license agreements are shown before starting
		    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
				builder1.setMessage(getString(view.LICENSE_TEXT));
				builder1.setNegativeButton(getString(view.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
		        		dialog.dismiss();
		            }
		        });
				AlertDialog alert1 = builder1.create();
				alert1.setTitle(getString(view.LICENSE_TITLE));
			    alert1.show();
			    editor.putBoolean("firstAccess", true);
			    editor.commit();
			} 

			if(ctx.isActualLocation()==false){
				Toast.makeText( this, getString(view.CONNECITON_GPS_DIALOG_TEXT), Toast.LENGTH_LONG ).show();
			}		

		} catch (Exception ex) {
			doError(ex);
		}
	}
	

	@Override
	protected void onPause() {
		super.onPause();

		try {
			this.mWakeLock.release();

			try {
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ignore) {
			}
			try {
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ignore) {
			}
			sensorMgr = null;

			try {
				locationMgr.removeUpdates(this);
			} catch (Exception ignore) {
			}
			try {
				locationMgr = null;
			} catch (Exception ignore) {
			}

			try {
				ctx.downloadManager.stop();
			} catch (Exception ignore) {
			}

			if (fError) {
				finish();
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			this.mWakeLock.acquire();//Ȧ��Ǭ��

			killOnError();
			ctx.mixView = this;
			view.doStart();
			view.clearEvents();

			double angleX, angleY;

			angleX = Math.toRadians(-90);
			m1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
					.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math
					.cos(angleX));

			angleX = Math.toRadians(-90);
			angleY = Math.toRadians(-90);
			m2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
					.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math
					.cos(angleX));
			m3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
					0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math
					.cos(angleY));

			m4.toIdentity();

			for (int i = 0; i < histR.length; i++) {
				histR[i] = new Matrix();
			}

			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0) {
				sensorGrav = sensors.get(0);
			}

			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0) {
				sensorMag = sensors.get(0);
			}


			sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_GAME);
			sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_GAME);

			try { //�������� ��������....
				Criteria c = new Criteria();

				c.setAccuracy(Criteria.ACCURACY_FINE);
				//c.setBearingRequired(true);
				
				locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10, this);

				String bestP = locationMgr.getBestProvider(c, true);
				isGpsEnabled = locationMgr.isProviderEnabled(bestP);


				/*defaulting to our place*/
				Location hardFix = new Location("reverseGeocoded");
				hardFix.setLatitude(46.47122383117541);
				hardFix.setLongitude(11.260278224944742);
				hardFix.setAltitude(300);
				
/*				hardFix.setLatitude(48.19616);
				hardFix.setLongitude(16.36886);
				hardFix.setAltitude(200); */

				
				try {
					ctx.curLoc = new Location(locationMgr.getLastKnownLocation(bestP));
					System.out.println("GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOD");
				} catch (Exception ex2) {
					System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
					ctx.curLoc = new Location(hardFix);
				}

				GeomagneticField gmf = new GeomagneticField((float) ctx.curLoc
						.getLatitude(), (float) ctx.curLoc.getLongitude(),
						(float) ctx.curLoc.getAltitude(), System
						.currentTimeMillis());

				angleY = Math.toRadians(-gmf.getDeclination());
				m4.set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math
						.sin(angleY), 0f, (float) Math.cos(angleY));
				ctx.declination = gmf.getDeclination();
				
			} catch (Exception ex) {
				Log.d("mixare", "GPS Initialize Error", ex);
			}

			downloadThread = new Thread(ctx.downloadManager);
			downloadThread.start();
			
		} catch (Exception ex) {
			doError(ex);

			try {
				if (sensorMgr != null) {
					sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
				}

				if (locationMgr != null) {
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}

				if (ctx != null) {
					if (ctx.downloadManager != null)
						ctx.downloadManager.stop();

				}
			} catch (Exception ignore) {

			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			int base = Menu.FIRST;
			/*define the first*/
			MenuItem item1 =menu.add(base, base, base, getString(view.MENU_ITEM_1)); 
			MenuItem item2 =menu.add(base, base+1, base+1,  getString(view.MENU_ITEM_2)); 
			MenuItem item3 =menu.add(base, base+2, base+2,  getString(view.MENU_ITEM_3));
			MenuItem item4 =menu.add(base, base+3, base+3,  getString(view.MENU_ITEM_4));
			MenuItem item5 =menu.add(base, base+4, base+4,  getString(view.MENU_ITEM_5));
			MenuItem item6 =menu.add(base, base+5, base+5,  getString(view.MENU_ITEM_6));
			MenuItem item7 =menu.add(base, base+6, base+6,  getString(view.MENU_ITEM_7));

			/*assign icons to the menu items*/
			item1.setIcon(android.R.drawable.ic_menu_edit);
			item2.setIcon(android.R.drawable.ic_menu_view);
			item3.setIcon(android.R.drawable.ic_menu_mapmode);
			item4.setIcon(android.R.drawable.ic_menu_zoom);
			item5.setIcon(android.R.drawable.ic_menu_search);
			item6.setIcon(android.R.drawable.ic_menu_info_details);
			item7.setIcon(android.R.drawable.ic_menu_share);
			
			return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			/*Data sources*/
			case 1:		
				if(view.isLauncherStarted==false){
					MixListView.setList(1);
					Intent intent = new Intent(MixView.this, MixListView.class); 
					startActivityForResult(intent, 40);
				}
				else{
					Toast.makeText( this, getString(view.OPTION_NOT_AVAILABLE_STRING_ID), Toast.LENGTH_LONG ).show();		
				}
				break;
			/*List view*/
			case 2:
				MixListView.setList(2);
				/*if the list of titles to show in alternative list view is not empty*/
				if(view.jLayer.markers.size()>0){
					Intent intent1 = new Intent(MixView.this, MixListView.class); 
					startActivityForResult(intent1, 42);
				}
				/*if the list is empty*/
				else{
					Toast.makeText( this, view.EMPTY_LIST_STRING_ID, Toast.LENGTH_LONG ).show();			
				}
				break;
			/*Map View*/
			case 3:
				Intent intent2 = new Intent(MixView.this, MixMap.class); 
				startActivityForResult(intent2, 20);
				break;
			/*zoom level*/
			case 4:
				myZoomBar.setVisibility(View.VISIBLE);
				zoomProgress = myZoomBar.getProgress();
				isZoombarVisible=true;	
				break;
			/*Search*/
			case 5:
				Toast.makeText( this, getString(view.OPTION_NOT_AVAILABLE_STRING_ID), Toast.LENGTH_LONG ).show();				
				break;
			/*GPS Information*/
			case 6:
				ctx.setCurrentGPSInfo();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(view.GENERAL_INFO_TEXT)+ "\n\n" +
							getString(view.GPS_LONGITUDE) + GPS_LONGITUDE + "\n" +
							getString(view.GPS_LATITUDE) + GPS_LATITUDE + "\n" +
							getString(view.GPS_ALTITUDE)+ GPS_ALTITUDE + "m\n" +
							getString(view.GPS_SPEED) + GPS_SPEED + "km/h\n" +
							getString(view.GPS_ACCURACY) + GPS_ACURRACY + "m\n" +
							getString(view.GPS_LAST_FIX) + GPS_LAST_FIX + "\n");
				builder.setNegativeButton(getString(view.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
		        		dialog.dismiss();
		            }
		        });
		        AlertDialog alert = builder.create();
				alert.setTitle(getString(view.GENERAL_INFO_TITLE));
		        alert.show();
				break;
			/*Case 6: license agreements*/
			case 7:
				AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
				builder1.setMessage(getString(view.LICENSE_TEXT));	
				/*Retry*/
				builder1.setNegativeButton(getString(view.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
		        		dialog.dismiss();
		            }
		        });
				AlertDialog alert1 = builder1.create();
				alert1.setTitle(getString(view.LICENSE_TITLE));
			    alert1.show();
				break;
			
		}
		return true;
	}
public float calcZoomLevel(){
		
		int myZoomLevel = myZoomBar.getProgress();
		float myout = 5;
		
		if (myZoomLevel <= 26) {
			myout = myZoomLevel / 25f;
		} else if (25 < myZoomLevel && myZoomLevel < 50) {
			myout = (1 + (myZoomLevel - 25)) * 0.38f;
		} 
				else if (25== myZoomLevel) {
					myout = 1;
				} 
				else if (50== myZoomLevel) {
					myout = 10;
				} 
		else if (50 < myZoomLevel && myZoomLevel < 75) {
			myout = (10 + (myZoomLevel - 50)) * 0.83f;
		} else {
			myout = (30 + (myZoomLevel - 75) * 2f);
		}
		
		/*Twitter Json file not available for radius <1km 
		 *smallest radius is set to 1km*/
		if(MixListView.getDataSource().equals("Twitter")&&myZoomBar.getProgress()<100){
			myout++;
		}
	
		return myout;
	}

	private void SetZoomLevel() {
		float myout = calcZoomLevel();

		view.radius =myout;

		myZoomBar.setVisibility(View.INVISIBLE);
		isZoombarVisible=false;
		zoomLevel = String.valueOf(myout);
		
		view.doStart();
		view.clearEvents();
		downloadThread = new Thread(ctx.downloadManager);
		downloadThread.start();

	};

	private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		Toast t;

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			float myout = calcZoomLevel();
					
			zoomLevel = String.valueOf(myout);
			zoomProgress = myZoomBar.getProgress();

			t.setText("Radius: " + String.valueOf(myout));
			t.show();
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			Context ctx = seekBar.getContext();
			t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
			zoomChanging= true;

		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    /*store the zoom range of the zoom bar selected by the user*/
		    editor.putInt("zoomLevel", myZoomBar.getProgress());
		    editor.commit();
			myZoomBar.setVisibility(View.INVISIBLE);
			isZoombarVisible=false;
			zoomChanging= false;

			myZoomBar.getProgress();
			
			t.cancel();
			SetZoomLevel();
		}

	};


	public void onSensorChanged(SensorEvent evt) {
		try {
			killOnError();

			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];

				augScreen.postInvalidate();
			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];

				augScreen.postInvalidate();
			}

			SensorManager.getRotationMatrix(RTmp, I, grav, mag);
			SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);

			tempR.set(R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7],
					R[8]);

			finalR.toIdentity();
			finalR.prod(m4);
			finalR.prod(m1);
			finalR.prod(tempR);
			finalR.prod(m3);
			finalR.prod(m2);
			finalR.invert(); 

			histR[rHistIdx].set(finalR);
			rHistIdx++;
			if (rHistIdx >= histR.length)
				rHistIdx = 0;

			smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
			for (int i = 0; i < histR.length; i++) {
				smoothR.add(histR[i]);
			}
			smoothR.mult(1 / (float) histR.length);

			synchronized (ctx.rotationM) {
				ctx.rotationM.set(smoothR);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == MotionEvent.ACTION_UP) {
				view.clickEvent(xPress, yPress);
			}

			return true;
		} catch (Exception ex) {
			//doError(ex);
			ex.printStackTrace();
			return super.onTouchEvent(me);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (view.state.detailsView) {
					view.keyEvent(keyCode);
					view.state.detailsView = false;
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			}
			else {
				view.keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return super.onKeyDown(keyCode, event);
		}
	}

	public void onProviderDisabled(String provider) {
		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onProviderEnabled(String provider) {
		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void onLocationChanged(Location location) {
		try {
			killOnError();
			if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
				synchronized (ctx.curLoc) {
					ctx.curLoc = location;
				}
				isGpsEnabled = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}

class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
	MixView app;
	SurfaceHolder holder;
	Camera camera;
	int width,height;
	CameraSurface(Context context,int width, int height) {
		super(context);
		this.width = width;
		this.height = height;

		try {
			app = (MixView) context;

			holder = getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (Exception ex) {

		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}

			camera = Camera.open();
			camera.setPreviewDisplay(holder);
		} catch (Exception ex) {
			try {
				if (camera != null) {
					try {
						camera.stopPreview();
					} catch (Exception ignore) {
					}
					try {
						camera.release();
					} catch (Exception ignore) {
					}
					camera = null;
				}
			} catch (Exception ignore) {

			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			try {
				List<Camera.Size> supportedSizes = null;
				supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

				Iterator<Camera.Size> itr = supportedSizes.iterator(); 
				while(itr.hasNext()) {
					Camera.Size element = itr.next(); 
					element.width -= w;
					element.height -= h;
				} 
				
//				Collections.sort(supportedSizes, new ResolutionsOrder());
				//TODO improve algorithm
				int preferredSizeIndex=0;
				int checkWidth =0;
				int bestDistance = Integer.MAX_VALUE;
				for (int i = 0; i < supportedSizes.size()-1; i++) {		
					if(supportedSizes.get(i).width==0){
						preferredSizeIndex = i;
					}
					else{						
						if(supportedSizes.get(i).width <0)
							checkWidth =(supportedSizes.get(i).width)*(-1);
						else 
							checkWidth = supportedSizes.get(i).width;
						
						if(checkWidth < bestDistance){
							bestDistance = checkWidth;
							preferredSizeIndex = i;
						}
					}
				}
				
				parameters.setPreviewSize(this.width, this.height);
//				parameters.setPreviewSize(w + supportedSizes.get(preferredSizeIndex).width, h + supportedSizes.get(preferredSizeIndex).height);
				//parameters.setPreviewSize(w + supportedSizes.get(supportedSizes.size()-1).width, h + supportedSizes.get(supportedSizes.size()-1).height);
			} catch (Exception ex) {
				parameters.setPreviewSize(480 , 320);
			}

			camera.setParameters(parameters);
			camera.startPreview();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


}

//class ResolutionsOrder implements java.util.Comparator<Camera.Size> {
//	public int compare(Camera.Size left, Camera.Size right) {
//
//		return Float.compare(left.width + left.height, right.width + right.height);
//	}
//}

class AugmentedView extends View {
	MixView app;

	public AugmentedView(Context context) {
		super(context);

		try {
			app = (MixView) context;

			app.killOnError();
		} catch (Exception ex) {
			app.doError(ex);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
//			if (app.fError) {
//
//				Paint errPaint = new Paint();
//				errPaint.setColor(Color.RED);
//				errPaint.setTextSize(16);
//				
//				/*Draws the Error code*/
//				canvas.drawText("ERROR: ", 10, 20, errPaint);
//				canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
//
//				return;
//			}

			app.killOnError();

			MixView.dWindow.setWidth(canvas.getWidth());
			MixView.dWindow.setHeight(canvas.getHeight());
			MixView.dWindow.setCanvas(canvas);

			
			if (!MixView.view.isInited()) {
				MixView.view.init(MixView.dWindow.getWidth(),
						MixView.dWindow.getHeight());
			}
			if(MixView.isZoombarVisible){
				Paint zoomPaint = new Paint();
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				if(MixListView.getDataSource().equals("Twitter")){
					startKM = "1km";
				}
				canvas.drawText(startKM, canvas.getWidth()/100*4, canvas.getHeight()/100*85, zoomPaint);
				canvas.drawText(endKM, canvas.getWidth()/100*99+25, canvas.getHeight()/100*85, zoomPaint);
				
				int height= canvas.getHeight()/100*85;
				if(MixView.zoomProgress >92||MixView.zoomProgress <6){
					height = canvas.getHeight()/100*80;
				}
				canvas.drawText(MixView.zoomLevel,  (canvas.getWidth())/100*MixView.zoomProgress+20, height, zoomPaint);
			}

			MixView.view.draw(MixView.dWindow);
		} catch (Exception ex) {
			app.doError(ex);
		}
	}
}
