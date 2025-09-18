package com.hawk.activity.event.speciality;

/**
 * 星甲召唤星币获取任务类事件
 * 
 * @author lating
 *
 */
public interface SpaceMechaEvent {

	String getPlayerId();
	
	<T> T convert();
}
