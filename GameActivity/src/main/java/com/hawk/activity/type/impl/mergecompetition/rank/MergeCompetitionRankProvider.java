package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import redis.clients.jedis.Tuple;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionActivity;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionRankCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.game.protocol.Activity.MCRankPB;
import com.hawk.game.protocol.Activity.MergeCompeteRank;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.LogConst.LogInfoType;


public abstract class MergeCompetitionRankProvider extends AbstractActivityRankProvider<MergeCompetitionRank> {
	/**
	 * 排行榜数据本地缓存
	 */
	private List<MergeCompetitionRank> showList = new ArrayList<>();
	
	/**
	 * 给区服贡献积分的上榜玩家（或联盟）
	 */
	private List<String> contributeList = new ArrayList<>();
	
	/**
	 * 将缓存数据刷新到redis
	 */
	public abstract void refreshCacheData();
	
	/**
	 * 本服前X名给区服贡献积分
	 */
	public abstract int getLocalRankSize();
	
	/**
	 * 获取服务器混排的rediskey
	 */
	public abstract String getRedisKey();
	
	/**
	 * 获取本服单排的rediskey
	 */
	public abstract String getLocalRedisKey(String serverId);
	
	public abstract int getRankSize();
	
	/**
	 * 获取本活动内部的排行榜类型：1-个人去兵战力，2-联盟去兵战力，3-个人体力消耗，4-嘉奖礼包积分，5-区服排名（胜利/失败）
	 */
	public abstract int getActivityRankType();
	
	public abstract String getRankTypeStr();
	
	
	@Override
	public boolean isFixTimeRank() {
		MergeCompetitionActivity activity = getActivity();
		return activity == null ? false : getActivity().isOpening("");
	}

	@Override
	public void loadRank() {
		this.doRankSort();
	}

