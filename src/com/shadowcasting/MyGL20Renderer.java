package com.shadowcasting;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class MyGL20Renderer implements GLSurfaceView.Renderer
{
	public float xPos = 0;
	public float yPos = 0;
	public float zPos = -3;
	private float POSITION_SCALE = 0.01f;
	private float xMax = 1.5f;
	private float yMax = 3;
	
    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    //private float[] mRotationMatrix = new float[16];

	private ArrayList<Ngon> ngons;
	private ArrayList<Shadow> shadows;
	
	public void onSurfaceCreated(GL10 unused, EGLConfig config)
	{
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		
		ngons = new ArrayList<Ngon>();
		for (int i = 0; i < 50; i++)
			ngons.add(new Ngon((float) (Math.random() * (xMax + 1) * 2 - xMax - 1),
							   (float) (Math.random() * (yMax + 1) * 2 - yMax - 1),
							   (int) Math.ceil(Math.random() * 5 + 2),
							   (float) (Math.random() * 0.2 + 0.1),
							   (float) (Math.random() * 2 * Math.PI),
							   (float) (Math.random() * 1 + 0.5 )));
		createShadows();
	}
	
	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}
	
	public void onDrawFrame(GL10 unused)
	{
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		// Set the camera position (View matrix)
		Matrix.setLookAtM(mVMatrix, 0, xPos, yPos, zPos, xPos, yPos, 0, 0, 1, 0);
		//Matrix.setLookAtM(mVMatrix, 0, 0, 1, -3, -1, 0, 0, 0, 1, 0);
		//Matrix.translateM(mVMatrix, 0, xPos, yPos, -5);
		
		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		// Draw
		drawShadows();
		drawNgons();
	}
	
	public void updatePosition(float[] values)
	{
		xPos += values[0] * POSITION_SCALE;
		yPos -= values[1] * POSITION_SCALE;

		xPos = xPos > xMax ? xMax : xPos;
		xPos = xPos < -xMax ? -xMax : xPos;
		yPos = yPos > yMax ? yMax : yPos;
		yPos = yPos < -yMax ? -yMax : yPos;
		zPos = -5;
	}
	
	public void drawNgons()
	{
		for (int i = 0; i < ngons.size(); i++)
			ngons.get(i).draw(mMVPMatrix);
	}
	
	public void createShadows()
	{
		shadows = new ArrayList<Shadow>();
		for (int i = 0; i < ngons.size(); i++)
		{
			ArrayList<float[]> verts = ngons.get(i).verts;
			for (int j = 0; j < verts.size(); j++)
			{
				Shadow s = new Shadow(verts.get(j)[0],
									  verts.get(j)[1],
									  verts.get((j+1)%verts.size())[0],
									  verts.get((j+1)%verts.size())[1],
									  xPos,
									  yPos);
				shadows.add(s);
			}
		}
	}

	public void drawShadows()
	{
		for (int i = 0; i < shadows.size(); i++)
		{
			Shadow shadow = shadows.get(i); 
			shadow.update(xPos, yPos);
			shadow.draw(mMVPMatrix);
		}
	}
}
