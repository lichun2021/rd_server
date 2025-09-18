package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 装备升阶
 * @author golden
 *
 */
public class EventEquipQualityUp extends MissionEvent {

	int beforeQuality;
	
	int afterQuality;
	
	int equipId;
	
	public EventEquipQualityUp(int beforeQuality, int afterQuality, int equipId) {
		super();
		this.beforeQuality = beforeQuality;
		this.afterQuality = afterQuality;
		this.equipId = equipId;
	}
	
	public int getBeforeQuality() {
		return beforeQuality;
	}

	public int getAfterQuality() {
		return afterQuality;
	}

	public int getEquipId() {
		return equipId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_EQUIP_QUALITY_COUNT);
		return touchMissionList;
	}
}
