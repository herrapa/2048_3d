package com.JoakimAndersson.twentyfourtyeight;

import org.lwjgl.opengl.Display;

public class ScreenMenu extends Screen {

	public ScreenMenu() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initScreen() {
		// TODO Auto-generated method stub
		state = GameState.GameMenuState;
	}

	@Override
	public GameState runScreen() {
		// TODO Auto-generated method stub
		while (true) {
			Display.update();
			
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
