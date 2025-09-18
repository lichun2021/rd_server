package com.hawk.game.service.tiberium;

import com.hawk.game.service.tiberium.TiberiumConst.FightState;

public class TWFightUnit {
	
	public int timeIndex;
	
	public FightState state = FightState.NOT_OPEN;
	
	public int getTimeIndex() {
		return timeIndex;
	}

	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	public FightState getState() {
		return state;
	}

	public void setState(FightState state) {
		this.state = state;
	}
	
}
