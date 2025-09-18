package com.hawk.activity.type.impl.hellfire;

import java.util.ArrayList;
import java.util.Collections;
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
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireConditionCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireCycleCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireKVCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireRankCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireTargetCfg;
import com.hawk.activity.type.impl.hellfire.entity.ActivityHellFireEntity;
import com.hawk.activity.type.impl.hellfire.entity.HellFireInfoEntity;
import com.hawk.activity.type.impl.hellfire.rank.HellFireRank;
import com.hawk.activity.type.impl.hellfire.rank.HellFireRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.HellFireInfoMsg;
import com.hawk.game.protocol.Activity.HellFireInfoS;
import com.hawk.game.protocol.Activity.HellFireRankMsg;
import com.hawk.game.protocol.Activity.HellFireRankResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class HellFireActivity extends ActivityBase {
	/**
	 * 记录活动的一些东西
	 */
	private HellFireInfoEntity hellFireInfo;
	/**
	 * 是否经过初始化
	 */
	private boolean isInit;
	/**
	 * 阶段
	 */
	private int stage;

	public HellFireActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HELL_FIRE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new HellFireActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityHellFireEntity> hellFireEntityList = HawkDBManager.getInstance()
				.query("from ActivityHellFireEntity where playerId=? and termId=? and invalid=0", playerId, termId);
		if (hellFireEntityList != null && !hellFireEntityList.isEmpty()) {
			return hellFireEntityList.get(0);
		}

		return null;
	}

	@Override
	public boolean isActivityClose(String playerId) {
		return !this.isPlayerOpen(playerId);
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityHellFireEntity entity = new ActivityHellFireEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		entity.setCycleStartTime(0);
		entity.setTargetIdsMap(new HashMap<>());

		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		try {
			if (!isShow(playerId)) {
				return;
			}

			HellFireInfoMsg.Builder infoMsg = HellFireInfoMsg.newBuilder();
			Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
			if (!opHellFireEntity.isPresent()) {
				return;
			}
			ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();

			ActivityHellFireKVCfg hellFireKVCfg = ActivityHellFireKVCfg.getInstance();
			long endTime = this.hellFireInfo.getCycleStartTime() * 1000L
					+ ((hellFireKVCfg.getEffectiveTime() + hellFireKVCfg.getAccountTime()) * 1000);

			infoMsg.setCycleId(this.hellFireInfo.getCycleId());
			infoMsg.setEndTime(endTime);
			infoMsg.addAllTargetCfgIdStatus(this.buildKeyValue(hellFireEntity.getTargetIdsMap()));
			infoMsg.setScore(hellFireEntity.getScore());

			HellFireInfoS.Builder sbuilder = HellFireInfoS.newBuilder();
			sbuilder.setInfoMsg(infoMsg);

			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.HELL_FIRE_INFO_S_VALUE, sbuilder);
			this.getDataGeter().sendProtocol(playerId, hawkProtocol);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	private List<KeyValuePairInt> buildKeyValue(Map<Integer, Integer> map) {
		List<KeyValuePairInt> builderList = new ArrayList<>();
		KeyValuePairInt.Builder builder = null;
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			builder = KeyValuePairInt.newBuilder();
			builder.setKey(entry.getKey());
			builder.setVal(entry.getValue());
			builderList.add(builder.build());
		}

		return builderList;
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
		if (this.isShow(playerId)) {
			loginTryFixData(playerId);
		}
	}

	private void loginTryFixData(String playerId) {
		Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();
		// 周期对不上，就重置数据.
		if (this.hellFireInfo.getCycleStartTime() != hellFireEntity.getCycleStartTime()) {
			this.resetPlayerData(playerId);
		}
	}

	private boolean isPlayerOpen(String playerId) {
		//做特殊处理
		if (HawkOSOperator.isEmptyString(playerId)) {
			return true;
		} else {
			int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
			int unlockLevel = ActivityHellFireKVCfg.getInstance().getUnlockCondition();

			return cityLvl >= unlockLevel;
		}		
	}

	@Override
	public void onTick() {
		//等应用程序启动起来再事件.
		
		// 起服的时候处理
		if (!isInit) {
			init();
			return;
		}

		checkStage();
	}

	private void checkStage() {
		ActivityHellFireKVCfg hellFireCfg = ActivityHellFireKVCfg.getInstance();
		long now = HawkTime.getMillisecond();
		long cycleStartTime = this.hellFireInfo.getCycleStartTime();
		cycleStartTime = cycleStartTime * 1000;
		if (now <= cycleStartTime + hellFireCfg.getEffectiveTime() * 1000) {
			if (this.stage != HellFireConst.STAGE_EFFECT) {
				onEffectStart();
			}
		} else {
			// 进入下一次循环伴随一次开始动作			
			this.doAsyncOver(false);			
			this.toNextCycle();
			this.onEffectStart();						
		}

	}

	private void doAsyncOver(boolean isFromEnd) {
		//两个活动靠的很紧的时候会有问题
		if (this.stage == HellFireConst.STAGE_ACCOUNT) {
			return;
		}
		// 切换状态,这样在开第二期的时候就会再走一次start.
		this.stage = HellFireConst.STAGE_ACCOUNT;
		HellFireInfoEntity hellFireInfoEntity = this.hellFireInfo;
		int termId = this.getActivityTermId();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				HellFireActivity.this.over(hellFireInfoEntity, isFromEnd, termId);
				return null;
			}
		});

	}

	private void onEffectStart() {
		logger.info("hell fire onEffectStart");
		this.stage = HellFireConst.STAGE_EFFECT;
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		int unlockLevel = ActivityHellFireKVCfg.getInstance().getUnlockCondition();
		for (String playerId : playerIds) {
			int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
			if (cityLvl >= unlockLevel) {
				this.callBack(playerId, MsgId.HELL_FIRE_ACTIVITY_EFFECT_START, () -> {
					onEffectStart(playerId);
				});
			}
		}
	}

	private void onEffectStart(String playerId) {
		this.syncActivityStateInfo(playerId);
		this.resetPlayerData(playerId);
		this.syncActivityDataInfo(playerId);
	}

	private void resetPlayerData(String playerId) {
		Optional<ActivityHellFireEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		ActivityHellFireEntity entity = opEntity.get();
		PlayerData4Activity playerData4Activity = this.getDataGeter().getPlayerData4Activity(playerId);
		entity.setScore(0);
		entity.setInitBuildingBattlePoint(playerData4Activity.getBuildingBattlePoint());
		entity.setInitTechBattlePoint(playerData4Activity.getTechBattlePoint());
		entity.setOtherSumScore(0);
		entity.setCycleStartTime(this.hellFireInfo.getCycleStartTime());
		entity.setTargetIdsMap(new HashMap<>());

		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		List<Integer> playerTargetIdList = this.getPlayerTargetIdList(cityLevel);

		playerTargetIdList.stream().forEach(targetId -> entity.getTargetIdsMap().put(targetId, 0));
		logger.info("hellFireActivityResetPlayerData playerId:{} targetIdList:{}", playerId, playerTargetIdList.toString());
	}

	private List<Integer> getPlayerTargetIdList(int cityLevel) {
		List<Integer> targetIdList = this.hellFireInfo.getTargetCfgIdList();
		ActivityHellFireTargetCfg targetCfg = null;
		List<Integer> playerTargetIdList = new ArrayList<>();
		for (Integer targetId : targetIdList) {
			targetCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityHellFireTargetCfg.class, targetId);
			if (targetCfg.getLvMin() <= cityLevel && cityLevel <= targetCfg.getLvMax()) {
				playerTargetIdList.add(targetId);
			}
		}

		return playerTargetIdList;
	}

	private void init() {
		HellFireInfoEntity entity = this.getHellInfoEntity();
		if (entity == null) {
			this.toNextCycle();
		} else {
			this.hellFireInfo = entity;
			int currentStartTime = this.calcCurrentCycleStartTime();
			// 当前的周期和数据库存储的周期不一样，这个时候需要处理上个周期的数据.
			if (entity.getCycleStartTime() != currentStartTime) {
				//跨期不做奖励处理
				this.toNextCycle();
			}
		}

		this.isInit = true;
	}

	private int calcCurrentCycleStartTime() {
		int termId = getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long now = HawkTime.getMillisecond();
		int cycleTimes = (int) ((now - startTime - 1) / (ActivityHellFireKVCfg.getInstance().getEffectiveTime() * 1000
				+ ActivityHellFireKVCfg.getInstance().getAccountTime() * 1000) + 1);
		int cycleStartTime = (int) (startTime / 1000)
				+ (cycleTimes - 1) * (ActivityHellFireKVCfg.getInstance().getEffectiveTime()
						+ ActivityHellFireKVCfg.getInstance().getAccountTime());

		return cycleStartTime;
	}

	private void toNextCycle() {
		HellFireInfoEntity entity = this.hellFireInfo;
		Integer nextCycleId = null;

		if (entity == null) {
			entity = new HellFireInfoEntity();
			this.hellFireInfo = entity;
		} else {
			// clone 一份新的数据，防止异步结算的时候使用该数据出错.
			this.hellFireInfo = this.hellFireInfo.clone();
			entity = this.hellFireInfo;
			nextCycleId = this.getNextCycleId(entity);
		}

		if (nextCycleId == null) {
			List<Integer> idList = this.calcCycle();
			entity.setCycleIdList(idList);
			nextCycleId = idList.get(0);
		}

		entity.setCycleId(nextCycleId);
		int cycleTimes = this.calcCurrentCycleStartTime();
		entity.setCycleStartTime(cycleTimes);

		ActivityHellFireCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireCycleCfg.class, nextCycleId);
		List<ActivityHellFireTargetCfg> targetCfgList = generateCurrentCycleTargetList(cycleCfg);
		List<Integer> targetIdList = new ArrayList<>(targetCfgList.size());
		targetCfgList.stream().forEach(targetCfg -> targetIdList.add(targetCfg.getId()));
		entity.setTargetCfgIdList(targetIdList);

		this.saveHellFireInfoEntity(entity);
		
		logger.info("cycidId :{} ", nextCycleId);
	}

	private Integer getNextCycleId(HellFireInfoEntity hellFireInfoEntity) {
		Integer nextId = null;
		int curCycleId = hellFireInfoEntity.getCycleId();
		List<Integer> cycleIdList = hellFireInfoEntity.getCycleIdList();
		int index = cycleIdList.indexOf(curCycleId);
		if (index != cycleIdList.size() - 1) {
			nextId = cycleIdList.get(index + 1);
		}

		return nextId;
	}

	private List<Integer> calcCycle() {
		ConfigIterator<ActivityHellFireCycleCfg> cycleConfigIter = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireCycleCfg.class);
		List<ActivityHellFireCycleCfg> cycleList = new ArrayList<>();
		while (cycleConfigIter.hasNext()) {
			cycleList.add(cycleConfigIter.next());
		}

		Collections.shuffle(cycleList);
		List<Integer> cycleIdList = new ArrayList<>();
		cycleList.stream().forEach(cfg -> cycleIdList.add(cfg.getCycleId()));

		return cycleIdList;
	}

	private void saveHellFireInfoEntity(HellFireInfoEntity entity) {
		String str = JSON.toJSONString(entity);
		ActivityLocalRedis.getInstance().set(ActivityRedisKey.HELL_FIRE, str);
	}

	private HellFireInfoEntity getHellInfoEntity() {
		String str = ActivityLocalRedis.getInstance().get(ActivityRedisKey.HELL_FIRE);
		if (str != null) {
			return JSON.parseObject(str, HellFireInfoEntity.class);
		}

		return null;
	}

	private List<ActivityHellFireTargetCfg> generateCurrentCycleTargetList(ActivityHellFireCycleCfg cycleCfg) {
		List<ActivityHellFireTargetCfg> targetList = new ArrayList<>();
		Table<Integer, Integer, List<ActivityHellFireTargetCfg>> table = this.getDataGeter()
				.getHellFireTargetCfgTable();
		int[][] difficultly = ActivityHellFireKVCfg.getInstance().getDifficultRndWeightArray();

		List<Integer> difficultList = new ArrayList<>();
		List<Integer> weightList = new ArrayList<>();
		for (int i = 0; i < difficultly.length; i++) {
			difficultList.add(difficultly[i][0]);
			weightList.add(difficultly[i][1]);
		}

		Integer difficult = HawkRand.randomWeightObject(difficultList, weightList);
		for (int targetId : cycleCfg.getTargetIdArray()) {
			targetList.addAll(table.get(targetId, difficult));
		}

		return targetList;
	}

	/**
	 * 当前阶段结束 由调用者决定是在哪个线程执行. 该方法所需的所有的参数，最好不要和活动数据挂钩，除了依赖基础数据其它数据最好都是通过参数传入.
	 */
	public void over(HellFireInfoEntity hellFireInfo, boolean isFromEnd, int termId) {
		ActivityHellFireKVCfg config = ActivityHellFireKVCfg.getInstance();
		if (config.getRankType() == HellFireConst.RANK_TYPE_ACTIVITY && isFromEnd) {
			handlerRankReward(hellFireInfo, termId);
		} else if(config.getRankType() == HellFireConst.RANK_TYPE_STAGE) {
			handlerRankReward(hellFireInfo, termId);
		}
		

	}

	private void handlerRankReward(HellFireInfoEntity hellFireInfo, int termid) {
		logger.info("HanleHellFireRankRreward startTime:{},cycleId:{}, targetIdList:{}, cycleIdList:{}", hellFireInfo.getCycleStartTime(), hellFireInfo.getCycleId(),
				hellFireInfo.getTargetCfgIdList(), hellFireInfo.getCycleIdList());
		ActivityHellFireCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireCycleCfg.class, hellFireInfo.getCycleId());
		int rankId = cycleCfg.getRankId();
		// 排名依次		
		List<ActivityHellFireRankCfg> rankCfgList = this.getDataGeter().getHellFireRankMap().get(rankId);
		ActivityHellFireKVCfg config = ActivityHellFireKVCfg.getInstance();
		String keySuffix = "";
		if (config.getRankType() == HellFireConst.RANK_TYPE_STAGE) {
			keySuffix = hellFireInfo.getCycleStartTime()+"";
		} else if (config.getRankType() == HellFireConst.RANK_TYPE_ACTIVITY) {
			keySuffix = termid+"";
		}
		List<HellFireRank> rankList = getRankListAndExpire(keySuffix);
		for (HellFireRank hellFireRank : rankList) {
			ActivityHellFireRankCfg rankCfg = this.getRankCfg(rankCfgList, hellFireRank.getRank());
			if (rankCfg == null) {
				logger.info("hellfireRankRewardNotFind playerId:{}, rankId: {}, rank: {}", hellFireRank.getId(), rankId,
						hellFireRank.getRank());
			} else {
				logger.info("hellfireRankReward playerId:{}, rankId: {}, rank: {}", hellFireRank.getId(),
						rankCfg.getId(), hellFireRank.getRank());
				this.getDataGeter().sendMail(hellFireRank.getId(), MailId.HELL_FIRE_RANK_AWARD,
						new Object[] { this.getActivityCfg().getActivityName() },
						new Object[] { this.getActivityCfg().getActivityName() }, new Object[] {
								this.getActivityCfg().getActivityName(), cycleCfg.getName(), hellFireRank.getRank() },
						rankCfg.getRewardList(), false);
				this.getDataGeter().recordActivityRewardClick(hellFireRank.getId(), ActivityBtns.ActivityChildCellBtn,
						getActivityType(), MailId.HELL_FIRE_RANK_AWARD_VALUE);
			}
		}
	}
	
	/**
	 * 取出排序后的，并且设置键过期
	 * @param keySuffix
	 * @return
	 */
	public List<HellFireRank> getRankListAndExpire(String keySuffix) {
		HellFireRankProvider rankProvider = (HellFireRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.HELL_FIRE_RANK, HellFireRank.class);
		int rankSize = ActivityHellFireKVCfg.getInstance().getRankSize();
		String redisKey = rankProvider.getRedisKey(keySuffix);
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, Math.max((rankSize - 1), 0));		
		List<HellFireRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			HellFireRank timeDropRank = new HellFireRank();
			timeDropRank.setId(rank.getElement());
			timeDropRank.setRank(index);
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
			timeDropRank.setScore(score);
			newRankList.add(timeDropRank);
			index++;
		}		
		ActivityLocalRedis.getInstance().getRedisSession().expire(redisKey, 86400 * 3);
		rankProvider.cleanShowList();
		return newRankList;
	}
	
	private ActivityHellFireRankCfg getRankCfg(List<ActivityHellFireRankCfg> rankRewardCfgList, int rank) {
		for (ActivityHellFireRankCfg rankCfg : rankRewardCfgList) {
			if (rank >= rankCfg.getRankUpper() && rank <= rankCfg.getRankLower()) {
				return rankCfg;
			}
		}

		return null;
	}

	@Subscribe
	public void onTrainCompleteEvent(TrainSoldierCompleteEvent event) {
		String playerId = event.getPlayerId();
		if (!canTriggerEvent(playerId, HellFireConst.SCORE_TYPE_DRAIN)) {
			return;
		}

		Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(event.getPlayerId());
		ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();
		int scoreCof = this.getScoreCfg(HellFireConst.SCORE_TYPE_DRAIN, event.getLevel());
		int addScore = scoreCof * event.getNum();
		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + addScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + addScore);
		logger.info("hellFireActivityTrain playerId:{}, addScore:{}, newSocre:{}", event.getPlayerId(), addScore, hellFireEntity.getScore());
		this.onScoreChange(hellFireEntity, addScore);
	}

	@Subscribe
	public void onHitNewMonster(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		int triggerEventType = 0;
		if ((event.getMosterType() == MonsterType.TYPE_1_VALUE || event.getMosterType() == MonsterType.TYPE_2_VALUE)
				&& event.isKill()) {
			triggerEventType = HellFireConst.SCORE_TYPE_HIT_OLD_MONSTER;
		} else if (event.getMosterType() == MonsterType.TYPE_7_VALUE) {
			triggerEventType = HellFireConst.SCORE_TYPE_HIT_MONSTER;
		} else {
			return;
		}

		if (!canTriggerEvent(playerId, triggerEventType)) {
			return;
		}

		Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(event.getPlayerId());
		ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();
		int hitMonsterTimes = event.getAtkTimes();
		int monsterLevel = event.getMonsterLevel();
		int newMonsterScore = this.getScoreCfg(triggerEventType, monsterLevel) * hitMonsterTimes;
		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + newMonsterScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + newMonsterScore);
		logger.info("hellFireActivityHitMonster playerId:{}, addScore:{}, newSocre:{}", event.getPlayerId(), newMonsterScore, hellFireEntity.getScore());
		this.onScoreChange(hellFireEntity, newMonsterScore);
	}

	private int getScoreCfg(int type, int param) {
		ActivityHellFireConditionCfg conditionCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireConditionCfg.class, type);
		if (conditionCfg == null) {
			return 0;
		}
		Integer score = conditionCfg.getScoreCfgMap().get(param);
		return score == null ? 0 : score;
	}

	@Subscribe
	public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityHellFireEntity entity = opEntity.get();
		if (event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			return;
		}

		if (entity.getCycleStartTime() != 0) {
			return;
		}

		if (this.isShow(event.getPlayerId())) {
			// 活动是一直都开着的，所以无需判断.阶段直接走一次开启即可
			this.getActivityEntity().setNewlyTime(HawkTime.getNextAM0Date());
			this.onEffectStart(playerId);
		}

		logger.info("activityHellFire player unlock playerId:{}", playerId);
	}

	@Subscribe
	public void onResourceCollectEvent(ResourceCollectEvent collectEvent) {
		String playerId = collectEvent.getPlayerId();
		if (!canTriggerEvent(playerId, HellFireConst.SCORE_TYPE_RESOURCE)) {
			return;
		}

		Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(collectEvent.getPlayerId());
		ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();
		int oldScore = hellFireEntity.getScore();
		int newScore = hellFireEntity.getScore();
		int addScore = 0;
		for (Entry<Integer, Double> entry : collectEvent.getCollectMap().entrySet()) {
			int collectScore = this.getScoreCfg(HellFireConst.SCORE_TYPE_RESOURCE, entry.getKey());
			addScore += Math.ceil((entry.getValue() * collectScore));
			newScore += addScore;
		}
		logger.info("hellFireActivity playerId:{},oldScore:{},newScore:{},otherSunScore:{},addScore:{}", collectEvent.getPlayerId(), oldScore, newScore, hellFireEntity.getOtherSumScore(), addScore);
		if (oldScore != newScore) {
			hellFireEntity.setOtherSumScore(addScore + hellFireEntity.getOtherSumScore());
			setScore(hellFireEntity, newScore);
			this.onScoreChange(hellFireEntity, addScore);
		}
	}

	/**
	 * 单次增加的值不可能从最大值到int的最大值 这里只是一个保险
	 * 
	 * @param hellFireEntity
	 * @param newScore
	 */
	private void setScore(ActivityHellFireEntity hellFireEntity, int newScore) {
		newScore = newScore > HellFireConst.MAX_SCORE ? HellFireConst.MAX_SCORE : newScore;
		hellFireEntity.setScore(newScore);
	}

	@Subscribe
	public void onBattlecChangeEvent(BattlePointChangeEvent event) {
		if (!this.isShow(event.getPlayerId())) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		ActivityHellFireEntity hellFireEntity = opHellFireEntity.get();
		if (hellFireEntity.getCycleStartTime() != this.hellFireInfo.getCycleStartTime()) {
			loginTryFixData(event.getPlayerId());
			int initTechBattle = event.getPowerData().getTechBattlePoint() - event.getChangeData().getTechBattleChange();
			int initBuildBattle = event.getPowerData().getBuildBattlePoint() - event.getChangeData().getBuildBattleChange();
			if (initTechBattle != 0) {
				hellFireEntity.setInitBuildingBattlePoint(initBuildBattle);
			}
			if (initBuildBattle != 0) {
				hellFireEntity.setInitTechBattlePoint(initTechBattle);
			}
		}				
		int oldScore = hellFireEntity.getScore();
		int techBattlePoint = 0;
		int buildingBattlePoint = 0;

		if (canTriggerEvent(playerId, HellFireConst.SCORE_TYPE_TECH)) {
			int techScoreCof = this.getScoreCfg(HellFireConst.SCORE_TYPE_TECH, 1);
			techBattlePoint = (event.getPowerData().getTechBattlePoint() - hellFireEntity.getInitTechBattlePoint())
					* techScoreCof;
		}

		if (canTriggerEvent(playerId, HellFireConst.SCORE_TYPE_BUILDING)) {
			int buildScoreCof = this.getScoreCfg(HellFireConst.SCORE_TYPE_BUILDING, 1);
			buildingBattlePoint = (event.getPowerData().getBuildBattlePoint()
					- hellFireEntity.getInitBuildingBattlePoint()) * buildScoreCof;
		}

		int newScore = techBattlePoint + buildingBattlePoint + hellFireEntity.getOtherSumScore();
		logger.info("playerId:{},techBattlePoint:{},buildingBattlePoint:{},otherSunScore:{}, newScore:{}", event.getPlayerId(),
				techBattlePoint, buildingBattlePoint, hellFireEntity.getOtherSumScore(), newScore);
		if (oldScore != newScore) {
			setScore(hellFireEntity, newScore);
			this.onScoreChange(hellFireEntity, newScore - oldScore);
		}
	}

	private boolean canTriggerEvent(String playerId, int conditionType) {
		return isShow(playerId) && canTriggerEvent(conditionType);
	}

	private boolean canTriggerEvent(int conditionType) {
		boolean open = isEffectStage();
		if (!open) {
			return false;
		}

		int cycleId = this.hellFireInfo.getCycleId();
		ActivityHellFireCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireCycleCfg.class, cycleId);

		int[] conditionTypes = cycleCfg.getConditionTypeArray();
		for (int tmpConditionType : conditionTypes) {
			if (tmpConditionType == conditionType) {
				return true;
			}
		}

		return false;
	}

	private boolean isEffectStage() {
		return this.stage == HellFireConst.STAGE_EFFECT;
	}

	private void onScoreChange(ActivityHellFireEntity hellFireEntity, int scoreChange) {
		boolean finish = false;
		for (Entry<Integer, Integer> entry : hellFireEntity.getTargetIdsMap().entrySet()) {
			// 大于0说明已经完成.
			if (entry.getValue().intValue() > 0) {
				continue;
			}

			ActivityHellFireTargetCfg targetCfg = HawkConfigManager.getInstance()
					.getConfigByKey(ActivityHellFireTargetCfg.class, entry.getKey());
			if (targetCfg.getScore() <= hellFireEntity.getScore()) {
				entry.setValue(HellFireConst.STATUS_FINISH);
				hellFireEntity.notifyUpdate();
				finish = true;
			} else {
				finish = finish | false;
			}
		}

		if (finish) {			
			this.syncActivityDataInfo(hellFireEntity.getPlayerId());
		}
		
		this.getDataGeter().logHellFire(hellFireEntity.getPlayerId(), GameConst.HELL_FIRE_TYPE.HELL_FIRE, hellFireEntity.getScore());
		
		ActivityHellFireKVCfg config = ActivityHellFireKVCfg.getInstance();
		if (config.getRankType() == HellFireConst.RANK_TYPE_NONE) {
			return;
		}
		ActivityRankProvider<HellFireRank> rankProvider = ActivityRankContext
				.getRankProvider(ActivityRankType.HELL_FIRE_RANK, HellFireRank.class);				
		HellFireRank hellFireRank = new HellFireRank(hellFireEntity.getPlayerId(), scoreChange);			
		rankProvider.insertIntoRank(hellFireRank);		
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

		Optional<ActivityHellFireEntity> optionalEntity = this.getPlayerDataEntity(playerId);
		if (!optionalEntity.isPresent()) {
			logger.error("hellfire can not find playerData playerId:{}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		ActivityHellFireEntity entity = optionalEntity.get();
		Integer status = entity.getTargetIdsMap().get(targetId);
		if (status == HellFireConst.STATUS_RECEIVED) {
			return Status.Error.HELL_FIRE_ALEARDY_RECEIVED_VALUE;
		} else if (status == HellFireConst.STATUS_NOT_FINISH) {
			return Status.Error.HELL_FIRE_NOT_FINISH_VALUE;
		}

		HawkConfigManager configManager = HawkConfigManager.getInstance();
		ActivityHellFireTargetCfg targetCfg = configManager.getConfigByKey(ActivityHellFireTargetCfg.class, targetId);
		entity.getTargetIdsMap().put(targetId, HellFireConst.STATUS_RECEIVED);
		entity.notifyUpdate();
		this.getDataGeter().takeReward(playerId, targetCfg.getRewardList(), Action.HELL_FIRE_ONE, true);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	
	@Override
	public void onEnd() {
		//必须先初始化,在起服跨阶段的时候需要通过这里去初始化.
		if (!isInit) {
			init();
		} else {
			doAsyncOver(true);
		}		
	}
	
	public void pushRankInfo(String playerId) {
		ActivityHellFireKVCfg config = ActivityHellFireKVCfg.getInstance();
		if (config.getRankType() == HellFireConst.RANK_TYPE_NONE) {
			return;
		}
		
		ActivityRankProvider<HellFireRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.HELL_FIRE_RANK, HellFireRank.class);
		List<HellFireRank> rankList = rankProvider.getRankList();
		HellFireRankResp.Builder sbuilder = HellFireRankResp.newBuilder(); 
		for (HellFireRank rank : rankList) {
			sbuilder.addRankList(buildHellFireRank(rank));
		}
		HellFireRank myRank = rankProvider.getRank(playerId);
		sbuilder.setMyRank(myRank.getRank());
		sbuilder.setMyScore(myRank.getScore());
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.HELL_FIRE_RANK_RESP_VALUE, sbuilder);
		this.getDataGeter().sendProtocol(playerId, protocol);
	}
	
	public HellFireRankMsg buildHellFireRank(HellFireRank rank) {
		ActivityDataProxy dataGeter = this.getDataGeter(); 
		HellFireRankMsg.Builder hellFireRank = HellFireRankMsg.newBuilder();		
		String playerName = dataGeter.getPlayerName(rank.getId());
		String guildName = dataGeter.getGuildTagByPlayerId(rank.getId());
		if (HawkOSOperator.isEmptyString(guildName) == false) {
			hellFireRank.setGuildName(guildName);
		}
		hellFireRank.setPlayerName(playerName);
		hellFireRank.setPlayerId(rank.getId());
		hellFireRank.addAllPersonalProtectSwitch(dataGeter.getPersonalProtectVals(rank.getId()));
		hellFireRank.setRank(rank.getRank());
		hellFireRank.setScore(rank.getScore());
		
		return hellFireRank.build();
	}
	/**
	 * 获取开始时间.
	 * @return
	 */
	public String getKeySuffix() {
		ActivityHellFireKVCfg config = ActivityHellFireKVCfg.getInstance();
		if (config.getRankType() == HellFireConst.RANK_TYPE_STAGE) {
			return this.hellFireInfo.getCycleStartTime()+""; 
		} else if (config.getRankType() == HellFireConst.RANK_TYPE_ACTIVITY) {
			return this.getActivityTermId()+"";
		}  else {
			return "";
		}
	}

	public boolean isInit() {
		return isInit;
	}

	public void setInit(boolean isInit) {
		this.isInit = isInit;
	}
	
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return true;
		}
		HellFireInfoEntity infoEntity = this.hellFireInfo.clone();
		int termId = this.getActivityTermId();
		this.over(infoEntity, true, termId);
		
		return true;
	}
	
	/**
	 * 移除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return;
		}
		ActivityRankProvider<HellFireRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.HELL_FIRE_RANK, HellFireRank.class);
		rankProvider.remMember(playerId);
		rankProvider.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}