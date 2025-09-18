package com.hawk.activity.event.speciality;

/**
 * 英雄进化之路特性事件
 * @author lating
 *
 */
public interface EvolutionEvent {

	String getPlayerId();
	
	<T> T convert();
}
