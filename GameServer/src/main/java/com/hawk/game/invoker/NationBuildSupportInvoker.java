package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.NationConstCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.log.Action;

public class NationBuildSupportInvoker extends HawkMsgInvoker {

	private Player player;

	public NationBuildSupportInvoker(Player player) {
		this.player = player;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(ItemInfo.valueListOf(NationConstCfg.getInstance().getSupportAward()));
		award.rewardTakeAffectAndPush(player, Action.NATIONAL_BUILD_SUPPORT_AWARD, true);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
