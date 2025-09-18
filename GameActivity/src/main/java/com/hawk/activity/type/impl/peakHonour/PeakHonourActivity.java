package com.hawk.activity.type.impl.peakHonour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.tools.ListSplitter;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.peakHonour.cfg.PeakHonourKVCfg;
import com.hawk.activity.type.impl.peakHonour.cfg.PeakHonourPointsAwardCfg;
import com.hawk.activity.type.impl.peakHonour.cfg.PeakHonourPointsGetCfg;
import com.hawk.activity.type.impl.peakHonour.cfg.PeakHonourRankAwardCfg;
import com.hawk.activity.type.impl.peakHonour.rank.PeakHonourPlayerInfo;
import com.hawk.activity.type.impl.peakHonour.rank.PeakHonourRank;
import com.hawk.activity.type.impl.peakHonour.rank.PeakHonourRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.ActivityPeakHonour.PeakHonourGetScore;
import com.hawk.game.protocol.ActivityPeakHonour.PeakHonourPageInfo;
import com.hawk.game.protocol.ActivityPeakHonour.PeakHonourRankMsg;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/**
 * 巅峰荣耀
 * 
 * @author Golden
 *
 */
public class PeakHonourActivity extends ActivityBase {

	/**
	 * 个人积分rediskey
	 */
	private static final String PLAYER_SCORE_KEY = "activity275_psk";

	/**
	 * 联盟积分rediskey
	 */
	private static final String GUILD_SCORE_KEY = "activity275_gsk";

	/**
	 * 玩家信息key
	 */
	private static final String PLAYER_INFO = "activity275_pinfo";
	
	/**
	 * 玩家积分奖励领取信息(个人)
	 */
	private static final String PLAYER_AWARD = "activity275_pa";

	/**
	 * 排行榜
	 */
	private static final String PLAYER_RANK = "activity275_rank";
	
