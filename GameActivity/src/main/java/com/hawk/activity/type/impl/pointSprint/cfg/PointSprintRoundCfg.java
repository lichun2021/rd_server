package com.hawk.activity.type.impl.pointSprint.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 巅峰荣耀奖励配置
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/point_sprint/point_sprint_round.xml")
public class PointSprintRoundCfg extends HawkConfigBase {

	/**
	 * id
	 */
	@Id
	private final int round;

	/**
	 * 奖励
	 */
	private final String item;

	/**
	 * 奖励
	 */
	private List<RewardItem.Builder> reward;

	/**
	 * 构造
	 */
	public PointSprintRoundCfg() {
		round = 0;
		item = "";
	}

	public int getRound() {
		return round;
	}

	public void setReward(List<RewardItem.Builder> reward) {
		this.reward = reward;
	}

	public String getItem() {
		return item;
	}

	public List<RewardItem.Builder> getReward() {
		return new ArrayList<>(reward);
	}

	@Override
	protected boolean assemble() {
		try {
			reward = RewardHelper.toRewardItemImmutableList(item);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
}
