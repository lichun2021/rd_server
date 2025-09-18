package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 占领盟总电塔x毫秒
 * @author Golden
 *
 */
public class EventOccupyCrossFortressSecond extends MissionEvent {

	/**
	 * 占领x毫秒
	 */
	private long millSeconds;
	
	public EventOccupyCrossFortressSecond(long occupyTime) {
		this.millSeconds = occupyTime;
	}

	public long getMillSeconds() {
		return millSeconds;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.OCCUPY_CROSS_FORTRESS_SECOND);
		return touchMissionList;
	}	
}
