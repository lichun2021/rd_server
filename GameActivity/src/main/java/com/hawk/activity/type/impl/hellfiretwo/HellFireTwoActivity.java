package com.hawk.activity.type.impl.hellfiretwo;

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
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoConditionCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoCycleCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoKVCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoRankCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoTargetCfg;
import com.hawk.activity.type.impl.hellfiretwo.entity.ActivityHellFireTwoEntity;
import com.hawk.activity.type.impl.hellfiretwo.entity.HellFireTwoInfoEntity;
import com.hawk.activity.type.impl.hellfiretwo.rank.HellFireTwoRank;
import com.hawk.activity.type.impl.hellfiretwo.rank.HellFireTwoRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.HellFireInfoMsg;
import com.hawk.game.protocol.Activity.HellFireRankMsg;
import com.hawk.game.protocol.Activity.HellFireTwoInfoS;
import com.hawk.game.protocol.Activity.HellFireTwoRankResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class HellFireTwoActivity extends ActivityBase {
	/**
	 * 记录活动的一些东西
	 */
	private HellFireTwoInfoEntity hellFireInfo;
	/**
	 * 是否经过初始化
	 */
	private boolean isInit;
	/**
	 * 阶段
	 */
	private int stage;

	public HellFireTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HELL_FIRE_TWO_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new HellFireTwoActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityHellFireTwoEntity> hellFireEntityList = HawkDBManager.getInstance()
				.query("from ActivityHellFireTwoEntity where playerId=? and termId=? and invalid=0", playerId, termId);
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
		ActivityHellFireTwoEntity entity = new ActivityHellFireTwoEntity();
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
			Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
			if (!opHellFireEntity.isPresent()) {
				return;
			}
			ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();
			ActivityHellFireTwoKVCfg hellFireKVCfg = ActivityHellFireTwoKVCfg.getInstance();
			long endTime = this.hellFireInfo.getStartTime() * 1000l
					+ ((hellFireKVCfg.getEffectiveTime() + hellFireKVCfg.getAccountTime()) * 1000);

			infoMsg.setCycleId(this.hellFireInfo.getCycleId());
			infoMsg.setEndTime(endTime);
			infoMsg.addAllTargetCfgIdStatus(this.buildKeyValue(hellFireEntity.getTargetIdsMap()));
			infoMsg.setScore(hellFireEntity.getScore());

			HellFireTwoInfoS.Builder sbuilder = HellFireTwoInfoS.newBuilder();
			sbuilder.setInfoMsg(infoMsg);

			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.HELL_FIRE_TWO_INFO_S_VALUE, sbuilder);
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
		if (isShow(playerId)) {
			loginTryFixData(playerId);
		}
	}

	private void loginTryFixData(String playerId) {
		Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();
		// 周期对不上，就重置数据.
		if (this.hellFireInfo.getStartTime() != hellFireEntity.getCycleStartTime()) {
			this.resetPlayerData(playerId);
		}
	}

	private boolean isPlayerOpen(String playerId) {
		//排行特殊处理
		if (HawkOSOperator.isEmptyString(playerId)) {
			return true;
		}
		int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
		int unlockLevel = ActivityHellFireTwoKVCfg.getInstance().getUnlockCondition();

		return cityLvl >= unlockLevel;
	}

	@Override
	public void onTick() {
		// 起服的时候处理
		if (!isInit) {
			init();
			return;
		}

		checkStage();
	}

	private void checkStage() {
		ActivityHellFireTwoKVCfg hellFireCfg = ActivityHellFireTwoKVCfg.getInstance();
		long now = HawkTime.getMillisecond();
		long cycleStartTime = this.hellFireInfo.getStartTime();
		cycleStartTime = cycleStartTime * 1000;
		if (now <= cycleStartTime + hellFireCfg.getEffectiveTime() * 1000) {
			if (this.stage != HellFireTwoConst.STAGE_EFFECT) {
				onEffectStart();
			}
		} else {
			// 进入下一次循环伴随一次开始动作
			if (HawkTaskManager.getInstance().getExtraExecutor().isRunning()) {
				logger.info("hell fire two execute asyncOver ");
				doAsyncOver(false);			
				this.toNextCycle();
				this.onEffectStart();
			}	
					 	
		}

	}
	
	private void doAsyncOver(boolean isFromEnd) {
		//防止checkStage在end的时候调用.
		if (this.stage == HellFireTwoConst.STAGE_ACCOUNT) {
			return;
		}
		this.stage = HellFireTwoConst.STAGE_ACCOUNT; 
		HellFireTwoInfoEntity hellFireInfoEntity = this.hellFireInfo;
		int termId = this.getActivityTermId();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

			@Override
			public Object run() {
				HellFireTwoActivity.this.over(hellFireInfoEntity, isFromEnd, termId);
				return null;
			}
		});

	}
	

	private void onEffectStart() {
		logger.info("hell fire two onEffectStart");
		this.stage = HellFireTwoConst.STAGE_EFFECT;
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		int unlockLevel = ActivityHellFireTwoKVCfg.getInstance().getUnlockCondition();
		for (String playerId : playerIds) {
			int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
			if (cityLvl >= unlockLevel) {
				this.callBack(playerId, MsgId.HELL_FIRE_TWO_ACTIVITY_EFFECT_START, () -> {
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
		Optional<ActivityHellFireTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityHellFireTwoEntity entity = opEntity.get();
		PlayerData4Activity playerData4Activity = this.getDataGeter().getPlayerData4Activity(playerId);
		entity.setCycleStartTime(this.hellFireInfo.getStartTime());
		entity.setScore(0);
		entity.setInitBuildingBattlePoint(playerData4Activity.getBuildingBattlePoint());
		entity.setInitTechBattlePoint(playerData4Activity.getTechBattlePoint() 
				+ playerData4Activity.getPlantScienceBattlePoint());
		entity.setOtherSumScore(0);
		entity.setTargetIdsMap(new HashMap<>());

		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		List<Integer> playerTargetIdList = this.getPlayerTargetIdList(cityLevel);

		playerTargetIdList.stream().forEach(targetId -> entity.getTargetIdsMap().put(targetId, 0));
	}

	private List<Integer> getPlayerTargetIdList(int cityLevel) {
		List<Integer> targetIdList = this.hellFireInfo.getTargetCfgIdList();
		ActivityHellFireTwoTargetCfg targetCfg = null;
		List<Integer> playerTargetIdList = new ArrayList<>();
		for (Integer targetId : targetIdList) {
			targetCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityHellFireTwoTargetCfg.class, targetId);
			if (targetCfg.getLvMin() <= cityLevel && cityLevel <= targetCfg.getLvMax()) {
				playerTargetIdList.add(targetId);
			}
		}

		return playerTargetIdList;
	}

	private void init() {
		HellFireTwoInfoEntity entity = this.getHellInfoEntity();
		if (entity == null) {
			this.toNextCycle();
		} else {
			this.hellFireInfo = entity;
		}
		this.isInit = true;
	}

	/**
	 * 接口名字和方法名不变, 把次数换成时间
	 * 
	 * @return
	 */
	private int calcCurrentCycleStartTime() {
		int termId = getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long now = HawkTime.getMillisecond();
		int cycleTimes = (int) ((now - startTime - 1)
				/ (ActivityHellFireTwoKVCfg.getInstance().getEffectiveTime() * 1000
						+ ActivityHellFireTwoKVCfg.getInstance().getAccountTime() * 1000)
				+ 1);
		int cycleStartTime = (int) ((startTime / 1000)
				+ (cycleTimes - 1) * (ActivityHellFireTwoKVCfg.getInstance().getEffectiveTime()
						+ ActivityHellFireTwoKVCfg.getInstance().getAccountTime()));

		return cycleStartTime;
	}

	private void toNextCycle() {
		HellFireTwoInfoEntity entity = this.hellFireInfo;
		Integer nextCycleId = null;

		if (entity == null) {
			entity = new HellFireTwoInfoEntity();
			this.hellFireInfo = entity;
		} else {			// clone 一份新的数据，防止异步结算的时候使用该数据出错.
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
		int cycleStartTime = this.calcCurrentCycleStartTime();
		entity.setStartTime(cycleStartTime);

		ActivityHellFireTwoCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireTwoCycleCfg.class, nextCycleId);
		List<ActivityHellFireTwoTargetCfg> targetCfgList = generateCurrentCycleTargetList(cycleCfg);
		List<Integer> targetIdList = new ArrayList<>(targetCfgList.size());
		targetCfgList.stream().forEach(targetCfg -> targetIdList.add(targetCfg.getId()));
		entity.setTargetCfgIdList(targetIdList);

		this.saveHellFireInfoEntity(entity);
		
		logger.info("cycidId :{} ", nextCycleId);
	}

	private Integer getNextCycleId(HellFireTwoInfoEntity hellFireInfoEntity) {
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
		ConfigIterator<ActivityHellFireTwoCycleCfg> cycleConfigIter = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireTwoCycleCfg.class);
		List<ActivityHellFireTwoCycleCfg> cycleList = new ArrayList<>();
		while (cycleConfigIter.hasNext()) {
			cycleList.add(cycleConfigIter.next());
		}

		Collections.shuffle(cycleList);
		List<Integer> cycleIdList = new ArrayList<>();
		cycleList.stream().forEach(cfg -> cycleIdList.add(cfg.getCycleId()));

		return cycleIdList;
	}

	private void saveHellFireInfoEntity(HellFireTwoInfoEntity entity) {
		String str = JSON.toJSONString(entity);
		String key = ActivityRedisKey.HELL_FIRE_2;
		ActivityLocalRedis.getInstance().set(key, str);
	}

	private HellFireTwoInfoEntity getHellInfoEntity() {
		String key = ActivityRedisKey.HELL_FIRE_2;
		String str = ActivityLocalRedis.getInstance().get(key);
		if (str != null) {
			return JSON.parseObject(str, HellFireTwoInfoEntity.class);
		}

		return null;
	}

	private List<ActivityHellFireTwoTargetCfg> generateCurrentCycleTargetList(ActivityHellFireTwoCycleCfg cycleCfg) {
		List<ActivityHellFireTwoTargetCfg> targetList = new ArrayList<>();
		Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> table = this.getDataGeter()
				.getHellTwoTargetCfgTable();
		int[][] difficultly = ActivityHellFireTwoKVCfg.getInstance().getDifficultRndWeightArray();

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
	public void over(HellFireTwoInfoEntity hellFireInfo, boolean isFromEnd, int termId) {
		ActivityHellFireTwoKVCfg config = ActivityHellFireTwoKVCfg.getInstance();
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_ACTIVITY && isFromEnd) {
			handlerRankReward(hellFireInfo, termId);
		} else if(config.getRankType() == HellFireTwoConst.RANK_TYPE_STAGE) {
			handlerRankReward(hellFireInfo, termId);
		}
	}
	
	private void handlerRankReward(HellFireTwoInfoEntity hellFireInfo, int termId) {
		logger.info("HanleHellFireTwoRankRreward cycleId:{}, targetIdList:{}, cycleIdList:{}", hellFireInfo.getCycleId(),
				hellFireInfo.getTargetCfgIdList(), hellFireInfo.getCycleIdList());
		ActivityHellFireTwoCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireTwoCycleCfg.class, hellFireInfo.getCycleId());
		int rankId = cycleCfg.getRankId();
		ActivityHellFireTwoKVCfg config = ActivityHellFireTwoKVCfg.getInstance();
		String keySuffix = "";
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_STAGE) {
			keySuffix = hellFireInfo.getStartTime()+"";
		} else if (config.getRankType() == HellFireTwoConst.RANK_TYPE_ACTIVITY) {
			keySuffix = termId+"";
		}
		List<HellFireTwoRank> rankList = getRankListAndExpire(keySuffix);
		List<ActivityHellFireTwoRankCfg> rankCfgList = this.getDataGeter().getHellFireTwoRankMap().get(rankId);
		for (HellFireTwoRank hellFireRank : rankList) {
			ActivityHellFireTwoRankCfg rankCfg = this.getRankCfg(rankCfgList, hellFireRank.getRank());
			if (rankCfg == null) {
				logger.info("hellfireTwoRankRewardNotFind playerId:{}, rankId: {}, rank: {}", hellFireRank.getId(), rankId,
						hellFireRank.getRank());
			} else {
				logger.info("hellfireTwoRankReward playerId:{}, rankId: {}, rank: {}", hellFireRank.getId(),
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
	public List<HellFireTwoRank> getRankListAndExpire(String keySuffix) {
		HellFireTwoRankProvider rankProvider = (HellFireTwoRankProvider) ActivityRankContext
				.getRankProvider(ActivityRankType.HELL_FIRE_TWO_RANK, HellFireTwoRank.class);
		int rankSize = ActivityHellFireTwoKVCfg.getInstance().getRankSize();
		String redisKey = rankProvider.getRedisKey(keySuffix);
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(redisKey, 0, Math.max((rankSize - 1), 0));		
		List<HellFireTwoRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			HellFireTwoRank timeDropRank = new HellFireTwoRank();
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
	private ActivityHellFireTwoRankCfg getRankCfg(List<ActivityHellFireTwoRankCfg> rankRewardCfgList, int rank) {
		for (ActivityHellFireTwoRankCfg rankCfg : rankRewardCfgList) {
			if (rank >= rankCfg.getRankUpper() && rank <= rankCfg.getRankLower()) {
				return rankCfg;
			}
		}

		return null;
	}

	@Subscribe
	public void onTrainCompleteEvent(TrainSoldierCompleteEvent event) {
		String playerId = event.getPlayerId();
		if (!canTriggerEvent(playerId, HellFireTwoConst.SCORE_TYPE_DRAIN)) {
			return;
		}

		Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(event.getPlayerId());
		ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();
		int scoreCof = this.getScoreCfg(HellFireTwoConst.SCORE_TYPE_DRAIN, event.getLevel());
		int addScore = scoreCof * event.getNum();

		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + addScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + addScore);
		logger.info("hellFireTwoTrain playerId:{}, addScore:{}, newScore:{}", event.getPlayerId(), addScore, hellFireEntity.getScore());
		this.onScoreChange(hellFireEntity, addScore);

	}

	@Subscribe
	public void onHitNewMonster(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		int triggerEventType = 0;
		if ((event.getMosterType() == MonsterType.TYPE_1_VALUE || event.getMosterType() == MonsterType.TYPE_2_VALUE)
				&& event.isKill()) {
			triggerEventType = HellFireTwoConst.SCORE_TYPE_HIT_OLD_MONSTER;
		} else if (event.getMosterType() == MonsterType.TYPE_7_VALUE) {
			triggerEventType = HellFireTwoConst.SCORE_TYPE_HIT_MONSTER;
		} else {
			return;
		}

		if (!canTriggerEvent(playerId, triggerEventType)) {
			return;
		}

		Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(event.getPlayerId());
		ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();
		int hitMonsterTimes = event.getAtkTimes();
		int monsterLevel = event.getMonsterLevel();
		int newMonsterScore = this.getScoreCfg(triggerEventType, monsterLevel) * hitMonsterTimes;

		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + newMonsterScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + newMonsterScore);
		logger.info("hellFireTwoHitMonster playerId:{}, addScore:{}, newScore:{}", event.getPlayerId(),newMonsterScore, hellFireEntity.getScore());
		this.onScoreChange(hellFireEntity, newMonsterScore);
	}

	private int getScoreCfg(int type, int param) {
		ActivityHellFireTwoConditionCfg conditionCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireTwoConditionCfg.class, type);

		if (conditionCfg == null) {
			return 0;
		}		
		Integer score = conditionCfg.getScoreCfgMap().get(param);
		return score == null ? 0 : score;
	}

	@Subscribe
	public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
		this.isAllowOprate(event.getPlayerId());
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityHellFireTwoEntity entity = opEntity.get();
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
		if (!canTriggerEvent(playerId, HellFireTwoConst.SCORE_TYPE_RESOURCE)) {
			return;
		}

		Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		//和当前的活动周期保持一致
		loginTryFixData(collectEvent.getPlayerId());
		ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();
		int oldScore = hellFireEntity.getScore();
		int newScore = hellFireEntity.getScore();
		int addScore = 0;
		for (Entry<Integer, Double> entry : collectEvent.getCollectMap().entrySet()) {
			int collectScore = this.getScoreCfg(HellFireTwoConst.SCORE_TYPE_RESOURCE, entry.getKey());
			addScore += Math.ceil((entry.getValue() * collectScore));
			newScore += addScore;
		}
		logger.info("hellFireTwoActivity playerId:{},oldScore:{},newScore:{},otherSunScore:{},addScore:{}", collectEvent.getPlayerId(), oldScore, newScore, hellFireEntity.getOtherSumScore(), addScore);
		if (oldScore != newScore) {
			hellFireEntity.setOtherSumScore(addScore + hellFireEntity.getOtherSumScore());
			setScore(hellFireEntity, newScore);
			this.onScoreChange(hellFireEntity, addScore);
		}
	}

	@Subscribe
	public void onBattlecChangeEvent(BattlePointChangeEvent event) {
		if (!this.isShow(event.getPlayerId())) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireTwoEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireTwoEntity hellFireEntity = opHellFireEntity.get();		
		if (hellFireEntity.getCycleStartTime() != this.getHellInfoEntity().getStartTime()) {
			//和当前的活动周期保持一致
			loginTryFixData(event.getPlayerId());
			int initTechBattle = (event.getPowerData().getTechBattlePoint() - event.getChangeData().getTechBattleChange())+
					event.getPowerData().getPlantScienceBattlePoint() - event.getChangeData().getPlantScienceBattlePoint();
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
		
		if (canTriggerEvent(playerId, HellFireTwoConst.SCORE_TYPE_TECH)) {
			int techScoreCof = this.getScoreCfg(HellFireTwoConst.SCORE_TYPE_TECH, 1);
			techBattlePoint = (event.getPowerData().getTechBattlePoint() + event.getPowerData().getPlantScienceBattlePoint()
					- hellFireEntity.getInitTechBattlePoint())* techScoreCof;
		}

		if (canTriggerEvent(playerId, HellFireTwoConst.SCORE_TYPE_BUILDING)) {
			int buildScoreCof = this.getScoreCfg(HellFireTwoConst.SCORE_TYPE_BUILDING, 1);
			buildingBattlePoint = (event.getPowerData().getBuildBattlePoint()
					- hellFireEntity.getInitBuildingBattlePoint()) * buildScoreCof;
		}

		int newScore = techBattlePoint + buildingBattlePoint + hellFireEntity.getOtherSumScore();
		logger.info("playerId:{},techBattlePoint:{},buildingBattlePoint:{},otherSunScore:{}", event.getPlayerId(),
				techBattlePoint, buildingBattlePoint, hellFireEntity.getOtherSumScore());
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
		ActivityHellFireTwoCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireTwoCycleCfg.class, cycleId);

		int[] conditionTypes = cycleCfg.getConditionTypeArray();
		for (int tmpConditionType : conditionTypes) {
			if (tmpConditionType == conditionType) {
				return true;
			}
		}

		return false;
	}

	private boolean isEffectStage() {
		return this.stage == HellFireTwoConst.STAGE_EFFECT;
	}

	/**
	 * 单次增加的值不可能从最大值到int的最大值 这里只是一个保险
	 * 
	 * @param hellFireEntity
	 * @param newScore
	 */
	private void setScore(ActivityHellFireTwoEntity hellFireTwoEntity, int newScore) {
		newScore = newScore > HellFireTwoConst.MAX_SCORE ? HellFireTwoConst.MAX_SCORE : newScore;
		hellFireTwoEntity.setScore(newScore);
	}

	private void onScoreChange(ActivityHellFireTwoEntity hellFireEntity, int changeScore) {
		boolean finish = false;
		for (Entry<Integer, Integer> entry : hellFireEntity.getTargetIdsMap().entrySet()) {
			// 大于0说明已经完成.
			if (entry.getValue().intValue() > 0) {
				continue;
			}

			ActivityHellFireTwoTargetCfg targetCfg = HawkConfigManager.getInstance()
					.getConfigByKey(ActivityHellFireTwoTargetCfg.class, entry.getKey());
			if (targetCfg.getScore() <= hellFireEntity.getScore()) {
				entry.setValue(HellFireTwoConst.STATUS_FINISH);
				hellFireEntity.notifyUpdate();
				finish = true;
			} else {
				finish = finish | false;
			}
		}

		if (finish) {
			this.syncActivityDataInfo(hellFireEntity.getPlayerId());
		}
		
		//全军动员
		this.getDataGeter().logHellFire(hellFireEntity.getPlayerId(), GameConst.HELL_FIRE_TYPE.HELL_FIRE_TOW, hellFireEntity.getScore());
		
		ActivityHellFireTwoKVCfg config = ActivityHellFireTwoKVCfg.getInstance();
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_NONE) {
			return;
		}
		ActivityRankProvider<HellFireTwoRank> rankProvider = ActivityRankContext
				.getRankProvider(ActivityRankType.HELL_FIRE_TWO_RANK, HellFireTwoRank.class);
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_STAGE) {
			HellFireTwoRank hellFireRank = new HellFireTwoRank(hellFireEntity.getPlayerId(), hellFireEntity.getScore());
			rankProvider.insertIntoRank(hellFireRank);
		} else if (config.getRankType() == HellFireTwoConst.RANK_TYPE_ACTIVITY){
			HellFireTwoRank hellFireRank = rankProvider.getRank(hellFireEntity.getPlayerId());
			HawkLog.logPrintln("hell fire two activity playerId:{}  oldScore:{}, changeScore:{}", hellFireEntity.getPlayerId(), hellFireRank.getScore(), changeScore);
			hellFireRank.setScore(hellFireEntity.getScore() + changeScore);
			rankProvider.insertIntoRank(hellFireRank);
		}
		
	}
	
	/**
	 * 领奖
	 * @param playerId
	 * @param targetId
	 * @return
	 */
	public int receive(String playerId, Integer targetId) {
		if (!isAllowOprate(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<ActivityHellFireTwoEntity> optionalEntity = this.getPlayerDataEntity(playerId);
		if (!optionalEntity.isPresent()) {
			logger.error("hellfire can not find playerData playerId:{}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		ActivityHellFireTwoEntity entity = optionalEntity.get();
		Integer status = entity.getTargetIdsMap().get(targetId);
		if (status == HellFireTwoConst.STATUS_RECEIVED) {
			return Status.Error.HELL_FIRE_ALEARDY_RECEIVED_VALUE;
		} else if (status == HellFireTwoConst.STATUS_NOT_FINISH) {
			return Status.Error.HELL_FIRE_NOT_FINISH_VALUE;
		}

		HawkConfigManager configManager = HawkConfigManager.getInstance();
		ActivityHellFireTwoTargetCfg targetCfg = configManager.getConfigByKey(ActivityHellFireTwoTargetCfg.class, targetId);
		entity.getTargetIdsMap().put(targetId, HellFireTwoConst.STATUS_RECEIVED);
		entity.notifyUpdate();
		this.getDataGeter().takeReward(playerId, targetCfg.getRewardList(), Action.HELL_FIRE_TWO, true);		
		
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
		ActivityHellFireTwoKVCfg config = ActivityHellFireTwoKVCfg.getInstance();
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_NONE) {
			return;
		}
		
		ActivityRankProvider<HellFireTwoRank> rankProvider = ActivityRankContext
				.getRankProvider(ActivityRankType.HELL_FIRE_TWO_RANK, HellFireTwoRank.class);
		List<HellFireTwoRank> rankList = rankProvider.getRankList();
		HellFireTwoRankResp.Builder sbuilder = HellFireTwoRankResp.newBuilder(); 
		for (HellFireTwoRank rank : rankList) {
			sbuilder.addRankList(buildHellFireRank(rank));
		}
		HellFireTwoRank myRank = rankProvider.getRank(playerId);
		sbuilder.setMyRank(myRank.getRank());
		sbuilder.setMyScore(myRank.getScore());
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.HELL_FIRE_TWO_RANK_RESP_VALUE, sbuilder);
		this.getDataGeter().sendProtocol(playerId, protocol);
	}
	
	public HellFireRankMsg buildHellFireRank(HellFireTwoRank rank) {
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
		ActivityHellFireTwoKVCfg config = ActivityHellFireTwoKVCfg.getInstance();
		if (config.getRankType() == HellFireTwoConst.RANK_TYPE_STAGE) {
			return this.hellFireInfo.getStartTime()+""; 
		} else if (config.getRankType() == HellFireTwoConst.RANK_TYPE_ACTIVITY) {
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
		HellFireTwoInfoEntity hellFireInfo = this.hellFireInfo.clone();
		int termId = this.getActivityTermId();
		this.over(hellFireInfo, true, termId);
		
		return true;
	}
	
	/**
	 * 移除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return;
		}
		ActivityRankProvider<HellFireTwoRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.HELL_FIRE_TWO_RANK, HellFireTwoRank.class);
		rankProvider.remMember(playerId);
		rankProvider.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
	
}