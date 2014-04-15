package com.JoakimAndersson.twentyfourtyeight;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import static org.lwjgl.opengl.GL11.*;

public class Game {
	
	private final int WIDTH = 1920;
	private final int HEIGHT = 1080;
	private final String WINDOW_TITLE = "2048 3D";
	
	public Game() {
		
		
	}
	
	private void initialize() {
		
		setupOpenGL();
	}
	
	public void run() {
		
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		
		initialize();
		System.out.println("game running");
		
		ScreenGame screengame = new ScreenGame(WIDTH, HEIGHT);
		screengame.initScreen();
		
		Screen screen = screengame;
		
		stateloop: while (true) {
			
			switch (screen.runScreen()) {
			case GameMenuState:
				break;
			case GameExitState:
				break stateloop;
				//break;
			case GamePausedState:
				break;
			case GameRunningState:
				break;
			default:
				break;
			}
		}		
		screengame.stopScreen();
		exitGame();
	}
	
	private void setupOpenGL() {
		System.out.println("Initialize screen");
		try {
			PixelFormat pixelFormat = new PixelFormat();
			ContextAttribs contextAttributes = new ContextAttribs(3, 2) //use OpenGL 3.2
			.withForwardCompatible(true)
			.withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.setTitle(WINDOW_TITLE);
			Display.setFullscreen(true);
			Display.create(pixelFormat, contextAttributes);
		} catch (LWJGLException e) {
			e.printStackTrace();
			Display.destroy();
			System.exit(0);
		}
		System.out.println("Setting up OpenGL");
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		//glMatrixMode(GL_PROJECTION);
		//GL11.glLoadIdentity();
		//glOrtho(0, 800, 600, 0, 1, -1);
		//GL11.glOrtho(-1, 1, -1, 1, -1, 1);
		//glMatrixMode(GL_MODELVIEW);
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		GL11.glEnable(GL_DEPTH_TEST);
	}

	private void exitGame() {
		Display.destroy();
		System.exit(0);
	}
}
