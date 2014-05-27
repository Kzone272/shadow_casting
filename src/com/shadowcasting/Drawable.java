package com.shadowcasting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class Drawable {

	public int mProgram;

	int COORDS_PER_VERTEX = 3;
	int COORDS_PER_COLOR = 4;
	int vertexStride = COORDS_PER_VERTEX * 4; // bytes per float
	int colorStride = COORDS_PER_COLOR * 4; // bytes per float

    public final String vertexShaderCode =
		// This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 aPosition;" +
        "attribute vec4 aColor;" +
        "varying vec4 vColor;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = uMVPMatrix * aPosition;" +
        "  vColor = aColor;" +
        "}";

    public final String fragmentShaderCode =
		"precision mediump float;" +
		"varying vec4 vColor;" +
		"void main() {" +
		"  gl_FragColor = vColor;" +
		"}";
    
	public Drawable()
	{
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(mProgram);
	}
	
	public static FloatBuffer createFloatBuffer(float[] floats)
	{
		ByteBuffer buff = ByteBuffer.allocateDirect(floats.length * 4);
		buff.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuff = buff.asFloatBuffer();
		floatBuff.put(floats);
		floatBuff.position(0);
		return floatBuff;
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
