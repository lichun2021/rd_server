package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 获取指定道具事件
 * 
 * @author lating
 *
 */
public class EventGainItem extends MissionEvent {

	private int itemId;
	private int count;
	
	public EventGainItem(int itemId, int count) {
		this.itemId = itemId;
		this.count = count;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.GAIN_ITEM);
		return touchMissionList;
	}

}
