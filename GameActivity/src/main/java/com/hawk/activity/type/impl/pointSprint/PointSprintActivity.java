package com.hawk.activity.type.impl.pointSprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.result.Result;

import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.tools.ListSplitter;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintExchangeCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintGetCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintKVCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintMissionCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintRankAwardCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintRoundCfg;
import com.hawk.activity.type.impl.pointSprint.entity.PointSprintEntity;
import com.hawk.activity.type.impl.pointSprint.rank.PointSprintPlayerInfo;
import com.hawk.activity.type.impl.pointSprint.rank.PointSprintRank;
import com.hawk.activity.type.impl.pointSprint.rank.PointSprintRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintExchangeInfo;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintGetScore;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintPageInfo;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class PointSprintActivity extends ActivityBase  implements IExchangeTip<PointSprintExchangeCfg>{

	/**
	 * 联盟积分rediskey
	 */
	private static final String GUILD_SCORE_KEY = "activity345_gsk";

	/**
	 * 玩家信息key
	 */
	private static final String PLAYER_INFO = "activity345_pinfo";

	/**
	 * 排行榜
	 */
	private static final String PLAYER_RANK = "activity345_rank";

	public PointSprintActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.POINT_SPRINT_345;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PointSprintActivity activity = new PointSprintActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		pushPageInfo(playerId);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		// 根据条件从数据库中检索
		List<PointSprintEntity> queryList = HawkDBManager.getInstance()
				.query("from PointSprintEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		// 如果有数据的话，返回第一个数据
		if (queryList != null && queryList.size() > 0) {
			PointSprintEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PointSprintEntity entity = new PointSprintEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isActivityClose(playerId)) {
			return;
		}
		pushPageInfo(playerId);
		
		try {
			final String mark = "sdfsdfsdf3456676:" + playerId;
			if (HawkTime.getYyyyMMddIntVal() == 20240523 && ActivityGlobalRedis.getInstance().getRedisSession().getString(mark) == null) {
				ActivityGlobalRedis.getInstance().getRedisSession().setString(mark, mark);
				String IDIP_DAILY_STATIS = "idip_daily_statis";
				String key = IDIP_DAILY_STATIS + ":" + playerId + ":" + HawkTime.getYyyyMMddIntVal() + ":" + "diamondConsume";
				String value = ActivityGlobalRedis.getInstance().getRedisSession().getString(key);
				if (!HawkOSOperator.isEmptyString(value)) {
					int todayCost = Integer.parseInt(value);
					int add = (int) (todayCost - getOwnScore(playerId) / 10);
					if (add > 0) {
						addScore(playerId, 1, add);
						getDataGeter().dungeonRedisLog("sdfsdfsdf3456676", "{}, todayCost = {} buchang: {}",playerId, todayCost, add);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	@Override
	public void onEnd() {
		// 发联盟积分奖励
		// sendGuildAwardMail();
		// 发排名奖励
		sendRankReward();
	}

	/**
	 * 请求界面信息
	 * 
	 * @param playerId
	 */
	public void pushPageInfo(String playerId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();

		PointSprintPageInfo.Builder builder = PointSprintPageInfo.newBuilder();

		// 个人积分
		builder.setOwnPoint(getOwnScore(playerId));

		// 联盟积分
//		String guildId = getDataGeter().getGuildId(playerId);
//		builder.setGuildPoint(getGuildScore(guildId));

		// 个人排名
		PointSprintRankProvider rankProvider = (PointSprintRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.POINT_SPRINT_345, PointSprintRank.class);
		PointSprintRank ownRank = rankProvider.getRank(playerId);
		builder.setOwnScore(ownRank.getScore());
		builder.setOwnRank(ownRank.getRank());
		builder.setOwnServerId(getDataGeter().getServerId());
		builder.setRound(entity.getRound());
		builder.setAwardRound(entity.getAwardRound());
		builder.setServerList(getServerList());

		// 排行榜
		builder.addAllRankList(rankProvider.getShowPBList());

		// 已领取的个人积分奖励
		for (Integer awardId : getPlayerAwarded(playerId)) {
			builder.addOwnPointReward(awardId);
		}

		Map<Integer, Integer> ownScoreMap = getOwnScoreMap(playerId);
		for (Entry<Integer, Integer> ownScore : ownScoreMap.entrySet()) {
			PointSprintGetScore.Builder scoreInfo = PointSprintGetScore.newBuilder();
			scoreInfo.setGetType(ownScore.getKey());
			scoreInfo.setGetScore(ownScore.getValue());
			builder.addGetScore(scoreInfo);
		}

		// 已经兑换数量
		for (int exchangeId : entity.getExchangeNumMap().keySet()) {
			PointSprintExchangeInfo.Builder exchangeInfo = PointSprintExchangeInfo.newBuilder();
			exchangeInfo.setExchangeId(exchangeId);
			exchangeInfo.setNum(entity.getExchangeNumMap().get(exchangeId));
			builder.addExchangeInfos(exchangeInfo);
		}
		// 兑换提醒
		ConfigIterator<PointSprintExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PointSprintExchangeCfg.class);
		while (ite.hasNext()) {
			PointSprintExchangeCfg cfg = ite.next();
			if (!entity.getPlayerPoints().contains(cfg.getId())) {
				builder.addTips(cfg.getId());
			}
		}
		pushToPlayer(playerId, HP.code2.POINT_SPRINT_PAGE_RESP_VALUE, builder);
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
		List<Integer> playerAwarded = getPlayerAwarded(playerId);
		if (playerAwarded.contains(awardId)) {
			return;
		}

		// 联盟奖励不用手动领取
		PointSprintMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintMissionCfg.class, awardId);
		if (cfg == null || cfg.getType() == 2) {
			return;
		}

		// 设置奖励领取
		addPlayerAward(playerId, Arrays.asList(awardId));

		// 推送奖励
		ActivityReward reward = new ActivityReward(cfg.getReward(), Action.POINT_SPRINT_PLAYER_SCORE_AWARD);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward, false);

		// 推界面信息
		pushPageInfo(playerId);

		logger.info("PointSprint get own award, playerId:{}, awardId:{}", playerId, awardId);
	}

	public void getRoundAward(String playerId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();

		// 更新player信息
		if (entity.getAwardRound() == entity.getRound()) {
			return; // 已领取
		}

		// 已经领取过该档位的奖励
		List<Integer> playerAwarded = getPlayerAwarded(playerId);

		ConfigIterator<PointSprintMissionCfg> missionCfgIt = HawkConfigManager.getInstance().getConfigIterator(PointSprintMissionCfg.class);

		List<PointSprintMissionCfg> roundcfglist = missionCfgIt.stream()
				.filter(cfg -> cfg.getRound() == entity.getRound())
				.collect(Collectors.toCollection(ArrayList::new));
		int needPoint = 0;
		for (PointSprintMissionCfg mcfg : roundcfglist) {
			needPoint = Math.max(needPoint, mcfg.getPointsGoal());
		}
		if (needPoint > getOwnScore(playerId)) {
			return;
		}

		List<PointSprintMissionCfg> mrewardcfglist = missionCfgIt.stream()
				.filter(cfg -> cfg.getRound() == entity.getRound())
				.filter(cfg -> !playerAwarded.contains(cfg.getId()))
				.collect(Collectors.toCollection(ArrayList::new));

		List<RewardItem.Builder> mrewardlist = mrewardcfglist.stream().flatMap(cfg -> cfg.getReward().stream()).collect(Collectors.toCollection(LinkedList::new));
		PointSprintRoundCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintRoundCfg.class, entity.getRound());
		mrewardlist.addAll(rcfg.getReward());

		// 推送奖励
		ActivityReward reward = new ActivityReward(mrewardlist, Action.POINT_SPRINT_PLAYER_SCORE_AWARD);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward, true);

		// 设置奖励领取
		if (!mrewardcfglist.isEmpty()) {
			addPlayerAward(playerId, mrewardcfglist.stream().map(cfg -> cfg.getId()).collect(Collectors.toList()));
		}
		entity.setAwardRound(entity.getRound());

		PointSprintRoundCfg nextrcfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintRoundCfg.class, entity.getRound() + 1);
		if (nextrcfg != null) {
			entity.setRound(nextrcfg.getRound());
		}

		// 推界面信息
		pushPageInfo(playerId);
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
		PointSprintGetCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintGetCfg.class, getType);
		if (cfg == null) {
			return;
		}
		if (num <= 0) {
			return;
		}

		// 添加的积分
		int addScore = num * cfg.getProportion();
		int beforeScore = getOwnScore(playerId, cfg.getGetType());
		int afterScore = beforeScore + addScore;

		if (cfg.getIsGetLimit() > 0) {
			if (afterScore > cfg.getLimitPoints()) {
				addScore = (int) (cfg.getLimitPoints() - beforeScore);
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
		PointSprintRankProvider rankProvider = (PointSprintRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.POINT_SPRINT_345, PointSprintRank.class);
		PointSprintRank rank = new PointSprintRank();
		rank.setId(playerId);
		rank.setScore(getOwnScore(playerId));
		rankProvider.insertIntoRank(rank);

		// 更新player信息
		PointSprintPlayerInfo playerInfo = rankProvider.getPlayerInfo(playerId);
		playerInfo.setPlayerId(playerId);
		playerInfo.setPlayerName(getDataGeter().getPlayerName(playerId));
		playerInfo.setGuildTag(getDataGeter().getGuildTagByPlayerId(playerId));
		playerInfo.setIcon(getDataGeter().getIcon(playerId));
		playerInfo.setPfIcon(getDataGeter().getPfIcon(playerId));
		playerInfo.setServerId(getDataGeter().getServerId());
		playerInfo.setOfficerId(getDataGeter().getOfficerId(playerId));
		rankProvider.updatePlayerInfo(playerInfo);

		// getDataGeter().logPointSprintScore(playerId, guildId == null ? "" : guildId, getType, addScore, afterScore, getGroupId());

		pushPageInfo(playerId);
	}

	/**
	 * 添加个人积分
	 * @param playerId
	 * @param getType
	 * @param score
	 * @return
	 */
	private void addOwnScore(String playerId, int getType, int addScore) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		entity.getScoreNumMap().merge(getType, addScore, (v1, v2) -> v1 + v2);
		entity.notifyUpdate();
	}

	/**
	 * 获取个人积分
	 * @param playerId
	 * @return
	 */
	private long getOwnScore(String playerId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return 0;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();

		long score = 0;
		for (Entry<Integer, Integer> info : entity.getScoreNumMap().entrySet()) {
			score += info.getValue();
		}
		return score;
	}

	private Map<Integer, Integer> getOwnScoreMap(String playerId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return new HashMap<>();
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		return entity.getScoreNumMap();
	}

	/**
	 * 获取个人积分
	 * @param playerId
	 * @return
	 */
	private int getOwnScore(String playerId, int getType) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return 0;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		return entity.getScoreNumMap().getOrDefault(getType, 0);
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
	private void addPlayerAward(String playerId, List<Integer> awardId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return;
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		entity.getAwardedList().addAll(awardId);

	}

	/**
	 * 获取已领取的奖励
	 * @return
	 */
	private List<Integer> getPlayerAwarded(String playerId) {
		// 获得玩家活动数据
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		// 如果数据为空直接返回
		if (!opEntity.isPresent()) {
			return new ArrayList<>();
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		return entity.getAwardedList();
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

	// /**
	// * 发送联盟奖励邮件
	// */
	// public void sendGuildAwardMail() {
	// HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
	// Map<String, String> guildScoreMap = redisSession.hGetAll(getGuildScoreKey());
	// if (guildScoreMap == null) {
	// return;
	// }
	//
	// for (Entry<String, String> guildScoreInfo : guildScoreMap.entrySet()) {
	//
	// try {
	// String guildId = guildScoreInfo.getKey();
	// long guildScore = Long.parseLong(guildScoreInfo.getValue());
	//
	// // 奖励
	// List<RewardItem.Builder> reward = new ArrayList<>();
	// ConfigIterator<PointSprintMissionCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PointSprintMissionCfg.class);
	// while (cfgIterator.hasNext()) {
	// PointSprintMissionCfg cfg = cfgIterator.next();
	// if (cfg.getType() == 1) {
	// continue;
	// }
	// if (guildScore >= cfg.getPointsGoal()) {
	// reward.addAll(cfg.getReward());
	// }
	// }
	//
	// if (reward.isEmpty()) {
	// continue;
	// }
	//
	// // 合并奖励
	// reward = RewardHelper.mergeRewardItem(reward);
	//
	// Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
	// for (String playerId : guildMemberIds) {
	// try {
	// // 邮件发奖
	// getDataGeter().sendMail(playerId, MailId.POINT_SPRINT_GUILD_SCORE_AWARD, null, null, new Object[] { this.getActivityCfg().getActivityName(), guildScore },
	// reward, false);
	// logger.error("PointSprint send guild award success, playerId:{}, guildId:{}, guildScore:{}", playerId, guildId, guildScore);
	// } catch (Exception e) {
	// logger.error("PointSprint send guild award exception, playerId:{}, guildId:{}, guildScore:{}", playerId, guildId, guildScore);
	// HawkException.catchException(e);
	// }
	// }
	// } catch (Exception e) {
	// logger.error("PointSprint send guild award exception, guildId:{}, score:{}", guildScoreInfo.getKey(), guildScoreInfo.getValue());
	// HawkException.catchException(e);
	// }
	//
	// }
	// }

	public void sendRankReward() {
		PointSprintRankProvider rankProvider = (PointSprintRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.POINT_SPRINT_345, PointSprintRank.class);
		rankProvider.loadRank();

		// 整体排名偏移
		int rankAdd = 0;

		List<PointSprintRank> rankList = rankProvider.getRankList();
		for (PointSprintRank rank : rankList) {
			try {
				String playerMainServerId = getDataGeter().getPlayerMainServerId(rank.getId());

				int realRank = rank.getRank() + rankAdd;

				// 前三名积分判定是否满足条件，如果不满足，则买下一档的
				int limitPointsRankSize = PointSprintKVCfg.getInstance().limitPointsRankSize();
				if (realRank <= limitPointsRankSize) {
					while (realRank <= limitPointsRankSize) {
						if (rank.getScore() < PointSprintKVCfg.getInstance().getRankAwardLimitPoints(realRank)) {
							rankAdd++;
							realRank++;
						} else {
							break;
						}
					}
					PointSprintRankAwardCfg nextRankCfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintRankAwardCfg.class, realRank);

					if (playerMainServerId != null && playerMainServerId.equals(getDataGeter().getServerId())) {
						sendMailToPlayer(rank.getId(), MailId.POINT_SPRINT_RANK_AWARD, null, null, new Object[] { this.getActivityCfg().getActivityName(), realRank },
								nextRankCfg.getRewardList(), false);
						logger.info("PointSprint send rank award, playerId:{}, rank:{}, score:{}, realRank:{}", rank.getId(), rank.getRank(), rank.getScore(), realRank);
					}
				} else {

					PointSprintRankAwardCfg rewardCfg = null;

					ConfigIterator<PointSprintRankAwardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PointSprintRankAwardCfg.class);
					while (cfgIterator.hasNext()) {
						PointSprintRankAwardCfg cfg = cfgIterator.next();
						if (realRank >= cfg.getRankUpper() && realRank <= cfg.getRankLower()) {
							rewardCfg = cfg;
							break;
						}
					}

					if (rewardCfg == null) {
						continue;
					}

					if (playerMainServerId != null && playerMainServerId.equals(getDataGeter().getServerId())) {
						sendMailToPlayer(rank.getId(), MailId.POINT_SPRINT_RANK_AWARD, null, null, new Object[] { this.getActivityCfg().getActivityName(), realRank },
								rewardCfg.getRewardList(), false);
						logger.info("PointSprint send rank award, playerId:{}, rank:{}, score:{}, realRank:{}", rank.getId(), rank.getRank(), rank.getScore(), realRank);
					}
				}
			} catch (Exception e) {
				logger.error("PointSprint send rank award exception, playerId:{}, rank:{}, score:{}", rank.getId(), rank.getRank(), rank.getScore());
				HawkException.catchException(e);
			}

		}
	}

	@Override
	public boolean isHidden(String playerId) {
		// 不在分组内,则关闭此活动
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

	/**
	* 兑换
	* @param playerId 玩家id
	* @param exchangeId 兑换id
	* @param num 兑换数量
	* @return
	*/
	public Result<Integer> exchange(String playerId, int exchangeId, int num) {
		// 判断活动是否开启，如果没开返回错误码
//		if (!isOpening(playerId)) {
//			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
//		}
		// 获取兑换配置，如果配置为空返回错误码
		PointSprintExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PointSprintExchangeCfg.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		// 获得玩家活动数据，如果活动数据为空直接返回
		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		// 获得玩家活动数据实体
		PointSprintEntity entity = opEntity.get();
		// 当前已经兑换数量
		int buyNum = entity.getExchangeNumMap().getOrDefault(exchangeId, 0);
		// 兑换后的数量
		int newNum = buyNum + num;
		// 判断是否超过可兑换数量最大值，如果超过返回错误码
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		// 兑换消耗
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.POINT_SPRINT_EXCHANGE, true);
		// 如果不够消耗，返回错误码
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		// 设置新的兑奖数量
		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 发奖
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.POINT_SPRINT_EXCHANGE, true);
		// 记录日志
		logger.info("rose_gift_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		pushPageInfo(playerId);
		// 返回兑换状态
		return Result.success(newNum);
	}

