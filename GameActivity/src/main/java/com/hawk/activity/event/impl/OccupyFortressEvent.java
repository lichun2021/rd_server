package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;

public class OccupyFortressEvent extends ActivityEvent implements CrossActivityEvent {

	public int armyCount = 0;
	
	public OccupyFortressEvent(){ super(null);}
	public OccupyFortressEvent(String playerId) {
		super(playerId);
	}

	public int getArmyCount() {
		return armyCount;
	}

	public void setArmyCount(int armyCount) {
		this.armyCount = armyCount;
	}
	
}
