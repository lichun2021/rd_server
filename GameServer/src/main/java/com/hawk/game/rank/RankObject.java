package com.hawk.game.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.GsConfig;
import com.hawk.game.config.RankCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.RankGroup;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.QQScoreBatch;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.log.Action;
import com.hawk.log.Source;

import redis.clients.jedis.Tuple;

public class RankObject {
	/**
	 * 榜单id
	 */
	private RankType rankType;
	/**
	 * 排序好的榜单
	 */
	List<RankInfo> sortedRank;
	
	/**
	 * 最大显示范围内排行信息
	 */
	Map<String, RankInfo> rankInfoMap;
	
	/**
	 * 最大排行范围内的榜单数据
	 */
	Map<String, HawkTuple2<Integer, Long>> rankDataMap;
	
	/*
	 * 构造榜单对象
	 */
	public RankObject(RankType rankType) {
		this.rankType = rankType;
		sortedRank = new ArrayList<>();
		rankInfoMap = new ConcurrentHashMap<>();
		rankDataMap = new ConcurrentHashMap<>();
	}

	/**
	 * 获取榜单类型
	 * 
	 * @return
	 */
	public RankType getRankType() {
		return rankType;
	}

	/**
	 * 获取排序榜单数量
	 * 
	 * @return
	 */
	public int getSortedCount() {
		return sortedRank.size();
	}
	
	/**
	 * 获取制定主键的排行数据(展示范围内)
	 * 
	 * @param key
	 * @return
	 */
	public RankInfo getRankInfo(String key) {
		return rankInfoMap.get(key);
	}

	/**
	 * 获取排行列表
	 * @return
	 */
	public List<RankInfo> getSortedRank() {
		return sortedRank;
	}
	
	/**
	 * 获取榜单内排行数据
	 * @param key
	 * @return
	 */
	public HawkTuple2<Integer, Long> getRankTuple(String key){
		return rankDataMap.get(key);
	}

