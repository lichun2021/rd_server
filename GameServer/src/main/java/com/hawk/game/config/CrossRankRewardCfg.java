package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.CrossActivity.CrossRankType;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 排名奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_reward.xml")
public class CrossRankRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** */
	private final String name;
	/** 排行奖励类型*/
	private final int rewardType;
	/** 高排名*/
	private final int rankUpper;
	/** 低排名*/
	private final int rankLower;
	/** 奖励列表*/
	private final String reward;
	
	private CrossRankType rankType;
	
	private List<ItemInfo> rewardList;
	
	public CrossRankRewardCfg() {
		id = 0;
		name = "";
		rewardType = 0;
		rankUpper = 0;
		rankLower = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		rankType = CrossRankType.valueOf(rewardType);
		rewardList =ItemInfo.valueListOf(reward);
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityCircularRankRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getRankUpper() {
		return rankUpper;
	}

	public int getRankLower() {
		return rankLower;
	}
	
	public String getReward() {
		return reward;
	}
	
	public String getName() {
		return name;
	}

	public CrossRankType getRankType() {
		return rankType;
	}

	public List<ItemInfo> getRewardList() {
		List<ItemInfo> listCopy = new ArrayList<>();
		for(ItemInfo info : rewardList){
			listCopy.add(info.clone());
		}
		return listCopy;
	}
	
}
