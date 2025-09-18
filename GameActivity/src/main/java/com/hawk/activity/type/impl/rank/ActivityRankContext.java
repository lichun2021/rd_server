package com.hawk.activity.type.impl.rank;

import java.util.HashMap;
import java.util.Map;

import org.hawk.os.HawkException;

public class ActivityRankContext {

	private static Map<ActivityRankType, ActivityRankProvider<? extends ActivityRank>> RANK_MAP = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T extends ActivityRank> ActivityRankProvider<T> getRankProvider(ActivityRankType rankType, Class<T> tClass) {
		ActivityRankProvider<? extends ActivityRank> rankProvider = RANK_MAP.get(rankType);
		if (rankProvider != null) {
			return (ActivityRankProvider<T>) rankProvider;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void init() {
		for (ActivityRankType rankType : ActivityRankType.values()) {
			try {
				Object rankProvider = rankType.getProviderClass().newInstance();
				RANK_MAP.put(rankType, (ActivityRankProvider<ActivityRank>) rankProvider);
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
		
		for (ActivityRankProvider<? extends ActivityRank> provider : RANK_MAP.values()) {
			provider.loadRank();
		}
	}

	/**
	 * 定时更新排行榜
	 */
	public static void updateRankList() {
		for (ActivityRankProvider<? extends ActivityRank> provider : RANK_MAP.values()) {
			if (provider.isFixTimeRank() == false) {
				continue;
			}
			provider.doRankSort();
		}
	}
}