	/**
	 * 更新或者添加榜单项目数据
	 * 
	 * @param key
	 * @param score
	 * @param userData
	 */
	public void updateRank(Set<Tuple> rankSet, RankCfg cfg) {
		try {
			Map<String, RankInfo> infoMap = new ConcurrentHashMap<String, RankInfo>();
			List<RankInfo> rankList = new ArrayList<>(infoMap.size());
			Map<String, HawkTuple2<Integer, Long>> dataMap = new ConcurrentHashMap<>();

			int rank = 1;
			int rankCount = cfg.getRankCount();
			if (RankService.getInstance().isGuildTypeRank(rankType)) {
				for (Tuple tuple : rankSet) {
					String guildId = tuple.getElement();
					GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
					if (guildInfo == null) {
						continue;
					}
					long realScore = RankService.getInstance().getRealScore(rankType, (long) tuple.getScore());
					dataMap.put(guildId, new HawkTuple2<Integer, Long>(rank, realScore));
					if (rank <= rankCount) {
						RankInfo rankInfo = buildRankInfo(rank, tuple.getElement(), realScore, null, guildInfo, RankGroup.ALLIANCE_TYPE_VALUE);
						infoMap.put(guildId, rankInfo);
						rankList.add(rankInfo);
					}
					
					// 手Q成就上报 - 在线的联盟成员需要上报，不在线的则不用报
					if (GsConfig.getInstance().isScoreBatchEnable()) {
						HawkTuple2<Integer, Long> oldRank = rankDataMap.get(guildId);
						if (oldRank == null || oldRank.first != rank) {
							for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
								Player player = GlobalData.getInstance().getActivePlayer(playerId);
								if (player == null || !GameUtil.isScoreBatchEnable(player)) {
									continue;
								}
								
								long expireTime = HawkApp.getInstance().getCurrentTime() + cfg.getPeriod();
								if (rankType == RankType.ALLIANCE_KILL_ENEMY_KEY) {
									QQScoreBatch.getInstance().scoreBatch(player,ScoreType.GUILD_KILL_RANK, rank, 0, String.valueOf(expireTime /1000));
								} else if (rankType == RankType.ALLIANCE_FIGHT_KEY) {
									QQScoreBatch.getInstance().scoreBatch(player,ScoreType.GUILD_POWER_RANK, rank, 0, String.valueOf(expireTime /1000));
								}
							}
						}
					}
					
					rank++;
				}
			} else {
				int idx = 0;
				String[] playerIds = new String[rankSet.size()];
				for (Tuple tuple : rankSet) {
					playerIds[idx++] = tuple.getElement();
				}

				Map<String, Player> snapshotMap = GlobalData.getInstance().getPlayerMap(playerIds);
				for (Tuple tuple : rankSet) {
					String playerId = tuple.getElement();
					Player playerInfo = snapshotMap.get(playerId);
					if (playerInfo == null) {
						continue;
					}
					// NPC玩家不上榜
					if(GameUtil.isNpcPlayer(playerId)){
						continue;
					}
					
					GuildInfoObject guildInfo = null;
					if (playerInfo.hasGuild()) {
						guildInfo = GuildService.getInstance().getGuildInfoObject(playerInfo.getGuildId());
					}
					
					long realScore = RankService.getInstance().getRealScore(rankType, (long) tuple.getScore());
					dataMap.put(playerId, new HawkTuple2<Integer, Long>(rank, realScore));
					if (rank <= rankCount) {
						RankInfo rankInfo = buildRankInfo(rank, tuple.getElement(), realScore, playerInfo, guildInfo, RankGroup.PLAYER_TYPE_VALUE);
						infoMap.put(playerInfo.getId(), rankInfo);
						rankList.add(rankInfo);
					}
					
					// 手Q成就上报 - 在线玩家需要上报
					if (GsConfig.getInstance().isScoreBatchEnable()) {
						HawkTuple2<Integer, Long> oldRank = rankDataMap.get(playerId);
						if (rankType == RankType.PLAYER_FIGHT_RANK && playerInfo.isActiveOnline() && (oldRank == null || oldRank.first != rank)) {
							long expireTime = HawkApp.getInstance().getCurrentTime() + cfg.getPeriod();
							QQScoreBatch.getInstance().scoreBatch(playerInfo,ScoreType.PLAYER_POWER_RANK, rank, 0, String.valueOf(expireTime /1000));
						}
					}
					
					rank++;
				}
			}
			// 替换现有数据
			this.rankInfoMap = infoMap;
			this.sortedRank = rankList;
			this.rankDataMap = dataMap;
			if (!sortedRank.isEmpty()) {
				BehaviorLogger.log4Service(null, Source.UNKNOWN_SOURCE, Action.RANK_UPDATE,
						Params.valueOf("rankType", rankType),
						Params.valueOf("ids", sortedRank.stream().map(RankInfo::getId).collect(Collectors.joining(","))));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 构建排行信息
	 * @param rankType
	 * @param rank
	 * @param tuple 排行数据
	 * @param playerInfo
	 * @param guildInfo
	 * @param rankGroup
	 * @return
	 */
	public RankInfo buildRankInfo(int rank, String rankKey, long score, Player playerInfo, GuildInfoObject guildInfo, int rankGroup) {
		boolean isGuildRank = RankService.getInstance().isGuildTypeRank(rankType);
		RankInfo.Builder rankInfo = RankInfo.newBuilder();
		rankInfo.setRankType(rankType);
		rankInfo.setId(rankKey);
		if (isGuildRank) {
			rankInfo.setPlayerName(guildInfo.getLeaderName());
			rankInfo.setAllianceIcon(guildInfo.getFlagId());
		} else {
			rankInfo.setPlayerName(playerInfo.getName());
			rankInfo.setIcon(playerInfo.getIcon());
			rankInfo.setVipLevel(playerInfo.getVipLevel());
			rankInfo.setCommon(BuilderUtil.genPlayerCommonBuilder(playerInfo));
			if (!HawkOSOperator.isEmptyString(playerInfo.getPfIcon())) {
				rankInfo.setPfIcon(playerInfo.getPfIcon());
			}
		}
		if (guildInfo == null) {
			rankInfo.setAllianceName("");
			rankInfo.setGuildTag("");
		} else {
			rankInfo.setAllianceName(guildInfo.getName());
			rankInfo.setGuildTag(guildInfo.getTag());
		}
		rankInfo.setRank(rank);
		rankInfo.setRankKey(rankKey);
		rankInfo.setRankInfoValue(score);
		rankInfo.setRankGrop(rankGroup);
		
		// 大本等级排行
		if (rankType.equals(RankType.PLAYER_CASTLE_KEY) && score >= 40) {
			rankInfo.setRankTime(GlobalData.getInstance().getCityRankTime(playerInfo.getId()));
		}
		return rankInfo.build();
	}
	
	public Map<String, HawkTuple2<Integer, Long>> getRankDataMap() {
		return rankDataMap;
	}
}
