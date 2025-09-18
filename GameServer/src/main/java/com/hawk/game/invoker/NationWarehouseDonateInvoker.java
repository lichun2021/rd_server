package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.log.Action;

public class NationWarehouseDonateInvoker extends HawkMsgInvoker {

	private Player player;
	private List<ItemInfo> itemInfos;

	public NationWarehouseDonateInvoker(Player player, List<ItemInfo> itemInfos) {
		this.player = player;
		this.itemInfos = itemInfos;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(itemInfos);
		awardItem.rewardTakeAffectAndPush(player, Action.NATIONAL_WAREHOUSE_DODATE_AWARD, true);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}
