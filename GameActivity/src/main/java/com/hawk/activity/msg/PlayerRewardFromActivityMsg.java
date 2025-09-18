package com.hawk.activity.msg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.msg.HawkMsg;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 给玩家发放奖励
 * @author PhilChen
 *
 */
public class PlayerRewardFromActivityMsg extends HawkMsg {
	/** */
	private List<RewardItem.Builder> rewardList;
	/** */
	private Action behaviorAction;
	/** */
	private boolean alert;
	/** */
	private RewardOrginType orginType;
	/** */
	private List<Integer> orginArgs = new ArrayList<Integer>();
	/**
	 * 奖励是否需要合并,默认都需要合并
	 */
	private boolean merge = true;
	/**
	 * 奖励原因
	 */
	private String awardReason;
	
	private PlayerRewardFromActivityMsg() {
		super(MsgId.PLAYER_REWARD_FROM_ACTIVITY);
	}
	
	public static PlayerRewardFromActivityMsg valueOf(List<RewardItem.Builder> rewardList, Action behaviorAction, boolean alert) {
		return valueOf(rewardList, behaviorAction, alert, null, 0);
	}
	
	public static PlayerRewardFromActivityMsg valueOf(List<RewardItem.Builder> rewardList, Action behaviorAction, boolean alert, 
			RewardOrginType orginType, int activityId) {
		PlayerRewardFromActivityMsg msg = new PlayerRewardFromActivityMsg();
		msg.rewardList = rewardList;
		msg.behaviorAction = behaviorAction;
		msg.alert = alert;
		msg.orginType = orginType;
		msg.orginArgs.add(activityId);
		return msg;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public Action getBehaviorAction() {
		return behaviorAction;
	}

	public boolean isAlert() {
		return alert;
	}

	public RewardOrginType getOrginType() {
		return orginType;
	}

	public List<Integer> getOrginArgs() {
		return orginArgs;
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public String getAwardReason() {
		return awardReason;
	}

	public void setAwardReason(String awardReason) {
		this.awardReason = awardReason;
	}
}