	@Override
	public void doRankSort() {
		MergeCompetitionActivity activity = getActivity();
		if (activity == null || activity.isHidden("")) {
			return;
		}
		
		HawkLog.logPrintln("MergeCompetitionActivity rankSort: {}", this.getRankTypeStr());
		List<String> memberIds = new ArrayList<>();
		Map<String, Double> serverPointMap = new HashMap<>(); //区服积分数据
		String oppServerId = "", serverId = ActivityManager.getInstance().getDataGeter().getServerId();

		try {
			//先把内存缓存数据刷到redis中
			this.refreshCacheData();
			
			int rankSize = this.getRankSize();
			Map<String, Set<String>> serverLocalRankElementsMap = new HashMap<>(); //本服（相对于server本身来说）排名头部玩家数据
			List<MergeCompetitionRank> newRankList = new ArrayList<>(rankSize);
			contributeList.clear();
			Set<Tuple> rankSet = getRedis().zRevrangeWithScores(this.getRedisKey(), 0, Math.max((rankSize - 1), 0), getRedisExpire());		
			int index = 1;
			for (Tuple rank : rankSet) {
				String[] elements = rank.getElement().split(":");
				String rankServerId = elements[0];
				String memberId = elements[1];
				if (!rankServerId.equals(serverId)) {
					oppServerId = rankServerId;
				}
				MergeCompetitionRank mcrank = new MergeCompetitionRank();
				mcrank.setId(memberId);
				mcrank.setRank(index);
				mcrank.setScore(RankScoreHelper.getRealScore((long) rank.getScore()));
				newRankList.add(mcrank);
				index++;
				
				memberIds.add(memberId);
				serverPointCalc(rankServerId, mcrank, serverLocalRankElementsMap, serverPointMap);
			}
			
			showList = newRankList;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("MergeCompetitionActivity rankSort: {}, serverPointMap: {}", this.getRankTypeStr(), serverPointMap);

		int rankType = this.getActivityRankType();
		contributeServerScore(rankType, serverPointMap, memberIds, oppServerId);
	}
	
	/**
	 * 为区服贡献积分
	 * @param rankType
	 * @param serverPointMap
	 * @param memberIds
	 * @param oppServerId
	 */
	public void contributeServerScore(int rankType, Map<String, Double> serverPointMap, List<String> memberIds, String oppServerId) {
		this.getRankHelper().contributeServerScore(rankType, serverPointMap, memberIds, oppServerId);
	}
	
	/**
	 * 区服积分计算
	 * @param rankServerId
	 * @param mcrank
	 * @param serverLocalSetMap
	 * @param serverPointMap
	 */
	private void serverPointCalc(String rankServerId, MergeCompetitionRank mcrank, Map<String, Set<String>> serverLocalRankElementsMap, Map<String, Double> serverPointMap) {
		List<MergeCompetitionRankCfg> configList = MergeCompetitionRankCfg.getConfigByType(this.getActivityRankType());
		Set<String> serverLocalSet = serverLocalRankElementsMap.get(rankServerId);
		if (serverLocalSet == null) {
			serverLocalSet = getServerLocalRankElements(rankServerId);
			serverLocalRankElementsMap.put(rankServerId, serverLocalSet);
		}
		double localServerPoint = serverPointMap.getOrDefault(rankServerId, 0D);
		Optional<MergeCompetitionRankCfg> optional = configList.stream().filter(e -> e.getRankUpper() <= mcrank.getRank() && e.getRankLower() >= mcrank.getRank()).findFirst();
		//排行行榜前X名，且本服前K名的玩家，可为服务器贡献服务器积分
		if (optional.isPresent() && serverLocalSet.contains(mcrank.getId())) {
			MergeCompetitionRankCfg cfg = optional.get();
			localServerPoint += cfg.getServerPoint();
			contributeList.add(mcrank.getId());
			HawkLog.logPrintln("MergeCompetitionActivity rankSort: {}, memberId: {}, rank: {}, addPoint: {}", this.getRankTypeStr(), mcrank.getId(), mcrank.getRank(), cfg.getServerPoint());
			
			//三大比拼排行榜结算
			logCompeteRankCalc(mcrank.getId(), rankServerId, mcrank.getRank(), cfg.getServerPoint());
		}
		serverPointMap.put(rankServerId, localServerPoint);
	}
	
	/**
	 * 三大比拼排行榜结算
	 * @param memberId
	 * @param rankServerId
	 * @param rank
	 * @param point
	 */
	private void logCompeteRankCalc(String memberId, String rankServerId, int rank, int point) {
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		if (!dataGeter.getServerId().equals(rankServerId)) {
			return;
		}
		
		int rankType = this.getActivityRankType();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				Map<String, Object> param = new HashMap<>();
				param.put("rankType", rankType);  //比拼类型（个人去兵战力、联盟去兵战力、个人体力消耗）
				param.put("rankIndex", rank);     //排名
				param.put("addScore", point);     //为区服提供的积分数量
				if (rankType == MergeCompetitionConst.RANK_TYPE_GUILD_POWER) {
					param.put("guildId", memberId); //玩家所属联盟id
					param.put("playerId", "");      //玩家id
				} else {
					String guildId = dataGeter.getGuildId(memberId);
					param.put("guildId", guildId == null ? "" : guildId); //玩家所属联盟id
					param.put("playerId", memberId);                      //玩家id
				}
				dataGeter.logActivityCommon(LogInfoType.merge_compete_rank_calc, param);
				return null;
			}
		});
	}
	
	/**
	 * 获取本服（相对于serverId来说）排名得头部成员
	 * @param serverId
	 * @return
	 */
	private Set<String> getServerLocalRankElements(String serverId) {
		Set<String> set = new HashSet<>();
		int localRankSize = this.getLocalRankSize();
		Set<Tuple> serverLocalRankSet = getRedis().zRevrangeWithScores(this.getLocalRedisKey(serverId), 0, Math.max((localRankSize - 1), 0), getRedisExpire());		
		for (Tuple rank : serverLocalRankSet) {
			String memberId = getMemberId(rank.getElement());
			set.add(memberId);
		}
		return set;
	}
	
	public void refreshPlayerInfo(Set<String> memberIds) {
		getRankHelper().refreshPlayerInfo(memberIds);
	}
	
	@Override
	public void clean() {
		showList  = new ArrayList<>();
		ActivityGlobalRedis.getInstance().del(this.getRedisKey());		
	}

	@Override
	public void addScore(String memberId, int score) {		
		throw new UnsupportedOperationException("addScore unsupport"); //都是全量得更新，不需要增量添加  
	}

	@Override
	public void remMember(String memberId) {
		if (memberId != null && memberId.indexOf(":") < 0) {
			memberId = getRankElementId(memberId);
		}
		ActivityGlobalRedis.getInstance().zrem(getRedisKey(), memberId); 
	}
	
	public String getRankElementId(String memberId) {
		return ActivityManager.getInstance().getDataGeter().getServerId() + ":" + memberId;
	}
	
	public String getMemberId(String rankElement) {
		String[] infos = rankElement.split(":");
		return infos.length > 1 ? infos[1] : infos[0];
	}

	@Override
	public MergeCompetitionRank getRank(String memberId) {
		String redisKey = getRedisKey();
		String rankElement = getRankElementId(memberId);
		Long rankVal = getRedis().zrevrank(redisKey, rankElement, getRedisExpire());
		Double scoreVal = getRedis().zScore(redisKey, rankElement, getRedisExpire());
		int rank = 0;
		long score = 0;
		if (rankVal != null && scoreVal != null) {
			rank = rankVal.intValue() + 1;
			score = RankScoreHelper.getRealScore(scoreVal.longValue());
		}
		MergeCompetitionRank strongestRank = new MergeCompetitionRank();
		strongestRank.setId(memberId);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		return strongestRank;
	}
		
	@Override
	public List<MergeCompetitionRank> getRanks(int start, int end) {
		if (start > showList.size()) {
			return Collections.emptyList();
		}
		start = start > 0 ? start - 1 : start;
		end = end > showList.size() ? showList.size() : end;
		return showList.subList(start, end);
	}
		
	@Override
	protected boolean canInsertIntoRank(MergeCompetitionRank rankInfo) {
		return true;
	}

	@Override
	public List<MergeCompetitionRank> getRankList() {
		return showList;
	}
	
	public void resetShowList(List<MergeCompetitionRank> list) {
		showList = list;
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
	
	public HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	public int getRedisExpire() {
		return 3600 * 24 * 30;
	}

	public MergeCompetitionActivity getActivity() {
		Optional<MergeCompetitionActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MERGE_COMPETITION.intValue());
		if (!opActivity.isPresent()) {
			return null;
		}
		return opActivity.get();
	}
	
	public MergeCompeteRankHelper getRankHelper() {
		return getActivity().getRankHelper();
	}
	
	public RankPlayerInfo getPlayerInfo(String playerId, boolean fromRedis) {
		return getRankHelper().getPlayerInfo(playerId, fromRedis);
	}
	
	public RankGuildInfo getGuildInfo(String guildId, boolean fromRedis) {
		return getRankHelper().getGuildInfo(guildId, fromRedis);
	}
	
	public List<String> getContributeList() {
		return contributeList;
	}
	
	/**
	 * 构建榜单数据
	 * @param memberId
	 * @param builder
	 */
	public void buildActivityRank(String playerId, MergeCompeteRank.Builder builder, long selfScore) {
		List<MergeCompetitionRank> rankList = this.getRankList();
		for (MergeCompetitionRank rank : rankList) {
			MCRankPB.Builder rankBuilder = buildRankPB(rank.getId(), rank, rank.getScore());
			builder.addRankData(rankBuilder);
		}
		
		MergeCompetitionRank rankData = this.getRank(playerId);
		MCRankPB.Builder rankBuilder = buildRankPB(playerId, rankData, selfScore);
		builder.setMyRank(rankBuilder);
	}

	/**
	 * 组装排名协议数据
	 * @param playerRank
	 * @return
	 */
	public MCRankPB.Builder buildRankPB(String playerId, MergeCompetitionRank rank, long score) {
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String rankPlayerServer = dataGeter.getPlayerMainServerId(rank.getId());
		String serverId = dataGeter.getServerId();
		boolean ownServer = (!HawkOSOperator.isEmptyString(rankPlayerServer) && rankPlayerServer.equals(serverId)); // 是否是本服玩家
		
		List<MergeCompetitionRankCfg> configList = MergeCompetitionRankCfg.getConfigByType(this.getActivityRankType());
		Optional<MergeCompetitionRankCfg> optional = configList.stream().filter(e -> e.getRankUpper() <= rank.getRank() && e.getRankLower() >= rank.getRank()).findFirst();
		
		MCRankPB.Builder rankPBBuilder = MCRankPB.newBuilder();
		rankPBBuilder.setPlayerId(rank.getId());
		rankPBBuilder.setRank(rank.getRank());
		rankPBBuilder.setScore(score);
		rankPBBuilder.setServerScoreRank(optional.isPresent() ? optional.get().getServerPoint() : 0);
		rankPBBuilder.setAddServerScore(contributeList.contains(rank.getId()) ? 1 : 0);
		if (ownServer) {
			String rankPlayerId = rank.getId();
			String playerName = dataGeter.getPlayerName(rankPlayerId);
			String guildTag = dataGeter.getGuildTagByPlayerId(rankPlayerId);
			rankPBBuilder.setServerId(serverId);
			rankPBBuilder.setPlayerName(playerName);
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				rankPBBuilder.setGuildTag(guildTag);
			}
			rankPBBuilder.setIcon(dataGeter.getIcon(rankPlayerId));
			String pfIcon = dataGeter.getPfIcon(rankPlayerId);
			if (!HawkOSOperator.isEmptyString(pfIcon)) {
				rankPBBuilder.setPfIcon(pfIcon);
			}
		} else {
			RankPlayerInfo playerInfo = this.getPlayerInfo(playerId, false);
			rankPBBuilder.setServerId(playerInfo.getServerId());
			rankPBBuilder.setPlayerName(playerInfo.getPlayerName());
			if (!HawkOSOperator.isEmptyString(playerInfo.getGuildTag())) {
				rankPBBuilder.setGuildTag(playerInfo.getGuildTag()); 
			}
			rankPBBuilder.setIcon(playerInfo.getIcon());
			if (!HawkOSOperator.isEmptyString(playerInfo.getPfIcon())) {
				rankPBBuilder.setPfIcon(playerInfo.getPfIcon());
			}
		}
		
		return rankPBBuilder;
	}
	
}
