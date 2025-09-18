package com.hawk.activity.type.impl.stronestleader.rank;

import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.rank.ActivityRankType;

/**
 * 最强指挥官总排名
 * @author PhilChen
 *
 */
public class StrongestTotalRankProvider extends StrongestStageRankProvider {

	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.STRONGEST_TOTALL_RANK;
	}
	
	protected String getRedisKey() {
		return ActivityRedisKey.STRONGEST_TOTAL_RANK;
	}
	

}
