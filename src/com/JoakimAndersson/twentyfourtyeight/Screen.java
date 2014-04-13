package com.JoakimAndersson.twentyfourtyeight;

public abstract class Screen {
	public enum GameState {
		GameMenuState,
		GameRunningState,
		GamePausedState,
		GameExitState
	}
	
	public abstract void initScreen();
	public abstract GameState runScreen();
	
	protected GameState state;

	public Screen() {
		// TODO Auto-generated constructor stub
	}

}
