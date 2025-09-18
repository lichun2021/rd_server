package com.hawk.activity.type.impl.bannerkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.PvpBannerBattleEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillKVCfg;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillRankCfg;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillTargetCfg;
import com.hawk.activity.type.impl.bannerkill.entity.ActivityBannerKillEntity;
import com.hawk.activity.type.impl.bannerkill.rank.BannerKillRank;
import com.hawk.activity.type.impl.bannerkill.rank.BannerKillRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.BannerKillActInfoPB;
import com.hawk.game.protocol.Activity.BannerKillRankMsg;
import com.hawk.game.protocol.Activity.BannerKillRankResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;
import redis.clients.jedis.Tuple;

/**
 * 战神降临活动
 * 
 * @author lating
 *
 */
public class BannerKillActivity extends ActivityBase {

	public BannerKillActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BANNER_KILL_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new BannerKillActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityBannerKillEntity> bannerEntityList = HawkDBManager.getInstance()
				.query("from ActivityBannerKillEntity where playerId=? and termId=? and invalid=0", playerId, termId);
		if (bannerEntityList != null && !bannerEntityList.isEmpty()) {
			return bannerEntityList.get(0);
		}

		return null;
	}

	@Override
	public boolean isActivityClose(String playerId) {
		return !this.isPlayerOpen(playerId);
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityBannerKillEntity entity = new ActivityBannerKillEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		Map<Integer, Integer> targetIdMap = new HashMap<Integer, Integer>();
		for (int targetId : ActivityBannerKillKVCfg.getInstance().getTargetIdArray()) {
			targetIdMap.put(targetId, 0);
		}
		
		entity.setTargetIdsMap(targetIdMap);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isShow(playerId)) {
			return;
		}

		Optional<ActivityBannerKillEntity> opBannerEntity = getPlayerDataEntity(playerId);
		if (!opBannerEntity.isPresent()) {
			return;
		}
		
		ActivityBannerKillEntity bannerEntity = opBannerEntity.get();
		// 要同步哪些信息
		BannerKillActInfoPB.Builder builder = BannerKillActInfoPB.newBuilder();
		builder.setScore(bannerEntity.getKillEnemyScore());
		for (Entry<Integer, Integer> entry : bannerEntity.getTargetIdsMap().entrySet()) {
			KeyValuePairInt.Builder keyValPair = KeyValuePairInt.newBuilder();
			keyValPair.setKey(entry.getKey());
			keyValPair.setVal(entry.getValue());
			builder.addTargetIdStatus(keyValPair);
		}
		
		this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.code.BANNER_KILL_INFO_S_VALUE, builder));
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// do nothing;
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// do nothing;
	}

	@Override
	public void onPlayerLogin(String playerId) {
//		if (isShow(playerId)) {}
	}

	@Override
	public void onTick() {

	}
	
	@Override
	public void onEnd() {
		int termId = this.getActivityTermId();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				handlerRankReward(termId);
				return null;
			}
		});
	}
	
	@Override
	public void onHidden() {
		BannerKillRankProvider rankProvider = (BannerKillRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.BANNER_KILL_RANK, BannerKillRank.class);
		String redisKey = rankProvider.getRedisKey(getKeySuffix());
		// 这里是否要设过期时间, 不让历史数据永久占用空间
		ActivityLocalRedis.getInstance().getRedisSession().expire(redisKey, 86400 * 3);
		rankProvider.cleanShowList();
	}
	
	/**
	 * 判断此活动对该玩家是否开启
	 * 
	 * @param playerId
	 * @return
	 */
	private boolean isPlayerOpen(String playerId) {
		// 排行特殊处理
		if (HawkOSOperator.isEmptyString(playerId)) {
			return true;
		}
		
		int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
		int unlockLevel = ActivityBannerKillKVCfg.getInstance().getUnlockCondition();
		return cityLvl >= unlockLevel;
	}

	/**
	 * 获取redis key
	 * 
	 * @return
	 */
	public String getKeySuffix() {
		return String.valueOf(this.getActivityTermId());
	}
	
	/**
	 * 活动结算时发送排名奖励
	 * 
	 * @param termId
	 */
	private void handlerRankReward(int termId) {
		String keySuffix = String.valueOf(termId);
		List<BannerKillRank> rankList = getRankListAndExpire(keySuffix);
		for (BannerKillRank bannerRank : rankList) {
			ActivityBannerKillRankCfg rankCfg = this.getRankCfg(bannerRank.getRank());
			if (rankCfg == null) {
				logger.info("bannerKillRank reward not found, playerId: {}, rank: {}", bannerRank.getId(), bannerRank.getRank());
			} else {
				logger.info("bannerKillRank reward, playerId: {}, rank: {}", bannerRank.getId(), bannerRank.getRank());
				// 邮件
				this.getDataGeter().sendMail(bannerRank.getId(), MailId.BANNER_KILL_RANK_AWARD,
						new Object[] { this.getActivityCfg().getActivityName() },
						new Object[] { this.getActivityCfg().getActivityName() }, 
						new Object[] { bannerRank.getRank() },
						rankCfg.getRewardList(), false);
				// 活动打点
				this.getDataGeter().recordActivityRewardClick(bannerRank.getId(), ActivityBtns.ActivityChildCellBtn,
						getActivityType(), MailId.BANNER_KILL_RANK_AWARD_VALUE);  
			}
		}
	}
	
	/**
	 * 取出排序后的，并且设置键过期
	 * 
	 * @param keySuffix
	 * @return
	 */
	public List<BannerKillRank> getRankListAndExpire(String keySuffix) {
		BannerKillRankProvider rankProvider = (BannerKillRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.BANNER_KILL_RANK, BannerKillRank.class);
		
		int rankSize = ActivityBannerKillKVCfg.getInstance().getRankSize();
		String redisKey = rankProvider.getRedisKey(keySuffix);
		
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, Math.max((rankSize - 1), 0));		
		List<BannerKillRank> newRankList = new ArrayList<BannerKillRank>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			BannerKillRank killRank = new BannerKillRank();
			killRank.setId(rank.getElement());
			killRank.setRank(index);
			long score = (long) rank.getScore();
			killRank.setScore(score);
			newRankList.add(killRank);
			index++;
		}
		
		return newRankList;
	}
	
	/**
	 * 获取排名奖励配置
	 * 
	 * @param rankRewardCfgList
	 * @param rank
	 * @return
	 */
	private ActivityBannerKillRankCfg getRankCfg(int rank) {
		ConfigIterator<ActivityBannerKillRankCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ActivityBannerKillRankCfg.class);
		while (iterator.hasNext()) {
			ActivityBannerKillRankCfg rankCfg = iterator.next();
			if (rank >= rankCfg.getRankUpper() && rank <= rankCfg.getRankLower()) {
				return rankCfg;
			}
		}

		return null;
	}
	
	/**
	 * 监听战胜降临击杀事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onBannerKillEvent(PvpBannerBattleEvent event) {
		String playerId = event.getPlayerId();
		if (!isShow(playerId)) {
			return;
		}

		Optional<ActivityBannerKillEntity> bannerEntityOp = getPlayerDataEntity(playerId);
		if (!bannerEntityOp.isPresent()) {
			return;
		}
		
		ActivityBannerKillEntity bannerEntity = bannerEntityOp.get();
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		Map<Integer, Integer> armyHurtMap = event.getArmyHurtMap();
		
		Map<Integer, Integer> levelScoreKillMap = ActivityBannerKillKVCfg.getInstance().getKillScoreCfgMap();
		Map<Integer, Integer> levelScoreHurtMap = ActivityBannerKillKVCfg.getInstance().getHurtScoreCfgMap();
		
		long killScore = getScore(armyKillMap, levelScoreKillMap);
		long hurtScore = getScore(armyHurtMap, levelScoreHurtMap);
		if (killScore <= 0 && hurtScore <= 0) {
			return;
		}
		
		long totalScore = killScore + hurtScore;
		int ratio = ActivityBannerKillKVCfg.getInstance().getAtkScoreRatio();
		if (!event.isAtk()) {
			ratio = ActivityBannerKillKVCfg.getInstance().getDefScoreRatio();
		}
		
		totalScore = (long) (totalScore * (ratio * 1D / ActivityConst.MYRIABIT_BASE));
		bannerEntity.setKillEnemyScore(bannerEntity.getKillEnemyScore() + totalScore);
		
		// 推送积分变化
		onScoreChange(bannerEntity, totalScore);
	}
	
	/**
	 * 获取杀敌积分
	 * 
	 * @param map
	 * @param levelScoreMap
	 * @return
	 */
	private long getScore(Map<Integer, Integer> map, Map<Integer, Integer> levelScoreMap){
		long score = 0;
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			Integer armyId = entry.getKey();
			Integer count = entry.getValue();
			int soldierLevel = dataGeter.getSoldierLevel(armyId);
			
			if (levelScoreMap.containsKey(soldierLevel)) {
				score += 1L * levelScoreMap.get(soldierLevel) * count;
			}
		}
		
		return score;
	}

	/**
	 * 积分变化
	 * 
	 * @param bannerEntity
	 * @param changeScore
	 */
	private void onScoreChange(ActivityBannerKillEntity bannerEntity, long changeScore) {
		boolean finish = false;
		int playerCityLevel = this.getDataGeter().getConstructionFactoryLevel(bannerEntity.getPlayerId());
		ConfigIterator<ActivityBannerKillTargetCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityBannerKillTargetCfg.class);
		for (Entry<Integer, Integer> entry : bannerEntity.getTargetIdsMap().entrySet()) {
			// 大于0说明已经完成.
			if (entry.getValue().intValue() > 0) {
				continue;
			}

			Optional<ActivityBannerKillTargetCfg> configOp = configIterator.stream().filter(e -> e.getTargetId() == entry.getKey() 
					&& e.getLvMin() <= playerCityLevel && e.getLvMax() >= playerCityLevel).findAny();
			if (configOp.isPresent() && configOp.get().getScore() <= bannerEntity.getKillEnemyScore()) {
				entry.setValue(BannerKillConst.STATUS_FINISH);
				bannerEntity.notifyUpdate();
				finish = true;
			} else {
				finish = finish | false;
			}
		}

		if (changeScore > 0) {
			this.syncActivityDataInfo(bannerEntity.getPlayerId());
		}
		
		ActivityRankProvider<BannerKillRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.BANNER_KILL_RANK, BannerKillRank.class);
		BannerKillRank bannerRank = rankProvider.getRank(bannerEntity.getPlayerId());
		HawkLog.logPrintln("bannerKill activity change score, playerId: {}, oldScore: {}, changeScore: {}", bannerEntity.getPlayerId(), bannerRank.getScore(), changeScore);
		bannerRank.setScore(bannerEntity.getKillEnemyScore());
		rankProvider.insertIntoRank(bannerRank);
	}
	
	/**
	 * 领奖
	 * 
	 * @param playerId
	 * @param targetId
	 * @return
	 */
	public int receive(String playerId, Integer targetId) {
		if (!isAllowOprate(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<ActivityBannerKillEntity> optionalEntity = this.getPlayerDataEntity(playerId);
		if (!optionalEntity.isPresent()) {
			logger.error("bannerKill activity can not find playerData, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		ActivityBannerKillEntity entity = optionalEntity.get();
		Integer status = entity.getTargetIdsMap().get(targetId);
		if (status == BannerKillConst.STATUS_RECEIVED) {
			return Status.Error.BANNER_KILL_TARGET_RECEIVED_VALUE;
		} else if (status == BannerKillConst.STATUS_NOT_FINISH) {
			return Status.Error.BANNER_KILL_TARGET_NOT_FINISH_VALUE;
		}

		int playerCityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		ConfigIterator<ActivityBannerKillTargetCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityBannerKillTargetCfg.class);
		Optional<ActivityBannerKillTargetCfg> configOp = configIterator.stream().filter(e -> e.getTargetId() == targetId 
				&& e.getLvMin() <= playerCityLevel && e.getLvMax() >= playerCityLevel).findAny();
		
		if (!configOp.isPresent()) {
			logger.error("bannerKill activity can not find ActivityBannerKillTargetCfg, playerId: {}, cityLevel: {}, targetId: {}", playerId, playerCityLevel, targetId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityBannerKillTargetCfg targetCfg = configOp.get();
		entity.getTargetIdsMap().put(targetId, BannerKillConst.STATUS_RECEIVED);
		entity.notifyUpdate();
		this.getDataGeter().takeReward(playerId, targetCfg.getRewardList(), Action.BANNER_KILL_TARGET_REWARD, true);
		this.syncActivityDataInfo(playerId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 同步排行榜信息
	 * 
	 * @param playerId
	 */
	public void pushRankInfo(String playerId) {
		ActivityRankProvider<BannerKillRank> rankProvider = ActivityRankContext
				.getRankProvider(ActivityRankType.BANNER_KILL_RANK, BannerKillRank.class);
		List<BannerKillRank> rankList = rankProvider.getRankList();
		// 同步排名信息
		BannerKillRankResp.Builder sbuilder = BannerKillRankResp.newBuilder(); 
		for (BannerKillRank rank : rankList) {
			sbuilder.addRankInfo(buildBannerKillRank(rank));
		}
		
		BannerKillRank myRank = rankProvider.getRank(playerId);
		sbuilder.setMyRank(myRank.getRank());
		sbuilder.setMyScore(myRank.getScore());
		this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.code.BANNER_KILL_RANK_RESP, sbuilder));
	}
	
	/**
	 * 构建排行榜信息PB
	 * 
	 * @param rank
	 * @return
	 */
	public BannerKillRankMsg buildBannerKillRank(BannerKillRank rank) {
		BannerKillRankMsg.Builder bannerKillRank = BannerKillRankMsg.newBuilder();		
		String playerName = this.getDataGeter().getPlayerName(rank.getId());
		String guildName = this.getDataGeter().getGuildTagByPlayerId(rank.getId());
		if (HawkOSOperator.isEmptyString(guildName) == false) {
			bannerKillRank.setGuildName(guildName);
		}
		bannerKillRank.setPlayerName(playerName);
		bannerKillRank.setRank(rank.getRank());
		bannerKillRank.setScore(rank.getScore());
		
		return bannerKillRank.build();
	}
	
}