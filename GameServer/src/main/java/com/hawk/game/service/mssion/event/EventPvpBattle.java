package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventPvpBattle extends MissionEvent {
	
	/** 是否胜利*/
	private boolean isAtkWin;
	
	/** 部队击杀数据*/
	private Map<Integer, Integer> armyKillMap;
	
	/** 部队击伤数据*/
	private Map<Integer, Integer> armyHurtMap;
	
	/** 是否是攻击部队*/
	private boolean isAttacker;
	
	/** 是否是集结部队*/
	private boolean isMass;
	
	/** 攻击方行军类型*/
	private WorldMarchType marchType;
	
	/** 攻击方行军类型*/
	private boolean isLeader;
	
	List<ArmyInfo> selfArmy;
	
	public EventPvpBattle(boolean isAtkWin, Map<Integer, Integer> armyKillMap, Map<Integer, Integer> armyHurtMap, boolean isAttacker, boolean isMass, WorldMarchType marchType, boolean isLeader, List<ArmyInfo> selfArmy) {
		this.isAtkWin = isAtkWin;
		this.armyKillMap = armyKillMap;
		this.armyHurtMap = armyHurtMap;
		this.isAttacker = isAttacker;
		this.isMass = isMass;
		this.selfArmy = selfArmy;
		this.marchType = marchType;
	}
	
	public boolean isAtkWin() {
		return isAtkWin;
	}
	
	public Map<Integer, Integer> getArmyKillMap() {
		return armyKillMap;
	}

	public Map<Integer, Integer> getArmyHurtMap() {
		return armyHurtMap;
	}
	
	public boolean isAttacker() {
		return isAttacker;
	}

	public boolean isMass() {
		return isMass;
	}

	public WorldMarchType getMarchType() {
		return marchType;
	}

	public boolean isLeader() {
		return isLeader;
	}

	public void setAttacker(boolean isAttacker) {
		this.isAttacker = isAttacker;
	}

	public List<ArmyInfo> getSelfArmy() {
		return selfArmy;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MASS_KILL_SOLIDER);
		touchMissionList.add(MissionType.ASSISTANCE_KILL_SOLIDER);
		touchMissionList.add(MissionType.TRAP_KILL_SOLIDER);
		touchMissionList.add(MissionType.MISSION_KILL_LEVEL_SOLIDER_COUNT);
		
		touchMissionList.add(MissionType.MISSION_HURT_IN_PRESIDENT);
		touchMissionList.add(MissionType.MISSION_HURT_IN_PRESIDENT_TOWER);
		touchMissionList.add(MissionType.MISSION_DEAD_IN_PRESIDENT);
		touchMissionList.add(MissionType.MISSION_DEAD_IN_PRESIDENT_TOWER);
		
		return touchMissionList;
	}
}
