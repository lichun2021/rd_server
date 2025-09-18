package com.hawk.activity.type.impl.mergecompetition.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/merge_competition/%s/merge_competition_rank.xml", autoLoad=false, loadParams="368")
public class MergeCompetitionRankCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/** 排名类型：1-个人去兵战力，2-联盟去兵战力，3-个人体力消耗，4-嘉奖礼包积分，5-区服排名（胜利/失败） */
	private final int type;
	
	/** 排名区间 */
	private final int min;
	private final int max;

	/** 奖励 */
	private final String rewards;

	/** 区服积分 */
	private final int serverPoint;
	
	static Map<Integer, List<MergeCompetitionRankCfg>> rankTypeConfigMap = new HashMap<>();
	
	public MergeCompetitionRankCfg() {
		id = 0;
		type = 0;
		min = 0;
		max = 0;
		rewards = "";
		serverPoint = 0;
	}
	
	@Override
	protected boolean assemble() {
		List<MergeCompetitionRankCfg> list = rankTypeConfigMap.get(type);
		if (list == null) {
			list = new ArrayList<>();
			rankTypeConfigMap.put(type, list);
		}
		list.add(this);
		return true;
	}
	
	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getRankUpper() {
		return min;
	}

	public int getRankLower() {
		return max;
	}

	public String getRewards() {
		return rewards;
	}

	public int getServerPoint() {
		return serverPoint;
	}
	
	public static List<MergeCompetitionRankCfg> getConfigByType(int type) {
		return rankTypeConfigMap.get(type);
	}

}
