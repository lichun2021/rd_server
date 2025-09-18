package com.hawk.game.module.lianmengXianquhx.entity;

import javax.persistence.Transient;

import org.hawk.db.entifytype.EntityType;

import com.hawk.game.entity.ArmyEntity;

public class XQHXArmyEntity extends ArmyEntity {
	@Transient
	private int maxFree;

	public XQHXArmyEntity() {
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
