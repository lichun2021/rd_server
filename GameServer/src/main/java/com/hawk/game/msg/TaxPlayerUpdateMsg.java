package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.item.ConsumeItems;
import com.hawk.gamelib.GameConst.MsgId;

public class TaxPlayerUpdateMsg extends HawkMsg {

	private ConsumeItems consumeItems;
	
	public TaxPlayerUpdateMsg(ConsumeItems consumeItems) {
		super(MsgId.TAX_PLAYER_UPDATE);
		this.consumeItems = consumeItems;
	}
	
	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}
	
	public void setConsumeItems(ConsumeItems consumeItems) {
		this.consumeItems = consumeItems;
	}

	public static TaxPlayerUpdateMsg valueOf(ConsumeItems consumeItems) {
		return new TaxPlayerUpdateMsg(consumeItems);
	}
}
