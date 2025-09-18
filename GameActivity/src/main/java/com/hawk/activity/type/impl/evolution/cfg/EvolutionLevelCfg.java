package com.hawk.activity.type.impl.evolution.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 英雄进化等级奖励配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/evoroad/evoroad_debris.xml")
public class EvolutionLevelCfg extends HawkConfigBase {
	@Id
	/** 奖池等级 */
	private final int level;
	/** 普通奖励 */
	private final String normalReward;

	private List<RewardItem.Builder> normalRewardList;

	public EvolutionLevelCfg() {
		level = 0;
		normalReward = "";
	}
	
	public int getLevel() {
		return level;
	}

	public String getNormalReward() {
		return normalReward;
	}

	public List<RewardItem.Builder> getNormalRewardList() {
		List<RewardItem.Builder> copyList = new ArrayList<>();
		for (RewardItem.Builder builder : normalRewardList) {
			copyList.add(builder.clone());
		}
		return copyList;
	}

	@Override
	protected boolean assemble() {
		try {
			normalRewardList = RewardHelper.toRewardItemImmutableList(normalReward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
