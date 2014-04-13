package com.JoakimAndersson.twentyfourtyeight;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import static org.lwjgl.opengl.GL11.*;

public class ScreenGame extends Screen {

	public ScreenGame() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initScreen() {
		// TODO Auto-generated method stub
		state = GameState.GameRunningState;

	}

	@Override
	public GameState runScreen() {
		// TODO Auto-generated method stub
		while (true) {
			Display.update();
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			
			
			Display.sync(60);
			
			if (Display.isCloseRequested()) {
				state = GameState.GameExitState;
				break;
				//return state;
			}
		}
		return state;
	}

}
