package com.hawk.game.module.lianmenxhjz.battleroom.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.log.Action;

/**
 * 世界迁城
 * @author jm
 *
 */
public class XHJZMoveCityMsgInvoker extends HawkMsgInvoker {

	private int moveCityType;
	private Player player;
	private ConsumeItems consumeItems;
	
	public XHJZMoveCityMsgInvoker(Player player, ConsumeItems consumeItems, int moveCityType){
		this.player = player;
		this.consumeItems = consumeItems;
		this.moveCityType = moveCityType;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 道具消耗
		Action action = Action.TBLY_MOVE_CITY;
		
		return consumeItems.consumeAndPush(player, action).hasAwardItem();
	}
	public int getMoveCityType() {
		return moveCityType;
	}
	public Player getPlayer() {
		return player;
	}
	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}
}
