package com.hawk.activity.type.impl.continuousRecharge.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 连续充值活动配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/even_filling/even_filling.xml")
public class ContinuousRechargeCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int day;
	
	private final int count;
	
	private final String rewards;

	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	/**
	 * 配置数据<天，档次，cfg>
	 */
	private static Table<Integer, Integer, ContinuousRechargeCfg> cfgTable = HashBasedTable.create();
	
	public ContinuousRechargeCfg() {
		id =0;
		day = 0;
		count = 0;
		rewards = "";
	}

	public int getId() {
		return id;
	}

	public int getDay() {
		return day;
	}

	public int getCount() {
		return count;
	}

	public String getRewards() {
		return rewards;
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			cfgTable.put(day, count, this);
		} catch (Exception e) {
			HawkException.catchException(e, new Object[0]);
			return false;
		}
		
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("ContinuousRechargeCfg reward error, id:%s, rewards:%s", id, rewards));
		}
		return super.checkValid();
	}
	
	public static ContinuousRechargeCfg getConfig(int day, int count) {
		return cfgTable.get(day, count);
	}
	
	public static Map<Integer, ContinuousRechargeCfg> getDayConfig(int day) {
		return cfgTable.row(day);
	}
}
