package com.hawk.activity.event.speciality;

/**
 * 跨服积分事件
 * @author Jesse
 *
 */
public interface CrossActivityEvent {

	String getPlayerId();
	
	<T> T convert();
}
