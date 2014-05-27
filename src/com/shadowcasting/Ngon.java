package com.shadowcasting;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

public class Ngon extends Drawable
{
	public float x, y;
	
	public FloatBuffer faceVertexBuffer;
	public FloatBuffer wallVertexBuffer;
	public FloatBuffer faceColorBuffer;
	public FloatBuffer wallColorBuffer;

	public ArrayList<float[]> verts = new ArrayList<float[]>();
	public float[] faceCoords;
	public float[] faceColors;
	public float[] wallCoords;
	public float[] wallColors;

	public Ngon(float x, float y, int n, float size, float rot, float height)
	{
    	super();
    	
		this.x = x;
		this.y = y;
		
		for (int i = 0; i < n; i++)
		{
			float theta = 2 * (float) (-1 * Math.PI / n * i + rot);
			
			float[] vert = new float[] { x + size * (float) Math.cos(theta),
										 y + size * (float) Math.sin(theta),
										 -height };
			verts.add(vert);
		}
		
		createGeometry();
	}
	
	public void createGeometry()
	{
		faceCoords = new float[verts.size() * COORDS_PER_VERTEX];
		faceColors = new float[verts.size() * COORDS_PER_COLOR];
		wallCoords = new float[(verts.size() + 1) * COORDS_PER_VERTEX * 2];
		wallColors = new float[(verts.size() + 1) * COORDS_PER_COLOR * 2];

		int drop1 = (int) Math.floor(Math.random() * 3);
		int drop2 = (int) Math.floor(Math.random() * 3);
		float color[] = { 1, 1, 1, 1 };
		color[drop1] = 0;
		color[drop2] = 0;
		
		for (int i = 0; i < verts.size(); i++)
		{
			float[] vert = verts.get(i);
			
			for (int j = 0; j < 3; j++)
				faceCoords[i * 3 + j] = vert[j];

			for (int j = 0; j < 4; j++)
				faceColors[i * 4 + j] = color[j];

			for (int j = 0; j < 6; j++)
				if (j == 5)
					wallCoords[i * 6 + j] = 0;
				else
					wallCoords[i * 6 + j] = vert[j % 3];

			for (int j = 0; j < 8; j++)
				if (j >= 4 && j <= 6)
					wallColors[i * 8 + j] = color[j % 4] / 3;
				else
					wallColors[i * 8 + j] = color[j % 4];
		}

		// Add first two vertices for the wrap-around.
		for (int i = 0; i < 6; i++)
			wallCoords[wallCoords.length - 6 + i] = wallCoords[i];
		
		for (int i = 0; i < 8; i++)
			wallColors[wallColors.length - 8 + i] = wallColors[i];

		faceVertexBuffer = createFloatBuffer(faceCoords);
		faceColorBuffer = createFloatBuffer(faceColors);
		wallVertexBuffer = createFloatBuffer(wallCoords);
		wallColorBuffer = createFloatBuffer(wallColors);
	}
	
    public void draw(float[] mvpMatrix)
    {	
		// number of coordinates per vertex
		int faceVertexCount = faceCoords.length / COORDS_PER_VERTEX;
		int wallVertexCount = wallCoords.length / COORDS_PER_VERTEX;
		
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
		                             vertexStride, wallVertexBuffer);

		// Set color for drawing the triangles
		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, wallColorBuffer);
		
		// Draw the triangles
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, wallVertexCount);
		
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, faceVertexBuffer);

		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, faceColorBuffer);
		// Draw the triangles
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, faceVertexCount);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
