package com.JoakimAndersson.twentyfourtyeight;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Block {

	private int vertexCubeArrayObjectId;
	private int vboCubeVertexHandle;
	private int vboCubeColorHandle;

	final private int amountOfCubeVertices = 8;
	final private int vertexSize = 4;
	final private int colorSize = 4;
	private int cubeIndicesCount = 0;
	private int vboCubeIndecesId = 0;
	final private float positionMult = 1.2f;

	private Matrix4f modelCubeMatrix = null;
	//TODO: getter instead
	public Vector3f modelPos = null;

	private Vector3f modelScale = null;

	private FloatBuffer matrix44Buffer = null;

	private int modelMatrixLocation = 0;
	private Vector3f zeroPos = null;
	
	private int value = 0;
	private float alpha = 0.8f;

	public Block(int x, int y, int z, int matrixLocation) {
		Random random = new Random();
		value = (random.nextInt(2) * 2) + 2;
		
		System.out.println(value);
		
		// Set the default quad rotation, scale and position values
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
		modelMatrixLocation = matrixLocation;

		zeroPos = new Vector3f(0, 0, 0);

		modelPos = new Vector3f(positionMult * ((float)x - 1.5f), positionMult * ((float)y - 1.5f), positionMult * ((float)z - 1.5f));

		modelScale = new Vector3f(1, 1, 1);

		// Setup model matrix
		modelCubeMatrix = new Matrix4f();

		//Cube
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(amountOfCubeVertices * vertexSize);
		vertexData.put(new float[]{
				-0.5f, 0.5f, 0.5f, 1f,
				-0.5f, -0.5f, 0.5f, 1f,
				0.5f, -0.5f, 0.5f, 1f,
				0.5f, 0.5f, 0.5f, 1f,

				-0.5f, 0.5f, -0.5f, 1f,
				-0.5f, -0.5f, -0.5f, 1f,
				0.5f, -0.5f, -0.5f, 1f,
				0.5f, 0.5f, -0.5f, 1f
		});
		vertexData.flip();

		float color = (float)value / 2048f;
		System.out.println(color);
		FloatBuffer colorData = BufferUtils.createFloatBuffer(amountOfCubeVertices * colorSize);
		colorData.put(new float[]{
				color * 300f, color * 200f, color, alpha, //front face
				color * 300f, color * 200f, color, alpha,
				color * 300f, color * 200f, color, alpha,
				color * 300f, color * 200f, color, alpha,

				color * 300f, color * 200f, color, alpha, //back face
				color * 300f, color * 200f, color, alpha,
				color * 300f, color * 200f, color, alpha,
				color * 300f, color * 200f, color, alpha});
		colorData.flip();
		

		byte[] indices = {
				0, 1, 2,
				2, 3, 0,

				4, 5, 6,
				6, 7, 4,

				3, 2, 6,
				6, 7, 3,

				4, 5, 1,
				1, 0, 4,

				0, 3, 7,
				7, 4, 0,

				2, 1, 5,
				5, 6, 2
		};

		cubeIndicesCount = indices.length;
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(cubeIndicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();

		//new vertex array object
		vertexCubeArrayObjectId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vertexCubeArrayObjectId);

		vboCubeVertexHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCubeVertexHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, vertexSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


		vboCubeColorHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCubeColorHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, colorSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


		vboCubeIndecesId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboCubeIndecesId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		GL30.glBindVertexArray(0);
	}

	public void logicCycle() {

		modelCubeMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);

	}

	public void renderRycle() {

		GL30.glBindVertexArray(vertexCubeArrayObjectId);
		//cube
		GL20.glEnableVertexAttribArray(0); //vertices
		GL20.glEnableVertexAttribArray(1); //colors

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboCubeIndecesId);

		//TODO: no logic here?
		modelCubeMatrix = new Matrix4f();
		Matrix4f.translate(modelPos, modelCubeMatrix, modelCubeMatrix);

		modelCubeMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, cubeIndicesCount, GL11.GL_UNSIGNED_BYTE, 0);

		modelCubeMatrix = new Matrix4f();
		Matrix4f.translate(zeroPos, modelCubeMatrix, modelCubeMatrix);

		modelCubeMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);

	}

	public void stop() {
		// Select the VAO
		GL30.glBindVertexArray(vertexCubeArrayObjectId);

		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);

		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboCubeVertexHandle);

		// Delete the color VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboCubeColorHandle);

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vertexCubeArrayObjectId);
	}

	public int getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	public void setCoord(int x, int y, int z) {
		modelPos.x = positionMult * ((float)x - 1.5f);
		modelPos.y = positionMult * ((float)y - 1.5f);
		modelPos.z = positionMult * ((float)z - 1.5f);
		
	}

}
