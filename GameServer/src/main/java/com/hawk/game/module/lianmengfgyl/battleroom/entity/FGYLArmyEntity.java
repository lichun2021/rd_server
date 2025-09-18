package com.hawk.game.module.lianmengfgyl.battleroom.entity;

import javax.persistence.Transient;

import org.hawk.db.entifytype.EntityType;

import com.hawk.game.entity.ArmyEntity;

public class FGYLArmyEntity extends ArmyEntity {
	@Transient
	private int maxFree;

	public FGYLArmyEntity() {
		setPersistable(false);
		setEntityType(EntityType.TEMPORARY);
	}

	@Override
	public synchronized void addFree(int free) {
		super.addFree(free);
		maxFree = Math.max(maxFree, getFree());
	}

	public int getMaxFree() {
		return maxFree;
	}

	public void setMaxFree(int maxFree) {
		this.maxFree = maxFree;
	}

}
