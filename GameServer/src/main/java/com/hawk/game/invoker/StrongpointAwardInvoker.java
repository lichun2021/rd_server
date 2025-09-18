package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

public class StrongpointAwardInvoker extends HawkMsgInvoker {
	
	private Player player;
	private AwardItems award;
	
	public StrongpointAwardInvoker(Player player, AwardItems award) {
		this.player = player;
		this.award = award;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		award.rewardTakeAffectAndPush(player, Action.WORLD_STRONGPOINT_MARCH, false, RewardOrginType.KILL_MONSTER);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public AwardItems getAward() {
		return award;
	}

	public void setAward(AwardItems award) {
		this.award = award;
	}
	
}
