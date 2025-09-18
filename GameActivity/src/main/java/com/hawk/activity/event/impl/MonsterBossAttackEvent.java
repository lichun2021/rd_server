package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 攻击怪物事件
 * 
 * @author lating
 *
 */
public class MonsterBossAttackEvent extends ActivityEvent {
	// 怪物类型
	private int mosterType;

	// 怪物Id
	private int monsterId;

	// 怪物等级
	private int monsterLevel;

	// 攻击次数
	private int atkTimes;

	// 是否击杀
	private boolean isKill;
	

	public MonsterBossAttackEvent(){ super(null);}
	public MonsterBossAttackEvent(String playerId, int mosterType, int monsterId, int monsterLevel, int atkTimes, boolean isKill) {
		super(playerId);
		this.monsterId = monsterId;
		this.mosterType = mosterType;
		this.monsterLevel = monsterLevel;
		this.atkTimes = atkTimes;
		this.isKill = isKill;
	}
	

	public int getMonsterId() {
		return monsterId;
	}

	public int getMonsterLevel() {
		return monsterLevel;
	}

	public int getMosterType() {
		return mosterType;
	}

	public int getAtkTimes() {
		return atkTimes;
	}

	public boolean isKill() {
		return isKill;
	}



	@Override
	public String toString() {
		return String.format("MonsterAttack, 1:%d,2:%d,3:%d,4:%d,5:%s", mosterType, monsterId, monsterLevel, atkTimes, isKill);
	}
}
