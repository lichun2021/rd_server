package com.hawk.activity.type.impl.pointSprint.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkException;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.pointSprint.PointSprintActivity;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintRankMsg;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

public class PointSprintRankProvider extends AbstractActivityRankProvider<PointSprintRank> {

	/**
	 * 缓存
	 */
	private List<PointSprintRank> showList = new ArrayList<>();
	private List<PointSprintRankMsg> showPBList = new ArrayList<>();
	/**
	 * 玩家信息
	 */
	private Map<String, PointSprintPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();

	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.POINT_SPRINT_345;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.POINT_SPRINT_345_VALUE);
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
		Optional<PointSprintActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.POINT_SPRINT_345.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		int rankSize = getRankSize();

		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Set<Tuple> rankSet = redisSession.zRevrangeWithScores(getRedisKey(), 0, Math.max((rankSize - 1), 0), 0);
		List<String> playerIds = new ArrayList<>();
		List<PointSprintRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			PointSprintRank pkRank = new PointSprintRank();
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
		Map<String, PointSprintPlayerInfo> playerInfoMap = new ConcurrentHashMap<>();
		String[] playerIdArray = playerIds.toArray(new String[0]);
		if (playerIdArray != null && playerIdArray.length > 0) {
			List<String> playerInfoList = redisSession.hmGet(getPlayerInfoKey(), playerIdArray);
			for (String playerInfoStr : playerInfoList) {
				try {
					PointSprintPlayerInfo playerInfo = SerializeHelper.getValue(PointSprintPlayerInfo.class, playerInfoStr, SerializeHelper.COLON_ITEMS);
					playerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		this.playerInfoMap = playerInfoMap;

		List<PointSprintRankMsg> pbList = new ArrayList<>(rankSize);
		for (PointSprintRank rank : showList) {
			try {
				PointSprintRankMsg.Builder rankBuilder = PointSprintRankMsg.newBuilder();
				rankBuilder.setRank(rank.getRank());
				rankBuilder.setScore(rank.getScore());
				rankBuilder.setPlayerId(rank.getId());

				PointSprintPlayerInfo playerInfo = getPlayerInfo(rank.getId());
				rankBuilder.setPlayerName(playerInfo.getPlayerName());
				rankBuilder.setServerId(playerInfo.getServerId());
				rankBuilder.setGuildTag(playerInfo.getGuildTag());
				rankBuilder.setOfficerId(playerInfo.getOfficerId());
				// 前十名才推这些信息
				if (rank.getRank() <= 10) {
					rankBuilder.setIcon(playerInfo.getIcon());
					rankBuilder.setPfIcon(playerInfo.getPfIcon());
				}
				pbList.add(rankBuilder.build());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		showPBList = pbList;
	}

	@Override
	public void clean() {
		showList = new ArrayList<>(this.getRankSize());
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.del(getRedisKey());
	}

	@Override
	public void addScore(String id, int score) {
		throw new UnsupportedOperationException("PointSprintRank can not addScore");
	}

	@Override
	public void remMember(String id) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.zRem(getRedisKey(), 0, id);
	}

	@Override
	public boolean insertRank(PointSprintRank rankInfo) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		redisSession.zAdd(getRedisKey(), rankScore, rankInfo.getId());
		return true;
	}

	@Override
	public List<PointSprintRank> getRankList() {
		return showList;
	}

	@Override
	public PointSprintRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = RankScoreHelper.getRealScore(index.getScore().longValue());
		}
		PointSprintRank strongestRank = new PointSprintRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		return strongestRank;
	}

	@Override
	public List<PointSprintRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;

		return showList.subList(start, end);
	}

	private String getRedisKey() {
		Optional<PointSprintActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.POINT_SPRINT_345.intValue());
		if (opActivity.isPresent()) {
			return opActivity.get().getRankKey();
		}
		return null;
	}

	private String getPlayerInfoKey() {
		Optional<PointSprintActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.POINT_SPRINT_345.intValue());
		if (opActivity.isPresent()) {
			return opActivity.get().getPlayerInfoKey();
		}
		return null;
	}

	@Override
	protected boolean canInsertIntoRank(PointSprintRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return PointSprintKVCfg.getInstance().getRankSize();
	}

	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}

	/**
	 * 更新玩家信息
	 * @param playerId
	 * @param info
	 */
	public void updatePlayerInfo(PointSprintPlayerInfo info) {
		playerInfoMap.put(info.getPlayerId(), info);

		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.hSet(getPlayerInfoKey(), info.getPlayerId(), SerializeHelper.toSerializeString(info, SerializeHelper.COLON_ITEMS));
	}

	/**
	 * 获取玩家信息
	 * @param playerId
	 * @return
	 */
	public PointSprintPlayerInfo getPlayerInfo(String playerId) {
		PointSprintPlayerInfo playerInfo = playerInfoMap.get(playerId);
		if (playerInfo != null) {
			return playerInfo;
		}

		// player信息存到redis
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		String playerInfoString = redisSession.hGet(getPlayerInfoKey(), playerId);
		if (StringUtils.isNotEmpty(playerInfoString)) {
			playerInfo = SerializeHelper.getValue(PointSprintPlayerInfo.class, playerInfoString, SerializeHelper.COLON_ITEMS);
		} else {
			playerInfo = new PointSprintPlayerInfo();
		}
		playerInfoMap.put(playerId, playerInfo);
		return playerInfo;
	}

	public List<PointSprintRank> getShowList() {
		return showList;
	}

	public void setShowList(List<PointSprintRank> showList) {
		this.showList = showList;
	}

	public List<PointSprintRankMsg> getShowPBList() {
		return showPBList;
	}

	public void setShowPBList(List<PointSprintRankMsg> showPBList) {
		this.showPBList = showPBList;
	}

	public Map<String, PointSprintPlayerInfo> getPlayerInfoMap() {
		return playerInfoMap;
	}

	public void setPlayerInfoMap(Map<String, PointSprintPlayerInfo> playerInfoMap) {
		this.playerInfoMap = playerInfoMap;
	}

}
