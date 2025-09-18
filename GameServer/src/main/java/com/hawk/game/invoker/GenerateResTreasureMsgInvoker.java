package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;

public class GenerateResTreasureMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	private ConsumeItems consumeItems;
	
	public GenerateResTreasureMsgInvoker(Player player, ConsumeItems consumeItems) {
		this.player = player;
		this.consumeItems = consumeItems;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		return consumeItems.consumeAndPush(player, Action.GEN_RES_TREASURE).hasAwardItem();
	}

	public Player getPlayer() {
		return player;
	}

	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}
	
}
