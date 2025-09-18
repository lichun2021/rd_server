package com.hawk.game.module.plantsoldier.science;

public enum PlantScienceState {
	FREE(0),
	RESEARCH(1);
	
	int state;
	private PlantScienceState(int state) {
		this.state = state;
	}
	
	
	public int getNum(){
		return this.state;
	}
	
	public static PlantScienceState valueOf(int state){
		switch (state) {
			case 0:return PlantScienceState.FREE;
			case 1:return PlantScienceState.RESEARCH;
		}
		return null;
	}
}
