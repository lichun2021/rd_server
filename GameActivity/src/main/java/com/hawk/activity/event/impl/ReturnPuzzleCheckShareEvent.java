

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 威龙庆典分享
 * 
 * @author lating
 *
 */
public class ReturnPuzzleCheckShareEvent extends ActivityEvent{
	
	private Integer shareId;
	
	public ReturnPuzzleCheckShareEvent(){ super(null);}
	public ReturnPuzzleCheckShareEvent(String playerId,Integer shareId) {
		super(playerId);
		this.shareId = shareId;
	}
	
	public static ReturnPuzzleCheckShareEvent valueOf(String playerId,Integer shareId){
		ReturnPuzzleCheckShareEvent event = new ReturnPuzzleCheckShareEvent(playerId,shareId);
		return event;
	}

	public Integer getShareId() {
		return shareId;
	}

	public void setShareId(Integer shareId) {
		this.shareId = shareId;
	}
}
