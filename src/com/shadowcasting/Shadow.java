package com.shadowcasting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.graphics.PointF;
import android.opengl.GLES20;

public class Shadow
{
	public ArrayList<float[]> verts;
	private FloatBuffer vertexBuffer;
	private float coords[];
	
	private float BIG = 100;
	private float x1;
	private float y1;
	private float x2;
	private float y2;

	public Shadow(float x1, float y1, float x2, float y2, float ox, float oy)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		
		update(ox, oy);
		createShaders();
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

		coords = new float[verts.size() * 3];
		
		for (int i = 0; i < verts.size(); i++)
		{
			float[] vert = verts.get(i);
			coords[i*3] = vert[0];
			coords[i*3+1] = vert[1];
			coords[i*3+2] = vert[2];
		}
		
		// Number of coordinates * size of float (4 bytes)
		ByteBuffer buff = ByteBuffer.allocateDirect(coords.length * 4);
		buff.order(ByteOrder.nativeOrder());
		vertexBuffer = buff.asFloatBuffer();
		vertexBuffer.put(coords);
		vertexBuffer.position(0);
	}
	
    public void draw(float[] mvpMatrix)
    {	
		// number of coordinates per vertex
		int COORDS_PER_VERTEX = 3;
		int vertexCount = coords.length / COORDS_PER_VERTEX;
		int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
		float color[] = { 0, 0, 0, 1 };
		
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);
		
		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		
		// Set color for drawing the triangles
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		// get handle to shape's transformation matrix
	    int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
		                             GLES20.GL_FLOAT, false,
		                             vertexStride, vertexBuffer);
		
		// Draw the triangle
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
		
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
	
    private final String vertexShaderCode =
		// This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private final String fragmentShaderCode =
		"precision mediump float;" +
		"uniform vec4 vColor;" +
		"void main() {" +
		"  gl_FragColor = vColor;" +
		"}";

	private int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	
	private void createShaders()
	{	
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
