package com.hawk.activity.event.speciality;

/**
 * 红警战令特性事件
 * @author Jesse
 *
 */
public interface OrderEvent {

	String getPlayerId();
	
	<T> T convert();
}
