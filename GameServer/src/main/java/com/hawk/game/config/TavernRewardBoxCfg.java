package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigBase.CombineId;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 酒馆宝箱奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tavern_reward_box.xml")
@CombineId(fields = {"boxId", "factoryLevel"})
public class TavernRewardBoxCfg extends HawkConfigBase {
	
	/** 宝箱id*/
	private final int boxId;
	/** 大本等级*/
	private final int factoryLevel;
	/** 奖励列表*/
	private final String rewards;

	private List<RewardItem.Builder> rewardList;
	
	public TavernRewardBoxCfg() {
		boxId = 0;
		factoryLevel = 0;
		rewards = "";
	}

	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public int getBoxId() {
		return boxId;
	}

	public int getFactoryLevel() {
		return factoryLevel;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public String getRewards() {
		return rewards;
	}
}
