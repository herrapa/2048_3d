package com.JoakimAndersson.twentyfourtyeight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class ScreenGame extends Screen {

	private int vboVertexHandle;
	private int vertexArrayObjectId;
	private int vboColorHandle;
	final private int amountOfVertices = 8;
	final private int vertexSize = 4;
	final private int colorSize = 4;
	private int indicesCount = 0;
	private int vboIndecesId = 0;

	private final double PI = 3.14159265358979323846;

	// Shader variables
	private int vsId = 0;
	private int fsId = 0;
	private int pId = 0;

	// Moving variables
	private int projectionMatrixLocation = 0;
	private int viewMatrixLocation = 0;
	private int modelMatrixLocation = 0;
	private Matrix4f projectionMatrix = null;
	private Matrix4f viewMatrix = null;
	private Matrix4f modelMatrix = null;
	private Vector3f modelPos = null;
	private Vector3f modelAngle = null;
	private Vector3f modelScale = null;
	private Vector3f cameraPos = null;
	private FloatBuffer matrix44Buffer = null;

	private int WIDTH;
	private int HEIGHT;

	public ScreenGame(int w, int h) {
		WIDTH = w;
		HEIGHT = h;

		setupModels();
		setupShaders();
		setupMatrices();
	}

	private void setupModels() {
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
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

		FloatBuffer colorData = BufferUtils.createFloatBuffer(amountOfVertices * colorSize);
		colorData.put(new float[]{
				1f, 0f, 0f, 1f, //front face
				0f, 1f, 0f, 1f,
				0f, 0f, 1f, 1f,
				1f, 1f, 1f, 1f,

				1f, 0f, 0f, 1f, //back face
				0f, 1f, 0f, 1f,
				0f, 0f, 1f, 1f,
				1f, 1f, 1f, 1f});
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

		indicesCount = indices.length;
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();



		//new vertex array object
		vertexArrayObjectId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vertexArrayObjectId);

		vboVertexHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, vertexSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);



		vboColorHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColorHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, colorSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL30.glBindVertexArray(0);

		vboIndecesId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndecesId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Set the default quad rotation, scale and position values
		modelPos = new Vector3f(0, 0, 0);
		modelAngle = new Vector3f(0, 0, 0);
		modelScale = new Vector3f(1, 1, 1);
		cameraPos = new Vector3f(0, 0, -1);
	}

	private void setupMatrices() {
		// Setup projection matrix
		projectionMatrix = new Matrix4f();
		float fieldOfView = 60f;
		float aspectRatio = (float)WIDTH / (float)HEIGHT;
		float near_plane = 0.1f;
		float far_plane = 100f;

		float y_scale = this.coTangent(this.degreesToRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length);
		projectionMatrix.m33 = 0;

		// Setup view matrix
		viewMatrix = new Matrix4f();

		// Setup model matrix
		modelMatrix = new Matrix4f();

		// Create a FloatBuffer with the proper size to store our matrices later
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
	}

	@Override
	public void initScreen() {
		state = GameState.GameRunningState;
	}

	@Override
	public GameState runScreen() {
		while (true) {

			logicCycle();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);


			GL30.glBindVertexArray(vertexArrayObjectId);
			GL20.glEnableVertexAttribArray(0); //vertices
			GL20.glEnableVertexAttribArray(1); //colors

			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndecesId);


			modelMatrix = new Matrix4f();
			Vector3f old_pos = new Vector3f(modelPos);//modelPos;


			GL20.glUseProgram(pId);
			//GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

			for (int i = -3; i <= 3; i++) {
				modelPos.x = old_pos.x + (float)i * 1.5f;
				for (int j = -3; j <= 3; j++) {
					modelPos.y = old_pos.y + (float)j * 1.5f;

					//modelMatrix.setZero();
					modelMatrix = new Matrix4f();
					
					//TODO: store position, value, animation to point in block, move them accordingly
					//Draw outline

					Matrix4f.translate(modelPos, modelMatrix, modelMatrix);

					Matrix4f.rotate(this.degreesToRadians(modelAngle.z), new Vector3f(0, 0, 1), 
							modelMatrix, modelMatrix);
					Matrix4f.rotate(this.degreesToRadians(modelAngle.y), new Vector3f(0, 1, 0), 
							modelMatrix, modelMatrix);
					Matrix4f.rotate(this.degreesToRadians(modelAngle.x), new Vector3f(1, 0, 0), 
							modelMatrix, modelMatrix);

					modelMatrix.store(matrix44Buffer); matrix44Buffer.flip();
					GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);

					GL20.glUseProgram(pId);
					GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

				}
			}






			modelPos = old_pos;

			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
			GL20.glUseProgram(0);

			Display.sync(60);
			Display.update();


			if (Display.isCloseRequested()) {
				state = GameState.GameExitState;
				break;
				//return state;
			}
		}
		return state;
	}

	private void logicCycle() {
		//-- Input processing
		float rotationDelta = 1.0f;
		float scaleDelta = 0.1f;
		float posDelta = 0.1f;
		Vector3f scaleAddResolution = new Vector3f(scaleDelta, scaleDelta, scaleDelta);
		Vector3f scaleMinusResolution = new Vector3f(-scaleDelta, -scaleDelta, 
				-scaleDelta);

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) ) {
			modelAngle.y += rotationDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ) {
			modelAngle.y -= rotationDelta;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_UP) ) {
			modelAngle.x += rotationDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) ) {
			modelAngle.x -= rotationDelta;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_O) ) {
			cameraPos.z += scaleDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_P) ) {
			cameraPos.z -= scaleDelta;
		}

		//-- Update matrices
		// Reset view and model matrices
		viewMatrix = new Matrix4f();
		modelMatrix = new Matrix4f();

		// Translate camera
		Matrix4f.translate(cameraPos, viewMatrix, viewMatrix);

		// Scale, translate and rotate model

		Matrix4f.scale(modelScale, modelMatrix, modelMatrix);
		Matrix4f.translate(modelPos, modelMatrix, modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(modelAngle.z), new Vector3f(0, 0, 1), 
				modelMatrix, modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(modelAngle.y), new Vector3f(0, 1, 0), 
				modelMatrix, modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(modelAngle.x), new Vector3f(1, 0, 0), 
				modelMatrix, modelMatrix);

		// Upload matrices to the uniform variables
		GL20.glUseProgram(pId);

		projectionMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(projectionMatrixLocation, false, matrix44Buffer);
		viewMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(viewMatrixLocation, false, matrix44Buffer);
		modelMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);

		GL20.glUseProgram(0);

		//this.exitOnGLError("logicCycle");
	}

	public int loadShader(String filename, int type) {
		StringBuilder shaderSource = new StringBuilder();
		int shaderID = 0;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read file.");
			e.printStackTrace();
			System.exit(-1);
		}

		shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);

		return shaderID;
	}

	private void setupShaders() {
		int errorCheckValue = GL11.glGetError();

		// Load the vertex shader
		vsId = this.loadShader("src/com/JoakimAndersson/twentyfourtyeight/vertex.glsl", GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		fsId = this.loadShader("src/com/JoakimAndersson/twentyfourtyeight/fragment.glsl", GL20.GL_FRAGMENT_SHADER);

		// Create a new shader program that links both shaders
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);

		// Position information will be attribute 0
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(pId, 1, "in_Color");

		GL20.glLinkProgram(pId);
		GL20.glValidateProgram(pId);

		// Get matrices uniform locations
		projectionMatrixLocation = GL20.glGetUniformLocation(pId,"projectionMatrix");
		viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
		modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");

		errorCheckValue = GL11.glGetError();
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			System.out.println("ERROR - Could not create the shaders:" + GLU.gluErrorString(errorCheckValue));
			System.exit(-1);
		}
	}

	public void stopScreen() {

		// Delete the shaders
		GL20.glUseProgram(0);
		GL20.glDetachShader(pId, vsId);
		GL20.glDetachShader(pId, fsId);

		GL20.glDeleteShader(vsId);
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId);

		// Select the VAO
		GL30.glBindVertexArray(vertexArrayObjectId);

		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);

		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboVertexHandle);

		// Delete the color VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboColorHandle);

		// Delete the index VBO
		//GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		//GL15.glDeleteBuffers(vboiId);

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vertexArrayObjectId);

		//GL15.glDeleteBuffers(vboVertexHandle);
		//GL15.glDeleteBuffers(vboColorHandle);
	}

	private float coTangent(float angle) {
		return (float)(1f / Math.tan(angle));
	}

	private float degreesToRadians(float degrees) {
		return degrees * (float)(PI / 180d);
	}

}
