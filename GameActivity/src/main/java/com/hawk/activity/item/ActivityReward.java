package com.hawk.activity.item;

import java.util.List;

import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

public class ActivityReward {

	private List<RewardItem.Builder> rewardList;
	
	private RewardOrginType orginType;
	
	private int activityId;
	
	private boolean alert = false;
	
	private Action behaviorAction;
	
	/**
	 *  奖励原因: 非必填参数，需要填时参考 GsConst.DiamondPresentReason
	 */
	private String awardReason;

	public ActivityReward(List<RewardItem.Builder> rewardList, Action behaviorAction) {
		this.rewardList = rewardList;
		this.behaviorAction = behaviorAction;
	}
	
	public void setOrginType(RewardOrginType orginType, int activityId) {
		this.orginType = orginType;
		this.activityId = activityId;
	}
	
	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	public void setAction(Action behaviorAction) {
		this.behaviorAction = behaviorAction;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public RewardOrginType getOrginType() {
		return orginType;
	}
	
	public int getActivityId() {
		return activityId;
	}
	
	public boolean isAlert() {
		return alert;
	}
	
	public Action getBehaviorAction() {
		return behaviorAction;
	}
	
	public String getAwardReason() {
		return awardReason;
	}

	public void setAwardReason(String awardReason) {
		this.awardReason = awardReason;
	}
}
