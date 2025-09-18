package com.hawk.activity.type.impl.order.activityNewOrder.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 占领等级配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/order_two/%s/order_two_level_cfg.xml", autoLoad=false, loadParams="211")
public class NewOrderLevelCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int level;

	/** 升级所需经验 */
	private final int levelUpExp;

	/** 普通奖励 */
	private final String normalReward;

	/** 进阶奖励 */
	private final String advReward;

	private List<RewardItem.Builder> normalRewardList;

	private List<RewardItem.Builder> advRewardList;

	public NewOrderLevelCfg() {
		level = 0;
		levelUpExp = 0;
		normalReward = "";
		advReward = "";
	}

	public int getLevel() {
		return level;
	}

	public int getLevelUpExp() {
		return levelUpExp;
	}

	public String getNormalReward() {
		return normalReward;
	}

	public String getAdvReward() {
		return advReward;
	}

	public List<RewardItem.Builder> getNormalRewardList() {
		List<RewardItem.Builder> copyList = new ArrayList<>();
		for (RewardItem.Builder builder : normalRewardList) {
			copyList.add(builder.clone());
		}
		return copyList;
	}

	public List<RewardItem.Builder> getAdvRewardList() {
		List<RewardItem.Builder> copyList = new ArrayList<>();
		for (RewardItem.Builder builder : advRewardList) {
			copyList.add(builder.clone());
		}
		return copyList;
	}

	@Override
	protected boolean assemble() {
		try {
			normalRewardList = RewardHelper.toRewardItemImmutableList(normalReward);
			advRewardList = RewardHelper.toRewardItemImmutableList(advReward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
