package com.hawk.game.module.lianmengtaiboliya.entity;

import javax.persistence.Transient;

import org.hawk.db.entifytype.EntityType;

import com.hawk.game.entity.ArmyEntity;

public class TBLYArmyEntity extends ArmyEntity {
	@Transient
	private int maxFree;

	public TBLYArmyEntity() {
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
