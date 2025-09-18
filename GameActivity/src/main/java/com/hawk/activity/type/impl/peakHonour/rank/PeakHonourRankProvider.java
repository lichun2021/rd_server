package com.hawk.activity.type.impl.peakHonour.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.peakHonour.PeakHonourActivity;
import com.hawk.activity.type.impl.peakHonour.cfg.PeakHonourKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

public class PeakHonourRankProvider extends AbstractActivityRankProvider<PeakHonourRank> {
	
	/**
	 * 缓存
	 */
	private List<PeakHonourRank> showList = new ArrayList<>();
	
	/**
	 * 玩家信息
	 */
	private Map<String, PeakHonourPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.PEAK_HONOUR;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.PEAK_HONOUR_VALUE);
		if (opActivity.isPresent()) {
			return opActivity.get().isOpening("");
		} else {
			return false;
		}
	}

	@Override
	public void loadRank() {
		this.doRankSort();
		
	}

	@Override
	public void doRankSort() {
		Optional<PeakHonourActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.PEAK_HONOUR.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		int rankSize = getRankSize();
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Set<Tuple> rankSet = redisSession.zRevrangeWithScores(getRedisKey(), 0, Math.max((rankSize - 1), 0), 0);		
		List<String> playerIds = new ArrayList<>();
		List<PeakHonourRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			PeakHonourRank pkRank = new PeakHonourRank();
			pkRank.setRank(index);
			pkRank.setId(rank.getElement());
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
			pkRank.setScore(score);
			newRankList.add(pkRank);
			playerIds.add(rank.getElement());
			index++;
		}
		showList = newRankList;	
		
		// 玩家信息
		Map<String, PeakHonourPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
		String[] playerIdArray = playerIds.toArray(new String[0]);
		if (playerIdArray != null && playerIdArray.length > 0) {
			List<String> playerInfoList = redisSession.hmGet(getPlayerInfoKey(), playerIdArray);
			for (String playerInfoStr : playerInfoList) {
				PeakHonourPlayerInfo playerInfo = SerializeHelper.getValue(PeakHonourPlayerInfo.class, playerInfoStr, SerializeHelper.COLON_ITEMS);
				playerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
			}
		}
		this.playerInfoMap = playerInfoMap;
	}

	@Override
	public void clean() {
		showList  = new ArrayList<>(this.getRankSize());
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.del(getRedisKey());
	}

	@Override
	public void addScore(String id, int score) {		
		throw  new UnsupportedOperationException("PeakHonourRank can not addScore");   
	}

	@Override
	public void remMember(String id) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.zRem(getRedisKey(), 0, id);
	}

	@Override
	public boolean insertRank(PeakHonourRank rankInfo) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		redisSession.zAdd(getRedisKey(), rankScore, rankInfo.getId());
		return true;
	}

	@Override
	public List<PeakHonourRank> getRankList() {
		return showList;
	}

	@Override
	public PeakHonourRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = RankScoreHelper.getRealScore(index.getScore().longValue());
		}
		PeakHonourRank strongestRank = new PeakHonourRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		return strongestRank;
	}
		
	@Override
	public List<PeakHonourRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}
		
	private String getRedisKey(){
		Optional<PeakHonourActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.PEAK_HONOUR.intValue());
		if (opActivity.isPresent()) {
			 return opActivity.get().getRankKey();
		}
		return null;
	}

	private String getPlayerInfoKey(){
		Optional<PeakHonourActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.PEAK_HONOUR.intValue());
		if (opActivity.isPresent()) {
			 return opActivity.get().getPlayerInfoKey();
		}
		return null;
	}
	
	@Override
	protected boolean canInsertIntoRank(PeakHonourRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return PeakHonourKVCfg.getInstance().getRankSize();
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
	
	/**
	 * 更新玩家信息
	 * @param playerId
	 * @param info
	 */
	public void updatePlayerInfo(String playerId, PeakHonourPlayerInfo info) {
		playerInfoMap.put(playerId, info);
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.hSet(getPlayerInfoKey(), playerId, SerializeHelper.toSerializeString(info, SerializeHelper.COLON_ITEMS));
	}
	
	/**
	 * 获取玩家信息
	 * @param playerId
	 * @return
	 */
	public PeakHonourPlayerInfo getPlayerInfo(String playerId, boolean fromRedis) {
		PeakHonourPlayerInfo playerInfo = playerInfoMap.get(playerId);
		if (playerInfo != null && !fromRedis) {
			return playerInfo;
		}
		
		// player信息存到redis
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		String playerInfoString = redisSession.hGet(getPlayerInfoKey(), playerId);
		playerInfo = SerializeHelper.getValue(PeakHonourPlayerInfo.class, playerInfoString, SerializeHelper.COLON_ITEMS);
		playerInfoMap.put(playerId, playerInfo);
		return playerInfo;
	}
}
