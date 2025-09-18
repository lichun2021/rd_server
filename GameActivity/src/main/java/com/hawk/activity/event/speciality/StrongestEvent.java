package com.hawk.activity.event.speciality;

/**
 * 最强指挥官特性事件
 * @author PhilChen
 *
 */
public interface StrongestEvent {

	String getPlayerId();
	
	<T> T convert();
}
