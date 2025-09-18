package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.game.player.Player;

public class WarhouseSendBackMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	private String resource;
	
	public WarhouseSendBackMsgInvoker(Player player, String resource) {
		this.player = player;
		this.resource = resource;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(ItemInfo.valueListOf(resource));
		awardItems.rewardTakeAffectAndPush(player, Action.WAREHOUSE_SEND_BACK);
		
		BehaviorLogger.log4Service(player, Source.GUILD_MANOR,
				Action.WAREHOUSE_SEND_BACK,
				Params.valueOf("ressendBack", awardItems));
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
}
