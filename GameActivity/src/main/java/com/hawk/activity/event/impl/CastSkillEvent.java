package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class CastSkillEvent extends ActivityEvent implements OrderEvent {
	private int skillId;

	public CastSkillEvent(){ super(null);}
	public CastSkillEvent(String playerId, int skillId) {
		super(playerId);
		this.skillId = skillId;
	}

	public final int getSkillId() {
		return skillId;
	}
}
