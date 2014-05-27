package com.shadowcasting;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

public class Radial extends Drawable
{
	public float x, y;
	
	public FloatBuffer vertexBuffer;
	public FloatBuffer colorBuffer;

	public ArrayList<float[]> verts = new ArrayList<float[]>();
	public float[] coords;
	public float[] colors;

	public Radial(float size)
	{
    	super();
		
    	int segs = 50;
    	
    	verts.add(new float[] { 0, 0, 0 });
		for (int i = 0; i <= segs; i++)
		{
			float theta = 2 * (float) (-1 * Math.PI / segs * i);
			
			float[] vert = new float[] { size * (float) Math.cos(theta),
										 size * (float) Math.sin(theta),
										 0.1f };
			verts.add(vert);
		}
		
		update(0, 0);
	}
	
	public void update(float x, float y)
	{
		coords = new float[verts.size() * COORDS_PER_VERTEX];
		colors = new float[verts.size() * COORDS_PER_COLOR];
		float[] pos = { x, y, 0 };
		
		for (int i = 0; i < verts.size(); i++)
		{
			float[] vert = verts.get(i);
			
			for (int j = 0; j < 3; j++)
				coords[i * 3 + j] = vert[j] + pos[j];

			for (int j = 0; j < 4; j++)
				if (i == 0)
					colors[i * 4 + j] = 1;
				else
					colors[i * 4 + j] = 0;
		}

		vertexBuffer = createFloatBuffer(coords);
		colorBuffer = createFloatBuffer(colors);
	}
	
    public void draw(float[] mvpMatrix)
    {	
		// number of coordinates per vertex
		int vertexCount = coords.length / COORDS_PER_VERTEX;
		
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to shape's transformation matrix
	    int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		
		// get handle to vertex shader's vPosition member
		int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");

		// get handle to fragment shader's vColor member
		int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");

	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		// Set color for drawing the triangles
		GLES20.glEnableVertexAttribArray(mColorHandle);
		
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, vertexBuffer);

		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, colorBuffer);
		// Draw the triangles
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
