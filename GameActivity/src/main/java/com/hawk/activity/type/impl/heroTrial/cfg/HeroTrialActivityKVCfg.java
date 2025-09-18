package com.hawk.activity.type.impl.heroTrial.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 英雄试炼配置
 * 
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/hero_trial/hero_trial_cfg.xml")
public class HeroTrialActivityKVCfg extends HawkConfigBase {

	/**
	 * 
	 */
	private final int serverDelay;

	/**
	 * 自动刷新时间
	 */
	private final String autoRefreshTime;

	/**
	 * 每日接受任务数量
	 */
	private final int trialLimit;

	/**
	 * 刷新消耗
	 */
	private final String refreshCost;

	/**
	 * 一次刷新任务数量
	 */
	private final int refreshCount;

	/**
	 * 一次试炼英雄数量限制
	 */
	private final int oneTrialHeroLimit;
	
	/**
	 * 刷新次数限制
	 */
	private final int refresTimesLimit;

	protected List<RewardItem.Builder> refreshCostBuilder;
	
	protected int[] refreshTime;
	
	/**
	 * 单例
	 */
	private static HeroTrialActivityKVCfg instance;

	public static HeroTrialActivityKVCfg getInstance() {
		return instance;
	}

	public HeroTrialActivityKVCfg() {
		serverDelay = 0;
		autoRefreshTime = "";
		trialLimit = 10;
		refreshCost = "10000_1001_1";
		refreshCount = 3;
		oneTrialHeroLimit = 2;
		refresTimesLimit = 3;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		refreshCostBuilder = RewardHelper.toRewardItemImmutableList(refreshCost);
		
		if (!HawkOSOperator.isEmptyString(autoRefreshTime)) {
			String[] timeArray = autoRefreshTime.split("_");
			int[] intTimeArray = new int[timeArray.length];			
			for (int i = 0; i < timeArray.length; i++) {
				intTimeArray[i] =  Integer.parseInt(timeArray[i]);
			}
			
			refreshTime = intTimeArray;
		} else {
			throw new RuntimeException("heroTrial refresh time must not null or empty");
		}
		
		return true;
	}

	public String getAutoRefreshTime() {
		return autoRefreshTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public int getTrialLimit() {
		return trialLimit;
	}

	public String getRefreshCost() {
		return refreshCost;
	}

	public int getOneTrialHeroLimit() {
		return oneTrialHeroLimit;
	}

	public int getRefresTimesLimit() {
		return refresTimesLimit;
	}

	public List<RewardItem.Builder> getRefreshCostBuilder() {
		return refreshCostBuilder;
	}

	public int[] getRefreshTime() {
		return refreshTime;
	}
}