	public PeakHonourActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PEAK_HONOUR;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PeakHonourActivity activity = new PeakHonourActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		pushPageInfo(playerId);
	}
	
	@Override
	public void onEnd() {
		// 发联盟积分奖励
		sendGuildAwardMail();
		// 发排名奖励
		sendRankReward();
	}
	
	/**
	 * 请求界面信息
	 * 
	 * @param playerId
	 */
	public void pushPageInfo(String playerId) {
		PeakHonourPageInfo.Builder builder = PeakHonourPageInfo.newBuilder();
		
		// 个人积分
		builder.setOwnPoint(getOwnScore(playerId));
		
		// 联盟积分
		String guildId = getDataGeter().getGuildId(playerId);
		builder.setGuildPoint(getGuildScore(guildId));
		
		// 个人排名
		PeakHonourRankProvider rankProvider = (PeakHonourRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.PEAK_HONOUR, PeakHonourRank.class);
		PeakHonourRank ownRank = rankProvider.getRank(playerId);
		builder.setOwnScore(ownRank.getScore());
		builder.setOwnRank(ownRank.getRank());
		builder.setOwnServerId(getDataGeter().getServerId());
		builder.setServerList(getServerList());
		// 排行榜
		for (PeakHonourRank rank : rankProvider.getRankList()) {
			try {
				PeakHonourRankMsg.Builder rankBuilder = PeakHonourRankMsg.newBuilder();
				rankBuilder.setRank(rank.getRank());
				rankBuilder.setScore(rank.getScore());
				rankBuilder.setPlayerId(rank.getId());
				
				// 是否是本服玩家
				String rankPlayerServer = getDataGeter().getPlayerMainServerId(rank.getId());
				String serverId = getDataGeter().getServerId();
				boolean ownServer = (!HawkOSOperator.isEmptyString(rankPlayerServer) && rankPlayerServer.equals(serverId));
				
				PeakHonourPlayerInfo playerInfo = rankProvider.getPlayerInfo(rank.getId(), false);
				if (ownServer) {
					rankBuilder.setPlayerName(getDataGeter().getPlayerName(rank.getId()));
					rankBuilder.setServerId(getDataGeter().getPlayerMainServerId(rank.getId()));
					rankBuilder.setGuildTag(getDataGeter().getGuildTagByPlayerId(rank.getId()));
					
					// 前十名才推这些信息
					if (rank.getRank() <= 10) {
						rankBuilder.setIcon(getDataGeter().getIcon(rank.getId()));
						String pfIcon = getDataGeter().getPfIcon(rank.getId());
						rankBuilder.setPfIcon(pfIcon == null ? "" : pfIcon);
					}
				} else {
					rankBuilder.setPlayerName(playerInfo.getPlayerName());
					rankBuilder.setServerId(playerInfo.getServerId());
					rankBuilder.setGuildTag(playerInfo.getGuildTag());
					
					// 前十名才推这些信息
					if (rank.getRank() <= 10) {
						rankBuilder.setIcon(playerInfo.getIcon());
						rankBuilder.setPfIcon(playerInfo.getPfIcon());
					}
				}
				builder.addRankList(rankBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 已领取的个人积分奖励
		for (Integer awardId : getPlayerAwarded(playerId)) {
			builder.addOwnPointReward(awardId);
		}
		
		Map<Integer, Integer> ownScoreMap = getOwnScoreMap(playerId);
		for (Entry<Integer, Integer> ownScore : ownScoreMap.entrySet()) {
			PeakHonourGetScore.Builder scoreInfo = PeakHonourGetScore.newBuilder();
			scoreInfo.setGetType(ownScore.getKey());
			scoreInfo.setGetScore(ownScore.getValue());
			builder.addGetScore(scoreInfo);
		}
		
		pushToPlayer(playerId, HP.code.ACTIVITY_PEAK_HONOUR_PAGE_RESP_VALUE, builder);
	}

	/**
	 * 获取个人奖励
	 * @param playerId
	 * @param awardId
	 */
	public void getOwnAward(String playerId, int awardId) {
		if (isHidden(playerId)) {
			return;
		}
		
		// 已经领取过该档位的奖励
		Set<Integer> playerAwarded = getPlayerAwarded(playerId);
		if (playerAwarded.contains(awardId)) {
			return;
		}
		
		// 联盟奖励不用手动领取
		PeakHonourPointsAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PeakHonourPointsAwardCfg.class, awardId);
		if (cfg == null || cfg.getType() == 2) {
			return;
		}
		
		// 设置奖励领取
		addPlayerAward(playerId, awardId);
		
		// 推送奖励
		ActivityReward reward = new ActivityReward(cfg.getReward(), Action.PEAK_HONOUR_PLAYER_SCORE_AWARD);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward, false);
		
		// 推界面信息
		pushPageInfo(playerId);
		
		logger.info("peakHonour get own award, playerId:{}, awardId:{}", playerId, awardId);
	}
	
	/**
	 * 消耗金条
	 * 
	 * @param event
	 */
	@Subscribe
	public void onCostDiamon(ConsumeMoneyEvent event) {
		if (event.getResType() != PlayerAttr.DIAMOND_VALUE) {
			return;
		}
		addScore(event.getPlayerId(), 1, (int) event.getNum());
	}

	/**
	 * 消耗金币
	 * 
	 * @param event
	 */
	@Subscribe
	public void onCostGold(ConsumeMoneyEvent event) {
		if (event.getResType() != PlayerAttr.GOLD_VALUE) {
			return;
		}
		addScore(event.getPlayerId(), 2, (int) event.getNum());
	}

	/**
	 * 消耗体力
	 * 
	 * @param event
	 */
	@Subscribe
	public void onConsumeVit(VitCostEvent event) {
		addScore(event.getPlayerId(), 3, event.getCost());
	}

	/**
	 * 消耗加速道具
	 * 
	 * @param event
	 */
	@Subscribe
	public void onConsumeSpeedTool(UseItemSpeedUpEvent event) {
		addScore(event.getPlayerId(), 4, event.getMinute());
	}

	/**
	 * 添加积分
	 * 
	 * @param playerId
	 * @param getType
	 * @param num
	 */
	private void addScore(String playerId, int getType, int num) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		PeakHonourPointsGetCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PeakHonourPointsGetCfg.class, getType);
		if (cfg == null) {
			return;
		}
		if (num <= 0) {
			return;
		}
		
		// 添加的积分
		long addScore = num * (long)cfg.getProportion();
		long beforeScore = getOwnScore(playerId, cfg.getGetType());
		long afterScore = beforeScore + addScore;
		
		if (cfg.getIsGetLimit() > 0) {
			if (afterScore > cfg.getLimitPoints()) {
				addScore = (int)(cfg.getLimitPoints() - beforeScore);
			}
		}

		// 添加个人积分
		addOwnScore(playerId, getType, addScore);

		// 添加联盟积分
		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			addGuildScore(guildId, getType, addScore);
		}
		
		// 更新个人排行榜
		PeakHonourRankProvider rankProvider = (PeakHonourRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.PEAK_HONOUR, PeakHonourRank.class);
		PeakHonourRank rank = new PeakHonourRank();
		rank.setId(playerId);
		rank.setScore(getOwnScore(playerId));
		rankProvider.insertIntoRank(rank);
		
		// 更新player信息
		PeakHonourPlayerInfo playerInfo = new PeakHonourPlayerInfo();
		playerInfo.setPlayerId(playerId);
		playerInfo.setPlayerName(getDataGeter().getPlayerName(playerId));
		playerInfo.setGuildTag(getDataGeter().getGuildTagByPlayerId(playerId));
		playerInfo.setIcon(getDataGeter().getIcon(playerId));
		playerInfo.setPfIcon(getDataGeter().getPfIcon(playerId));
		playerInfo.setServerId(getDataGeter().getServerId());
		rankProvider.updatePlayerInfo(playerId, playerInfo);
		
		getDataGeter().logPeakHonourScore(playerId, guildId == null ? "" : guildId, getType, addScore, afterScore, getGroupId());
		
		pushPageInfo(playerId);
	}
	
	/**
	 * 添加个人积分
	 * @param playerId
	 * @param getType
	 * @param score
	 * @return
	 */
	private void addOwnScore(String playerId, int getType, long addScore) {
		String psk = getOwnScoreKey(playerId);
		ActivityGlobalRedis.getInstance().hIncrBy(psk, String.valueOf(getType), addScore);
	}
	
	/**
	 * 获取个人积分
	 * @param playerId
	 * @return
	 */
	private long getOwnScore(String playerId) {
		long score = 0;
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Map<String, String> ownScoreMap = redisSession.hGetAll(getOwnScoreKey(playerId));
		if (ownScoreMap != null) {
			for (Entry<String, String> info : ownScoreMap.entrySet()) {
				score += Long.parseLong(info.getValue());
			}
		}
		return score;
	}
	
	private Map<Integer, Integer> getOwnScoreMap(String playerId) {
		Map<Integer, Integer> retMap = new HashMap<>();
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Map<String, String> ownScoreMap = redisSession.hGetAll(getOwnScoreKey(playerId));
		if (ownScoreMap == null) {
			return retMap;
		}
		
		for (Entry<String, String> scoreMap : ownScoreMap.entrySet()) {
			retMap.put(Integer.valueOf(scoreMap.getKey()), Integer.valueOf(scoreMap.getValue()));
		}
		return retMap;
	}
	
	/**
	 * 获取个人积分
	 * @param playerId
	 * @return
	 */
	private long getOwnScore(String playerId, int getType) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		String score = redisSession.hGet(getOwnScoreKey(playerId), String.valueOf(getType));
		if (HawkOSOperator.isEmptyString(score)) {
			return 0;
		}
		return Long.parseLong(score);
	}
	
	/**
	 * 添加联盟积分
	 * @param guildId
	 * @param getType
	 * @param addScore
	 */
	private void addGuildScore(String guildId, int getType, long addScore) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		String gsk = getGuildScoreKey();
		ActivityGlobalRedis.getInstance().hIncrBy(gsk, guildId, addScore);
	}
	
	/**
	 * 获取联盟积分
	 * @param guildId
	 * @return
	 */
	private long getGuildScore(String guildId) {
		long score = 0;
		if (HawkOSOperator.isEmptyString(guildId)) {
			return score;
		}
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		String guildScore = redisSession.hGet(getGuildScoreKey(), guildId);
		if (guildScore != null) {
			score += Long.parseLong(guildScore);
		}
		return score;
	}

	/**
	 * 添加玩家领取奖励信息
	 * @param playerId
	 * @param awardId
	 */
	private void addPlayerAward(String playerId, int awardId) {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.hSet(getPlayerAwardKey(playerId), String.valueOf(awardId), "");
	}
	
	/**
	 * 获取已领取的奖励
	 * @return
	 */
	private Set<Integer> getPlayerAwarded(String playerId) {
		Set<Integer> awardIdSet = new HashSet<>();
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Map<String, String> playerAward = redisSession.hGetAll(getPlayerAwardKey(playerId));
		if (playerAward != null) {
			for (String awardId : playerAward.keySet()) {
				awardIdSet.add(Integer.valueOf(awardId));
			}
		}
		return awardIdSet;
	}
	
	/**
	 * 个人积分key
	 * @param playerId
	 * @return
	 */
	private String getOwnScoreKey(String playerId) {
		return PLAYER_SCORE_KEY + ":" + getActivityTermId() + ":" + playerId;
	}
	
	/**
	 * 联盟积分key
	 * @param guildId
	 * @return
	 */
	private String getGuildScoreKey() {
		String serverId = getDataGeter().getServerId();
		return GUILD_SCORE_KEY + ":" + getActivityTermId() + ":" + serverId;
	}
	
	/**
	 * 获取已领取积分奖励的key
	 * @param playerId
	 * @return
	 */
	public String getPlayerAwardKey(String playerId) {
		return PLAYER_AWARD + ":" + getActivityTermId() + ":" + playerId;
	}
	
	/**
	 * 获取玩家信息key
	 * @param playerId
	 * @return
	 */
	public String getPlayerInfoKey() {
		return PLAYER_INFO;
	}
	
	/**
	 * 获取排行榜key
	 * @return
	 */
	public String getRankKey() {
		int groupId = getGroupId();
		return PLAYER_RANK + ":" + getActivityTermId() + ":" + groupId;
	}
	
	/**
	 * 发送联盟奖励邮件
	 */
	public void sendGuildAwardMail() {
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		Map<String, String> guildScoreMap = redisSession.hGetAll(getGuildScoreKey());
		if (guildScoreMap == null) {
			return;
		}
		
		for (Entry<String, String> guildScoreInfo : guildScoreMap.entrySet()) {
			
			try {
				String guildId = guildScoreInfo.getKey();
				long guildScore = Long.parseLong(guildScoreInfo.getValue());
				
				// 奖励
				List<RewardItem.Builder> reward = new ArrayList<>();
				ConfigIterator<PeakHonourPointsAwardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PeakHonourPointsAwardCfg.class);
				while (cfgIterator.hasNext()) {
					PeakHonourPointsAwardCfg cfg = cfgIterator.next();
					if (cfg.getType() == 1) {
						continue;
					}
					if (guildScore >= cfg.getPointsGoal()) {
						reward.addAll(cfg.getReward());
					}
				}
				
				if (reward.isEmpty()) {
					continue;
				}
				
				// 合并奖励
				reward = RewardHelper.mergeRewardItem(reward);
				
				Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
				for (String playerId : guildMemberIds) {
					try {
						// 邮件发奖
						getDataGeter().sendMail(playerId, MailId.PEAK_HONOUR_GUILD_SCORE_AWARD, null, null, new Object[] { this.getActivityCfg().getActivityName(), guildScore }, reward, false);
						logger.error("peakHonour send guild award success, playerId:{}, guildId:{}, guildScore:{}", playerId, guildId, guildScore);
					} catch (Exception e) {
						logger.error("peakHonour send guild award exception, playerId:{}, guildId:{}, guildScore:{}", playerId, guildId, guildScore);
						HawkException.catchException(e);
					}
				}
			} catch (Exception e) {
				logger.error("peakHonour send guild award exception, guildId:{}, score:{}", guildScoreInfo.getKey(), guildScoreInfo.getValue());
				HawkException.catchException(e);
			}
			
		}
	}
	
	public void sendRankReward() {
		PeakHonourRankProvider rankProvider = (PeakHonourRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.PEAK_HONOUR, PeakHonourRank.class);
		rankProvider.loadRank();
		
		// 整体排名偏移
		int rankAdd = 0;
		
		List<PeakHonourRank> rankList = rankProvider.getRankList();
		for (PeakHonourRank rank : rankList) {
			try {
				String playerMainServerId = getDataGeter().getPlayerMainServerId(rank.getId());
				
				int realRank = rank.getRank() + rankAdd;
				
				// 前三名积分判定是否满足条件，如果不满足，则买下一档的
				int limitPointsRankSize = PeakHonourKVCfg.getInstance().limitPointsRankSize();
				if (realRank <= limitPointsRankSize) {
					while (realRank <= limitPointsRankSize) {
						if (rank.getScore() <  PeakHonourKVCfg.getInstance().getRankAwardLimitPoints(realRank)) {
							rankAdd++;
							realRank++;
						} else {
							break;
						}
					}
					PeakHonourRankAwardCfg nextRankCfg = HawkConfigManager.getInstance().getConfigByKey(PeakHonourRankAwardCfg.class, realRank);
					
					if (playerMainServerId != null && playerMainServerId.equals(getDataGeter().getServerId())) {
						sendMailToPlayer(rank.getId(), MailId.PEAK_HONOUR_RANK_AWARD, null, null,  new Object[] { this.getActivityCfg().getActivityName(), realRank }, nextRankCfg.getRewardList(), false);
						logger.info("peakHonour send rank award, playerId:{}, rank:{}, score:{}, realRank:{}", rank.getId(), rank.getRank(), rank.getScore(), realRank);
					}
				} else {
					
					PeakHonourRankAwardCfg rewardCfg = null;
					
					ConfigIterator<PeakHonourRankAwardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PeakHonourRankAwardCfg.class);
					while(cfgIterator.hasNext()) {
						PeakHonourRankAwardCfg cfg = cfgIterator.next();
						if (realRank >= cfg.getRankUpper() && realRank <= cfg.getRankLower()) {
							rewardCfg = cfg;
							break;
						}
					}
					
					if (rewardCfg == null) {
						continue;
					}
					
					if (playerMainServerId != null && playerMainServerId.equals(getDataGeter().getServerId())) {
						sendMailToPlayer(rank.getId(), MailId.PEAK_HONOUR_RANK_AWARD, null, null,  new Object[] { this.getActivityCfg().getActivityName(), realRank }, rewardCfg.getRewardList(), false);
						logger.info("peakHonour send rank award, playerId:{}, rank:{}, score:{}, realRank:{}", rank.getId(), rank.getRank(), rank.getScore(), realRank);
					}
				}
			} catch (Exception e) {
				logger.error("peakHonour send rank award exception, playerId:{}, rank:{}, score:{}", rank.getId(), rank.getRank(), rank.getScore());
				HawkException.catchException(e);
			}
			
		}
	}
	
	@Override
	public boolean isHidden(String playerId) {
		int groupId = getGroupId();

		if (groupId == 0) {
			return true;
		}
		
		return super.isHidden(playerId);
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		// 不在分组内,则关闭此活动
		int groupId = getGroupId();

		return groupId == 0;
	}

	@Override
	public void onOpen() {
		groupId = -1;
		serverList = "";
		getGroupId();
		getServerList();
		super.onOpen();
	}

	int groupId = -1;

	private int getGroupId() {
		if (groupId > -1) {
			return groupId;
		}

		String serverId = getDataGeter().getServerId();
		String lockKey = lockKey();
		String result = ActivityGlobalRedis.getInstance().getRedisSession().hGet(lockKey, serverId);
		if (StringUtils.isEmpty(result)) {
			List<String> serverList = getOpenServerList();
			int amount = PeakHonourKVCfg.getInstance().getGroupAmount();
			List<List<String>> list = ListSplitter.splitList(serverList, amount);
			for (int i = 0; i < list.size(); i++) {
				for (String serv : list.get(i)) {
					ActivityGlobalRedis.getInstance().getRedisSession().hSetNx(lockKey, serv, Integer.valueOf(i + 1).toString());
				}
			}
		}
		groupId = NumberUtils.toInt(ActivityGlobalRedis.getInstance().getRedisSession().hGet(lockKey, serverId));
		return groupId;
	}

	String serverList = "";

	private String getServerList() {
		if (StringUtils.isNotEmpty(serverList)) {
			return serverList;
		}

		String groupId = getGroupId() + "";
		String lockKey = lockKey();
		Map<String, String> result = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(lockKey);
		List<String> parts = new ArrayList<>();
		for (Entry<String, String> ent : result.entrySet()) {
			if (ent.getValue().equals(groupId)) {
				parts.add(ent.getKey());
			}
		}
		serverList = Joiner.on("_").join(parts);
		return serverList;
	}

	private String lockKey() {
		return "peak_hdonoer_acty:" + this.getActivityTermId();
	}

	/**
	 * 符合开启条件的服
	 */
	private List<String> getOpenServerList() {
		List<ServerInfo> serverInfoList = getDataGeter().getServerList();
		Collections.sort(serverInfoList, Comparator.comparing(ServerInfo::getOpenTime));
		int termId = getActivityTermId();
		Collections.shuffle(serverInfoList, new Random(termId)); //乱序
		
		List<String> result = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		long serverDelay = getTimeControl().getServerDelay();
		for (ServerInfo sinfo : serverInfoList) {
			long timeLimit = HawkTime.parseTime(sinfo.getOpenTime()) + serverDelay;
			if (timeLimit < now) {
				result.add(sinfo.getId());
			}
		}
		return result;
	}
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		PeakHonourRankProvider rankProvider = (PeakHonourRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.PEAK_HONOUR, PeakHonourRank.class);
		rankProvider.remMember(playerId);
		rankProvider.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
