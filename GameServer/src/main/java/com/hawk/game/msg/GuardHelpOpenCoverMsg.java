package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.config.ItemCfg;

/**
 * 帮助开罩。
 * @author jm
 *
 */
public class GuardHelpOpenCoverMsg extends HawkMsg {
	//source player id 
	String playerId;
	//buff ID
	private ItemCfg item;
	
	public GuardHelpOpenCoverMsg(String playerId, ItemCfg item) {
		this.playerId = playerId;
		this.item = item;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public ItemCfg getItem() {
		return item;
	}

	public void setItem(ItemCfg item) {
		this.item = item;
	}
	
	
}
