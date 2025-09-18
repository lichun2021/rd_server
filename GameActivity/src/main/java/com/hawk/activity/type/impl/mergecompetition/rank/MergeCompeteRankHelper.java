package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

/**
 * helper
 */
public class MergeCompeteRankHelper {

	/**
	 * 排行榜玩家信息
	 */
	private Map<String, RankPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
	/**
	 * 排行榜联盟信息
	 */
	private Map<String, RankGuildInfo> guildInfoMap = new ConcurrentHashMap<>();
	
	private Map<String, Double> rankContributeScoreMap = new ConcurrentHashMap<>();
	private Set<Integer> rankTypes = new ConcurrentHashSet<>();
	
	private Set<String> cachePlayerIds = new ConcurrentHashSet<>();
	private Set<String> cacheGuildIds = new ConcurrentHashSet<>();
	
	private String serverGroup = "";
	

	public Map<String, RankPlayerInfo> getPlayerInfoMap() {
		return playerInfoMap;
	}

	public Map<String, RankGuildInfo> getGuildInfoMap() {
		return guildInfoMap;
	}

	public Map<String, Double> getRankContributeScoreMap() {
		return rankContributeScoreMap;
	}

	public Set<Integer> getRankTypes() {
		return rankTypes;
	}
	
	public void contributeServerScore(int rankType, Map<String, Double> serverPointMap, List<String> memberIds, String oppServerId) {
		rankTypes.add(rankType);
		if (rankType == MergeCompetitionConst.RANK_TYPE_GUILD_POWER) {
			cacheGuildIds.addAll(memberIds);
		} else {
			cachePlayerIds.addAll(memberIds);
		}
		
		//区服积分存储
		for (Entry<String, Double> entry : serverPointMap.entrySet()) {
			rankContributeScoreMap.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1 + v2);
		}
		
		if (rankTypes.size() < 4) {
			return;
		}
		
		HawkLog.logPrintln("MergeCompetitionActivity rankSort merge serverPoint, serverPointMap: {}", rankContributeScoreMap);
		
		rankTypes.clear();
		if (!rankContributeScoreMap.isEmpty() && !HawkOSOperator.isEmptyString(serverGroup)) {
			String redisKey = getServerScoreRedisKey();
			getRedis().zAdd(redisKey, rankContributeScoreMap, getRedisExpire());
			logServerScoreCalc(rankContributeScoreMap, oppServerId); //结算区服比拼奖励
			rankContributeScoreMap.clear();
		}
		
