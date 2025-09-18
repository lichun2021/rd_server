

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 威龙庆典分享
 * 
 * @author lating
 *
 */
public class ReturnPuzzleShareEvent extends ActivityEvent{
	
	private Integer shareId;
	
	public ReturnPuzzleShareEvent(){ super(null);}
	public ReturnPuzzleShareEvent(String playerId,Integer shareId) {
		super(playerId);
		this.shareId = shareId;
	}
	
	public static ReturnPuzzleShareEvent valueOf(String playerId,Integer shareId){
		ReturnPuzzleShareEvent event = new ReturnPuzzleShareEvent(playerId,shareId);
		return event;
	}

	public Integer getShareId() {
		return shareId;
	}

	public void setShareId(Integer shareId) {
		this.shareId = shareId;
	}
}
