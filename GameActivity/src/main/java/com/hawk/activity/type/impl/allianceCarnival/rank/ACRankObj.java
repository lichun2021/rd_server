package com.hawk.activity.type.impl.allianceCarnival.rank;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.db.HawkDBManager;

import com.hawk.activity.type.impl.allianceCarnival.entity.AllianceCarnivalEntity;

/**
 * 联盟总动员排行榜
 * @author golden
 *
 */
public class ACRankObj {

	public Map<String, Map<String, ACRankInfo>> rankMap = new ConcurrentHashMap<>();

	/**
	 * 初始化
	 */
	public void init(int termId) {
		String sql = "select initGuildId, playerId, finishTimes, exp from activity_alliance_carnival where termId = " + termId;
		List<ACRankInfo> rankInfos = HawkDBManager.getInstance().executeQuery(sql, ACRankInfo.class);
		if (rankInfos == null) {
			return;
		}
		
		for (ACRankInfo rankInfo : rankInfos) {
			Map<String, ACRankInfo> guildRank = getGuildRank(rankInfo.getInitGuildId());
			guildRank.put(rankInfo.getPlayerId(), rankInfo);
		}
	}
	
	/**
	 * 获取联盟排行榜
	 */
	public Map<String, ACRankInfo> getGuildRank(String guildId) {
		Map<String, ACRankInfo> guildRank = rankMap.get(guildId);
		if (guildRank == null) {
			guildRank = new ConcurrentHashMap<>();
			rankMap.put(guildId, guildRank);
		}
		return guildRank;
	}
	
	/**
	 * 更新排名信息
	 */
	public void updateRankInfo(AllianceCarnivalEntity entity) {
		
		Map<String, ACRankInfo> guildRank = getGuildRank(entity.getInitGuildId());
		
		ACRankInfo info = new ACRankInfo();
		info.setInitGuildId(entity.getInitGuildId());
		info.setExp(entity.getExp());
		info.setFinishTimes(entity.getFinishTimes());
		info.setPlayerId(entity.getPlayerId());
		
		guildRank.put(entity.getPlayerId(), info);
	}
}
