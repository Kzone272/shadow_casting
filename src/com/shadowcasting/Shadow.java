package com.shadowcasting;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

public class Shadow extends Drawable
{
	private ArrayList<float[]> verts;
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private float[] coords;
	private float[] colors;
	
	private final float BIG = 100;
	private float x1;
	private float y1;
	private float x2;
	private float y2;

	public Shadow(float x1, float y1, float x2, float y2, float ox, float oy)
	{
		super();
		
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		
		update(ox, oy);
	}
	
	public void update(float ox, float oy)
	{
		double theta1 = Math.atan2(y1 - oy, x1 - ox);
		double theta2 = Math.atan2(y2 - oy, x2 - ox);
		
		verts = new ArrayList<float[]>();
		verts.add(new float[] { x1, y1 , 0});
		verts.add(new float[] { (float) (x1 + BIG * Math.cos(theta1)),
								(float) (y1 + BIG * Math.sin(theta1)), 0 });
		verts.add(new float[] { x2, y2, 0 });
		verts.add(new float[] { (float) (x2 + BIG * Math.cos(theta2)),
								(float) (y2 + BIG * Math.sin(theta2)), 0 });

		coords = new float[verts.size() * COORDS_PER_VERTEX];
		colors = new float[verts.size() * COORDS_PER_COLOR];
		float color[] = { 0, 0, 0, 1 };
		
		for (int i = 0; i < verts.size(); i++)
		{
			float[] vert = verts.get(i);

			for (int j = 0; j < 3; j++)
				coords[i * 3 + j] = vert[j];
			
			for (int j = 0; j < 4; j++)
				colors[i * 4 + j] = color[j];
		}

		vertexBuffer = createFloatBuffer(coords);
		colorBuffer = createFloatBuffer(colors);
	}
	
    public void draw(float[] mvpMatrix)
    {
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

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, vertexBuffer);

		// Set color for drawing the triangles
		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, colorBuffer);
		
		// Draw the triangles
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
