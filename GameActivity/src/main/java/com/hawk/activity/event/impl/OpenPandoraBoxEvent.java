package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 开启潘多拉宝箱
 * @author Jesse
 *
 */
public class OpenPandoraBoxEvent extends ActivityEvent implements OrderEvent {
	private int count;

	public OpenPandoraBoxEvent(){ super(null);}
	public OpenPandoraBoxEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

}
