package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 研究/升级科技
 * @author PhilChen
 *
 */
public class TechnologyLevelUpEvent extends ActivityEvent implements OrderEvent{
	
	private int techId;
	
	private int addPowar;
	
	/** 1.普通科技  2泰能科技*/
	private int source;
	
	public TechnologyLevelUpEvent(){ super(null);}
	public TechnologyLevelUpEvent(String playerId,int source, int techId, int addPowar) {
		super(playerId);
		this.techId = techId;
		this.addPowar = addPowar;
		this.source = source;
	}
	
	public int getTechId() {
		return techId;
	}

	public int getAddPowar() {
		return addPowar;
	}
	
	
	public int getSource(){
		return this.source;
	}
}
