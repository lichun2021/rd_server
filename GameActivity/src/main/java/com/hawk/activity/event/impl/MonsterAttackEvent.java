package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;
import com.hawk.activity.event.speciality.StrongestEvent;

/**
 * 攻击怪物事件
 * 
 * @author lating
 *
 */
public class MonsterAttackEvent extends ActivityEvent implements StrongestEvent, CrossActivityEvent, OrderEvent, EvolutionEvent, SpaceMechaEvent {
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
	/**
	 * 是否携带英雄出征
	 */
	private boolean withHero;
	
	/**
	 * 是否集结队长
	 */
	private boolean isLeader;

	public MonsterAttackEvent(){ super(null);}
	public MonsterAttackEvent(String playerId, int mosterType, int monsterId, int monsterLevel, int atkTimes, boolean isKill, boolean withHero, boolean isLeader) {
		super(playerId);
		this.monsterId = monsterId;
		this.mosterType = mosterType;
		this.monsterLevel = monsterLevel;
		this.atkTimes = atkTimes;
		this.isKill = isKill;
		this.withHero = withHero;
		this.isLeader = isLeader;
	}
	

	public MonsterAttackEvent(String playerId, int mosterType, int monsterId, int monsterLevel, int atkTimes, boolean isKill) {
		this(playerId, mosterType, monsterId, monsterLevel, atkTimes, isKill, false);
	}
	
	public MonsterAttackEvent(String playerId, int mosterType, int monsterId, int monsterLevel, int atkTimes, boolean isKill, boolean withHero) {
		this(playerId, mosterType, monsterId, monsterLevel, atkTimes, isKill, withHero, false);
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

	public boolean isWithHero() {
		return withHero;
	}
	
	public boolean isLeader() {
		return isLeader;
	}


	@Override
	public String toString() {
		return String.format("MonsterAttack, 1:%d,2:%d,3:%d,4:%d,5:%s", mosterType, monsterId, monsterLevel, atkTimes, isKill);
	}
}
