package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 世界奖励发放调用
 * @author golden
 *
 */
public class WorldAwardPushInvoker extends HawkMsgInvoker {

	/**
	 * 玩家
	 */
	private Player player;
	
	/**
	 * 奖励
	 */
	private AwardItems award;
	
	/**
	 * 行为
	 */
	private Action action;
	
	/**
	 * 是否推送
	 */
	private boolean popup;
	
	/**
	 * 奖励来源
	 */
	private RewardOrginType orginType;

	public WorldAwardPushInvoker(Player player, AwardItems award, Action action, boolean popup, RewardOrginType orginType) {
		this.player = player;
		this.award = award;
		this.action = action;
		this.popup = popup;
		this.orginType = orginType;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		award.rewardTakeAffectAndPush(player, action, popup, orginType);
		return false;
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

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public boolean isPopup() {
		return popup;
	}

	public void setPopup(boolean popup) {
		this.popup = popup;
	}

	public RewardOrginType getOrginType() {
		return orginType;
	}

	public void setOrginType(RewardOrginType orginType) {
		this.orginType = orginType;
	}
}