//	/**
//	 * 兑换勾选提醒
//	 * @param playerId 玩家id
//	 * @param ids 兑换id
//	 * @param tips 勾选类型 0为去掉 1为增加 2为全选 3为全取消
//	 * @return
//	 */
//	public Result<?> exchangeTips(String playerId, List<Integer> ids, int tips) {
//		// 判断活动是否开启，如果没开返回错误码
//		if (!isOpening(playerId)) {
//			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
//		}
//		// 获得玩家活动数据，如果为空但会错误码
//		Optional<PointSprintEntity> opEntity = getPlayerDataEntity(playerId);
//		if (!opEntity.isPresent()) {
//			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
//		}
//		// 活动玩家活动数据实体
//		PointSprintEntity entity = opEntity.get();
//		switch (tips) {
//		case 0: {
//			for (int id : ids) {
//				if (entity.getPlayerPoints().contains(id)) {
//					continue;
//				}
//				entity.getPlayerPoints().add(id);
//			}
//			entity.notifyUpdate();
//		}
//			break;
//		case 1: {
//			for (int id : ids) {
//				entity.getPlayerPoints().remove(new Integer(id));
//			}
//			entity.notifyUpdate();
//		}
//			break;
//		}
//		pushPageInfo(playerId);
//		return Result.success();
//	}

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
			int amount = PointSprintKVCfg.getInstance().getGroupAmount();
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
		return "pont_spnttt_acty:" + this.getActivityTermId();
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
		PointSprintRankProvider rankProvider = (PointSprintRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.POINT_SPRINT_345, PointSprintRank.class);
		rankProvider.remMember(playerId);
		rankProvider.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
