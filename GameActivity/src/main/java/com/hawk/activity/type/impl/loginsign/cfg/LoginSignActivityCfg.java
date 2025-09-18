package com.hawk.activity.type.impl.loginsign.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 登录签到活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/login_sign/login_sign_activity.xml")
public class LoginSignActivityCfg extends HawkConfigBase {
	
	/** */
	@Id
	private final int itemId;
	
	/** 建筑工厂最小等级(包含)*/
	private final int factoryLevelMin;
	
	/** 建筑工厂最大等级(包含)*/
	private final int factoryLevelMax;
	
	/** 天数*/
	private final int day;
	
	/** 达成奖励列表(通用奖励格式)*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	/** 配置缓存集合<factoryLevelMin, config>*/
	private static TreeMap<Integer, List<LoginSignActivityCfg>> CONFIG_MAP = new TreeMap<>();
	
	public static List<LoginSignActivityCfg> getConfigList(int factoryLevel) {
		Entry<Integer, List<LoginSignActivityCfg>> entry = CONFIG_MAP.floorEntry(factoryLevel);
		if (entry == null) {
			return Collections.emptyList();
		}
		return entry.getValue();
	}
	
	public LoginSignActivityCfg() {
		itemId = 0;
		factoryLevelMin = 0;
		factoryLevelMax = 0;
		day = 0;
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
		List<LoginSignActivityCfg> configList = CONFIG_MAP.get(factoryLevelMin);
		if (configList == null) {
			configList = new ArrayList<>();
			CONFIG_MAP.put(factoryLevelMin, configList);
		}
		configList.add(this);
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("LoginSignActivityCfg reward error, itemId: %s , rewards: %s", itemId, rewards));
		}
		return super.checkValid();
	}
	
	public int getItemId() {
		return itemId;
	}

	public int getDay() {
		return day;
	}

	public String getRewards() {
		return rewards;
	}
	
	public int getFactoryLevelMax() {
		return factoryLevelMax;
	}
	
	public int getFactoryLevelMin() {
		return factoryLevelMin;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	
}
