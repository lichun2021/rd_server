package com.hawk.activity.type.impl.growUpBoost.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 *  中部养成计划
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/grow_up_boost/grow_up_boost_cfg.xml")
public class GrowUpBoostKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	// 每日积分上限
	private final int maxDailyScoreLimit;
	// 总积分上限
	private final int maxScoreLimit;
	// 积分奖励轮次
	private final int scoreRewardPages;
	// 兑换代币道具ID
	private final String exchangeItemId;
	//用于展示的道具
	private final String scoreItemId;
		
	private final String decomposeItemDate;
	
	private Map<Integer,Long> decomposeItemMap = new HashMap<>();
	
	public GrowUpBoostKVCfg() {
		serverDelay = 0;
		maxDailyScoreLimit = 0;
		maxScoreLimit = 0;
		scoreRewardPages = 0;
		exchangeItemId = "";
		scoreItemId = "";
		decomposeItemDate = "";
		
	}
	
	
	@Override
	protected boolean assemble() {
		Map<Integer,Long> decomposeItemMapTemp = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(decomposeItemDate)) {
			String[] arr = decomposeItemDate.split(",");
			for (String element : arr) {
				String[] eArr = element.split("_");
				if(eArr.length != 2){
					continue;
				}
				decomposeItemMapTemp.put(Integer.parseInt(eArr[0]), HawkTime.parseTime(eArr[1]));
			}
		}
		this.decomposeItemMap = decomposeItemMapTemp;
		return true;
	}


	public long getServerDelay() {
		return serverDelay  * 1000l;
	}


	public int getMaxDailyScoreLimit() {
		return maxDailyScoreLimit;
	}


	public int getMaxScoreLimit() {
		return maxScoreLimit;
	}


	public int getScoreRewardPages() {
		return scoreRewardPages;
	}


	public String getExchangeItemId() {
		return exchangeItemId;
	}

	public String getScoreItemId() {
		return scoreItemId;
	}
	

	public Map<Integer, Long> getDecomposeItemMap() {
		return decomposeItemMap;
	}
	
}