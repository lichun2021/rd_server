package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 泰能研究所生产线升级事件
 * 
 * @author lating
 *
 */
public class EventPlantFactoryLvup extends MissionEvent {

	/** 生产线类型 */
	int factoryType;

	/** 生产线等级 */
	int afterLevel;

	public EventPlantFactoryLvup(int factoryType, int afterLevel) {
		this.factoryType = factoryType;
		this.afterLevel = afterLevel;
	}

	public int getFactoryType() {
		return factoryType;
	}

	public int getAfterLevel() {
		return afterLevel;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.PLANT_FACTOFY_LEVEL);
		return touchMissionList;
	}
	
}
