package com.shadowcasting;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class OpenGLES20 extends Activity
{
	private GLSurfaceView mGLView;
	public MyGL20Renderer mRenderer;
	
	private SensorManager sensorManager;
	private Sensor gyroscope;
	private SensorEventListener listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mGLView = new GLSurfaceView(this);
		setContentView(mGLView);

		mGLView.setEGLContextClientVersion(2);
		mRenderer = new MyGL20Renderer();
		mGLView.setRenderer(mRenderer);
		
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

	    
	    listener = new SensorEventListener() {
			
	    	int POINTS = 50;
		    int POS = 0;
		    float[][] prev = new float[POINTS][3];
		    
		    float angle = 0;
	    	
			@Override
			public void onSensorChanged(SensorEvent event)
			{
				
				prev[POS] = event.values;
				POS = (POS + 1) % POINTS;

				float[] smooth = new float[3];
				for (int i = 0; i < 3; i++)
				{
					for (int j = 0; j < POINTS; j++)
						smooth[i] += prev[j][i];
					smooth[i] /= POINTS;
				}
				
				mRenderer.updatePosition(smooth);
//				
//				float[] move = new float[3];
//
//				move[0] = (float) (0.5 * Math.cos(angle));
//				move[1] = (float) (0.5 *  Math.sin(angle));
//				move[2] = 0;
//				
//				angle += Math.PI / 75;
//				
//				mRenderer.updatePosition(move);
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{
				
			}
		};
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
	    sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		sensorManager.unregisterListener(listener);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
