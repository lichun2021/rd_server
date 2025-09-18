package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildAssistanceEvent;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGuildDeal;
import com.hawk.game.util.GsConst;

public class AssistanceResMarchReachMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	private Player tarPlayer;
	
	public AssistanceResMarchReachMsgInvoker(Player player, Player tarPlayer, String assistants, int tradeTaxRate) {
		super(assistants, tradeTaxRate);
		this.player = player;
		this.tarPlayer = tarPlayer;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		String awardStr = getParam(0);
		int taxRate = getParam(1); // 税率，万分比
		if (HawkOSOperator.isEmptyString(awardStr) || taxRate < 0) {
			return false;
		}
		AwardItems awardItems = AwardItems.valueOf(awardStr);
		GuildAssistanceEvent event = new GuildAssistanceEvent(player.getId());
		for (ItemInfo itemInfo : awardItems.getAwardItems()) {
			int count = (int) Math.max(1, GsConst.EFF_PER * (10000 - taxRate) * itemInfo.getCount());
			// 任务刷新
			MissionManager.getInstance().postMsg(player, new EventGuildDeal(itemInfo.getItemId(), count));
			event.addRes(itemInfo.getItemId(), (int)itemInfo.getCount());
			itemInfo.setCount(count);
		}
		// 活动事件
		ActivityManager.getInstance().postEvent(event);
		// 被支援方获取支援方送来的资源
		awardItems.rewardTakeAffectAndPush(tarPlayer, Action.ASSISTANT_RES);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.ASSISTANT_RES,
				Params.valueOf("receiverPlayerId", tarPlayer.getId()),
				Params.valueOf("receiverPlayerName", tarPlayer.getName()),
				Params.valueOf("taxRate", taxRate), Params.valueOf("receiverRes", awardStr));
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public Player getTarPlayer() {
		return tarPlayer;
	}

}
