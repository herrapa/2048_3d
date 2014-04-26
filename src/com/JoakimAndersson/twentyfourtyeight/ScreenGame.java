package com.JoakimAndersson.twentyfourtyeight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

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

	private final float gridSize = 2.5f;
	private int vertexGridArrayObjectId;
	private int vboGridVertexHandle;
	private int vboGridColorHandle;
	final private int amountOfGridVertices = 10;
	final private int vertexGridSize = 4;
	final private int colorGridSize = 4;
	private int gridIndicesCount = 0;
	private int vboGridIndecesId = 0;

	private final double PI = 3.14159265358979323846;

	// Shader variables
	private int vsId = 0;
	private int fsId = 0;
	private int pId = 0;

	// Moving variables
	private int projectionMatrixLocation = 0;
	private int viewMatrixLocation = 0;
	private int modelMatrixLocation = 0;
	//private int modelGridMatrixLocation = 0;
	private Matrix4f projectionMatrix = null;
	private Matrix4f viewMatrix = null;
	private Vector3f cameraPos = null;
	private FloatBuffer matrix44Buffer = null;

	private Vector3f modelAngle = null;

	private int WIDTH;
	private int HEIGHT;

	//private Vector<Block> cubeVector = new Vector<Block>();
	private Block[][][] blockArray = new Block[4][4][4];

	public ScreenGame(int w, int h) {
		WIDTH = w;
		HEIGHT = h;

		setupShaders();
		setupMatrices();
		setupModels();
	}

	private void setupModels() {

		//Grid
		FloatBuffer vertexGridData = BufferUtils.createFloatBuffer(amountOfGridVertices * vertexGridSize);
		vertexGridData.put(new float[]{
				-gridSize, gridSize, gridSize, 1f,
				-gridSize, -gridSize, gridSize, 1f,
				gridSize, -gridSize, gridSize, 1f,
				gridSize, gridSize, gridSize, 1f,

				-gridSize, gridSize, -gridSize, 1f,
				-gridSize, -gridSize, -gridSize, 1f,
				gridSize, -gridSize, -gridSize, 1f,
				gridSize, gridSize, -gridSize, 1f,

				-gridSize, 0f, gridSize, 1f,
				gridSize, 0f, gridSize, 1f
		});
		vertexGridData.flip();

		FloatBuffer colorGridData = BufferUtils.createFloatBuffer(amountOfGridVertices * colorGridSize);
		colorGridData.put(new float[]{
				0f, 1f, 0f, 1f, //front face
				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f,

				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f,

				0f, 1f, 0f, 1f,
				0f, 1f, 0f, 1f});
		colorGridData.flip();

		byte[] griIindices = {
				0, 1, 
				1, 2,
				2, 3,
				3, 0,

				4, 5,
				5, 6,
				6, 7,
				7, 4,

				0, 4,
				3, 7,
				1, 5,
				2, 6,
				8, 9
		};

		gridIndicesCount = griIindices.length;
		ByteBuffer indicesGridBuffer = BufferUtils.createByteBuffer(gridIndicesCount);
		indicesGridBuffer.put(griIindices);
		indicesGridBuffer.flip();

		//new vertex array object
		vertexGridArrayObjectId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vertexGridArrayObjectId);

		vboGridVertexHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGridVertexHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexGridData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, vertexGridSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		vboGridColorHandle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGridColorHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorGridData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, colorGridSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		vboGridIndecesId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboGridIndecesId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesGridBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		GL30.glBindVertexArray(0);

		Random rand = new Random();

		int x = rand.nextInt(4);
		int y = 1;
		int z = rand.nextInt(4);
		blockArray[x][y][z] = new Block(x, 1, z, modelMatrixLocation);
		
		x = rand.nextInt(4);
		y = 2;
		z = rand.nextInt(4);
		blockArray[x][y][z] = new Block(x, 1, z, modelMatrixLocation);

		cameraPos = new Vector3f(0, 0, -10);

		modelAngle = new Vector3f(0, 0, 0);
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
			renderRycle();

			if (Display.isCloseRequested()) {
				state = GameState.GameExitState;
				break;
			}
		}
		return state;
	}

	private void renderRycle() {

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL20.glUseProgram(pId);


		//Grid
		GL30.glBindVertexArray(vertexGridArrayObjectId);

		GL20.glEnableVertexAttribArray(0); //vertices
		GL20.glEnableVertexAttribArray(1); //colors
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboGridIndecesId);

		GL11.glDrawElements(GL11.GL_LINES, gridIndicesCount, GL11.GL_UNSIGNED_BYTE, 0);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);


		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 4; z++) {
					if (blockArray[x][y][z] != null) {
						blockArray[x][y][z].renderRycle();
					}
				}
			}
		}
		/*
		for (Block b : cubeVector) {
			b.renderRycle();
		}
*/

		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);

		Display.sync(60);
		Display.update();
	}

	private void logicCycle() {
		//-- Input processing
		float rotationDelta = 1.0f;
		float scaleDelta = 0.1f;
		//float posDelta = 0.1f;
		//Vector3f scaleAddResolution = new Vector3f(scaleDelta, scaleDelta, scaleDelta);
		//Vector3f scaleMinusResolution = new Vector3f(-scaleDelta, -scaleDelta, -scaleDelta);

		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) ) {
			//modelAngle.y += rotationDelta;
			cameraPos.x += scaleDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ) {
			//modelAngle.y -= rotationDelta;
			cameraPos.x -= scaleDelta;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_UP) ) {
			//modelAngle.x += rotationDelta;
			cameraPos.y -= scaleDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) ) {
			//modelAngle.x -= rotationDelta;
			cameraPos.y += scaleDelta;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_O) ) {
			cameraPos.z += scaleDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_P) ) {
			cameraPos.z -= scaleDelta;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_U) ) {
			modelAngle.y += rotationDelta;

		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_I) ) {
			modelAngle.y -= rotationDelta;
		}

		
		Vector3f dir = new Vector3f(0f, 0f, 0f);
		//move cubes
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_W) {
					System.out.println("W Key Pressed");
					dir.y = 1f;
					
					//for (int lay = 2; lay >= 0; lay--) {
						for (int y = 2; y >= 0; y--) {
							for (int x = 0; x < 4; x++) {
								for (int z = 0; z < 4; z++) {
									if (blockArray[x][y][z] != null) {
										if (blockArray[x][y + 1][z] == null){// || blockArray[x][y - 1][z].getValue() == blockArray[x][y][z].getValue()) {
											//combine blocks, or move
											blockArray[x][y + 1][z] = blockArray[x][y][z];
											blockArray[x][y + 1][z].setCoord(x, y + 1, z);
											blockArray[x][y][z] = null;
										}
									}
								}
							}
						}
					//}
					
					
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					System.out.println("D Key Pressed");
					dir.y = -1f;
				}
				
				if (Keyboard.getEventKey() == Keyboard.KEY_A) {
					System.out.println("A Key Pressed");
					dir.x = 1f;
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					System.out.println("S Key Pressed");
					dir.x = -1f;
				}
				
				if (Keyboard.getEventKey() == Keyboard.KEY_E) {
					System.out.println("E Key Pressed");
					dir.z = 1f;
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
					System.out.println("Q Key Pressed");
					dir.z = -1f;
				}
			}
		}
		
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 4; z++) {
					if (blockArray[x][y][z] != null) {
						blockArray[x][y][z].logicCycle();
					}
				}
			}
		}
		
		/*TODO:
		 * for layer just above "bottom" face, check if can move and remove/add block
		 * repeat for all layers.
		 * 
		 */

		//-- Update matrices
		// Reset view and model matrices
		viewMatrix = new Matrix4f();

		// Translate camera
		Matrix4f.translate(cameraPos, viewMatrix, viewMatrix);

		// Scale, translate and rotate model

		Matrix4f.rotate(this.degreesToRadians(modelAngle.z), new Vector3f(0, 0, 1), 
				viewMatrix, viewMatrix);
		Matrix4f.rotate(this.degreesToRadians(modelAngle.y), new Vector3f(0, 1, 0), 
				viewMatrix, viewMatrix);
		Matrix4f.rotate(this.degreesToRadians(modelAngle.x), new Vector3f(1, 0, 0), 
				viewMatrix, viewMatrix);

		// Upload matrices to the uniform variables
		GL20.glUseProgram(pId);

		projectionMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(projectionMatrixLocation, false, matrix44Buffer);
		viewMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		GL20.glUniformMatrix4(viewMatrixLocation, false, matrix44Buffer);

		
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 4; z++) {
					if (blockArray[x][y][z] != null) {
						blockArray[x][y][z].logicCycle();
					}
				}
			}
		}

		GL20.glUseProgram(0);

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
		GL30.glBindVertexArray(vertexGridArrayObjectId);

		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);

		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboGridVertexHandle);

		// Delete the color VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboGridColorHandle);

		//TODO: delete grid vbo

		// Delete the index VBO
		//GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		//GL15.glDeleteBuffers(vboiId);

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vertexGridArrayObjectId);

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
