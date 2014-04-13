package com.JoakimAndersson.twentyfourtyeight;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

public class Game {
	
	public Game() {
		
		
	}
	
	public void initialize() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.setTitle("2048 3D");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			Display.destroy();
			System.exit(0);
		}
		setupOpenGL();
	}
	
	public void run() {
		System.out.print("game running");
		
		ScreenGame screengame = new ScreenGame();
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
		exitGame();
	}
	
	private void setupOpenGL() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 600, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
	}

	private void exitGame() {
		Display.destroy();
		System.exit(0);
	}
}
