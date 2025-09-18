package com.hawk.activity.type.impl.timeLimitLogin.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 限时登录
 */
@HawkConfigManager.XmlResource(file = "activity/time_limit_login/time_limit_login_reward.xml")
public class TimeLimitLoginAwardCfg extends HawkConfigBase {
	/** id */
	@Id
	private final int id;
	/** 天数 */
	private final int day;
	/** 第几次 */
	private final int time;
	/** 奖励列表 */
	private final String reward;

	private List<RewardItem.Builder> rewardList;

	public TimeLimitLoginAwardCfg() {
		id = 0;
		day = 0;
		time = 0;
		reward = "";
	}

	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public String getReward() {
		return reward;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getId() {
		return id;
	}

	public int getDay() {
		return day;
	}

	public int getTime() {
		return time;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

}
