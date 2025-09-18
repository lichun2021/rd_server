package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 指挥官学院能源数量
 * @author che
 *
 */
public class EnergyCountEvent extends ActivityEvent {

	
	private int energy;

	public EnergyCountEvent(){ super(null);}
	public EnergyCountEvent(String playerId, int energy) {
		super(playerId);
		this.energy = energy;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	

	
	
	
	
	

}
