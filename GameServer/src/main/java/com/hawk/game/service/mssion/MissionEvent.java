package com.hawk.game.service.mssion;

import java.util.List;

/**
 * 任务事件
 * 
 * @author golden
 *
 */
public abstract class MissionEvent {

	/**
	 * 触发任务列表
	 * @return
	 */
	public List<MissionType> touchMissions() {
		return null;
	}
	
	/**
	 * 触发普通任务
	 * @return
	 */
	public List<MissionType> touchGeneralMissions() {
		return null;
	}
}
