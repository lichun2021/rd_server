package com.hawk.game.service.cyborgWar;

import com.hawk.game.service.cyborgWar.CWConst.FightState;

public class CWFightUnit {
	
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
