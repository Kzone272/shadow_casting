package com.shadowcasting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.graphics.PointF;
import android.opengl.GLES20;

public class Ngon extends PointF
{
	public FloatBuffer faceVertexBuffer;
	public FloatBuffer wallVertexBuffer;
	public FloatBuffer faceColorVertexBuffer;
	public FloatBuffer wallColorVertexBuffer;

	public ArrayList<float[]> verts = new ArrayList<float[]>();
	public float faceCoords[];
	public float wallCoords[];
	public float faceColors[];
	public float wallColors[];

	public Ngon(float x, float y, int n, float size, float rot, float height)
	{
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
		createBuffers();
	}

	private int mProgram;
	
    public void draw(float[] mvpMatrix)
    {	
		// number of coordinates per vertex
		int COORDS_PER_VERTEX = 3;
		int COORDS_PER_COLOR = 4;
		int faceVertexCount = faceCoords.length / COORDS_PER_VERTEX;
		int wallVertexCount = wallCoords.length / COORDS_PER_VERTEX;
		int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
		int colorStride = COORDS_PER_COLOR * 4; // bytes per vertex
		float[] color = { 1, 1, 1, 1 };
		
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);
		
		// get handle to vertex shader's vPosition member
		int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// get handle to fragment shader's vColor member
		int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");

		// get handle to shape's transformation matrix
	    int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		// Set color for drawing the triangles
		GLES20.glEnableVertexAttribArray(mColorHandle);

		// Set color for drawing the triangles
		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, wallColorVertexBuffer);
		
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, wallVertexBuffer);
		
		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, wallVertexCount);

		GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             colorStride, faceColorVertexBuffer);
		
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, faceVertexBuffer);
		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, faceVertexCount);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
	
    private final String vertexShaderCode =
		// This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "attribute vec4 aColor;" +
        "varying vec4 vColor;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  vColor = aColor;" +
        "}";

    private final String fragmentShaderCode =
		"precision mediump float;" +
		"varying vec4 vColor;" +
		"void main() {" +
		"  gl_FragColor = vColor;" +
		"}";
	
	private void createBuffers()
	{
		faceCoords = new float[verts.size() * 3];
		wallCoords = new float[(verts.size() + 1) * 6];

		faceColors = new float[verts.size() * 4];
		wallColors = new float[(verts.size() + 1) * 8];

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
		
		// Number of coordinates * size of float (4 bytes)
		ByteBuffer buff = ByteBuffer.allocateDirect(faceCoords.length * 4);
		buff.order(ByteOrder.nativeOrder());
		faceVertexBuffer = buff.asFloatBuffer();
		faceVertexBuffer.put(faceCoords);
		faceVertexBuffer.position(0);
		
		buff = ByteBuffer.allocateDirect(wallCoords.length * 4);
		buff.order(ByteOrder.nativeOrder());
		wallVertexBuffer = buff.asFloatBuffer();
		wallVertexBuffer.put(wallCoords);
		wallVertexBuffer.position(0);

		buff = ByteBuffer.allocateDirect(faceColors.length * 4);
		buff.order(ByteOrder.nativeOrder());
		faceColorVertexBuffer = buff.asFloatBuffer();
		faceColorVertexBuffer.put(faceColors);
		faceColorVertexBuffer.position(0);
		
		buff = ByteBuffer.allocateDirect(wallColors.length * 4);
		buff.order(ByteOrder.nativeOrder());
		wallColorVertexBuffer = buff.asFloatBuffer();
		wallColorVertexBuffer.put(wallColors);
		wallColorVertexBuffer.position(0);
		
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(mProgram);   
	}

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
