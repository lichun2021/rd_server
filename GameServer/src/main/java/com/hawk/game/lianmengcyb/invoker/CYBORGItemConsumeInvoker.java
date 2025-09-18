package com.hawk.game.lianmengcyb.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.log.Action;

public class CYBORGItemConsumeInvoker extends HawkMsgInvoker {

	private Player player;
	private Action action;
	private ConsumeItems consumeItems;

	public CYBORGItemConsumeInvoker(Player player, ConsumeItems consumeItems, Action action) {
		this.player = player;
		this.consumeItems = consumeItems;
		this.action = action;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {

		return consumeItems.consumeAndPush(player, action).hasAwardItem();
	}

	public Player getPlayer() {
		return player;
	}

	public ConsumeItems getConsumeItems() {
		return consumeItems;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setConsumeItems(ConsumeItems consumeItems) {
		this.consumeItems = consumeItems;
	}

}
