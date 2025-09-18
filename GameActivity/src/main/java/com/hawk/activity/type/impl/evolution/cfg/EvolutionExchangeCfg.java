package com.hawk.activity.type.impl.evolution.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 英雄进化奖励兑换配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/evoroad/evoroad_level.xml")
public class EvolutionExchangeCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/** 奖池等级 */
	private final int level;
	
	/** 兑换奖励所需经验积分 */
	private final int exp;
	
	/** 普通奖励 */
	private final String normalReward;

	private List<RewardItem.Builder> normalRewardList;
	
	private static Map<Integer, List<Integer>> levelExchangeMap = new HashMap<Integer, List<Integer>>();

	public EvolutionExchangeCfg() {
		id= 0;
		level = 0;
		exp = 0;
		normalReward = "";
	}
	
	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getNormalReward() {
		return normalReward;
	}

	public int getExp() {
		return exp;
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
		
		List<Integer> list = levelExchangeMap.get(level);
		if (list == null) {
			list = new ArrayList<Integer>();
			levelExchangeMap.put(level, list);
		}
		
		if (exp > 0) {
			list.add(id);
		}
		
		return true;
	}
	
	public static List<Integer> getExchangeList(int level) {
		return levelExchangeMap.getOrDefault(level, Collections.emptyList());
	}

}
