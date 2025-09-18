package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;

public class StoreMarchBackMsgInvoker extends HawkMsgInvoker {
	AwardItems awardItems;
	Player player;

	public StoreMarchBackMsgInvoker(AwardItems awardItems, Player player) {
		super();
		this.awardItems = awardItems;
		this.player = player;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		awardItems.rewardTakeAffectAndPush(player, Action.WARE_HOUSE_TAKE_MARCH_BACK);
		return true;
	}

	public AwardItems getAwardItems() {
		return awardItems;
	}

	public void setAwardItems(AwardItems awardItems) {
		this.awardItems = awardItems;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