		//玩家信息更新
		refreshPlayerInfo(cachePlayerIds);
		refreshGuildInfo(cacheGuildIds);
		cachePlayerIds.clear();
		cacheGuildIds.clear();
	}
	
	/**
	 * 结算区服比拼奖励
	 * @param rankServerId
	 */
	private void logServerScoreCalc(Map<String, Double> serverPointMap, String oppServerId) {
		try {
			ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
			String serverId = dataGeter.getServerId();
			double serverPoint = serverPointMap.getOrDefault(serverId, 0D);
			double oppServerPoint = serverPointMap.getOrDefault(oppServerId, 0D);
			Map<String, Object> param = new HashMap<>();
			param.put("serverId", serverId);       //本区服
			param.put("serverScore", serverPoint); //本区服积分
			param.put("oppServerId", oppServerId);       //对手区服
			param.put("oppServerScore", oppServerPoint); //对手区服积分
			dataGeter.logActivityCommon(LogInfoType.merge_compete_server_score, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取区服积分排名数据
	 * @return
	 */
	public List<MergeCompetitionRank> getServerScore() {
		if (HawkOSOperator.isEmptyString(serverGroup)) {
			return Collections.emptyList();
		}
		List<MergeCompetitionRank> rankList = new ArrayList<>();
		String redisKey = getServerScoreRedisKey();
		int index = 1;
		Set<Tuple> rankSet = getRedis().zRevrangeWithScores(redisKey, 0, -1, getRedisExpire());
		for (Tuple rank : rankSet) {
			MergeCompetitionRank mcrank = new MergeCompetitionRank();
			mcrank.setId(rank.getElement());
			mcrank.setRank(index);
			mcrank.setScore((long)rank.getScore());
			rankList.add(mcrank);
			index++;
		}
		return rankList;
	}
	
	public void refreshPlayerInfo(Set<String> memberIds) {
		if (memberIds.isEmpty() || HawkOSOperator.isEmptyString(serverGroup)) {
			return;
		}
		try {
			String key = getPlayerInfoKey();
			Map<String, RankPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
			String[] playerIdArray = memberIds.toArray(new String[0]);
			List<String> playerInfoList = getRedis().hmGet(key, playerIdArray);
			for (String playerInfoStr : playerInfoList) {
				RankPlayerInfo playerInfo = SerializeHelper.getValue(RankPlayerInfo.class, playerInfoStr, SerializeHelper.COLON_ITEMS);
				playerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
			}
			this.playerInfoMap = playerInfoMap;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public void refreshGuildInfo(Set<String> memberIds) {
		if (memberIds.isEmpty() || HawkOSOperator.isEmptyString(serverGroup)) {
			return;
		}
		try {
			Map<String, RankGuildInfo> guildInfoMap = new ConcurrentHashMap<>();
			String[] guildIdArray = memberIds.toArray(new String[0]);
			List<String> guildInfoList = getRedis().hmGet(getGuildInfoKey(), guildIdArray);
			for (String gInfoStr : guildInfoList) {
				RankGuildInfo guildInfo = SerializeHelper.getValue(RankGuildInfo.class, gInfoStr, SerializeHelper.COLON_ITEMS);
				guildInfoMap.put(guildInfo.getGuildId(), guildInfo);
			}
			this.guildInfoMap = guildInfoMap;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新玩家信息
	 * @param playerId
	 * @param info
	 */
	public void updatePlayerInfo(String playerId, RankPlayerInfo info) {
		if (HawkOSOperator.isEmptyString(serverGroup)) {
			return;
		}
		playerInfoMap.put(playerId, info);
		getRedis().hSet(getPlayerInfoKey(), playerId, info.toString(), getRedisExpire());
	}
	
	/**
	 * 获取玩家信息
	 * @param playerId
	 * @return
	 */
	public RankPlayerInfo getPlayerInfo(String playerId, boolean fromRedis) {
		RankPlayerInfo playerInfo = playerInfoMap.get(playerId);
		if (playerInfo != null && !fromRedis) {
			return playerInfo;
		}
		
		String playerInfoStr = getRedis().hGet(getPlayerInfoKey(), playerId, getRedisExpire());
		playerInfo = RankPlayerInfo.parseObj(playerInfoStr);
		playerInfoMap.put(playerId, playerInfo);
		return playerInfo;
	}
	
	/**
	 * 更新联盟信息
	 * @param playerId
	 * @param info
	 */
	public void updateGuildInfo(String guildId, RankGuildInfo info) {
		if (HawkOSOperator.isEmptyString(serverGroup)) {
			return;
		}
		guildInfoMap.put(guildId, info);
		getRedis().hSet(getGuildInfoKey(), guildId, info.toString(), getRedisExpire());
	}
	
	/**
	 * 获取玩家信息
	 * @param playerId
	 * @return
	 */
	public RankGuildInfo getGuildInfo(String guildId, boolean fromRedis) {
		RankGuildInfo guildInfo = guildInfoMap.get(guildId);
		if (guildInfo != null && !fromRedis) {
			return guildInfo;
		}
		
		String guildInfoStr = getRedis().hGet(getGuildInfoKey(), guildId, getRedisExpire());
		guildInfo = RankGuildInfo.parseObj(guildInfoStr);
		guildInfoMap.put(guildId, guildInfo);
		return guildInfo;
	}
	
	public String getServerScoreRedisKey() {
		return MergeCompetitionConst.RANK_SERVER_SCORE + serverGroup;
	}
	
	public String getPlayerInfoKey() {
		return MergeCompetitionConst.RANK_PLAYER_INFO + serverGroup;
	}
	
	public String getGuildInfoKey() {
		return MergeCompetitionConst.RANK_GUILD_INFO + serverGroup;
	}
	
	public HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	public int getRedisExpire() {
		return 3600 * 24 * 30;
	}

	public String getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(String serverGroup) {
		this.serverGroup = serverGroup;
	}
	
}
