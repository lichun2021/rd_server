package com.hawk.activity.type.impl.hellfirethree;

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
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
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
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeConditionCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeCycleCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeKVCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeTargetCfg;
import com.hawk.activity.type.impl.hellfirethree.entity.ActivityHellFireThreeEntity;
import com.hawk.activity.type.impl.hellfirethree.entity.HellFireThreeInfoEntity;
import com.hawk.game.protocol.Activity.HellFireInfoMsg;
import com.hawk.game.protocol.Activity.HellFireThreeInfoS;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

public class HellFireThreeActivity extends ActivityBase {
	/**
	 * 记录活动的一些东西
	 */
	private HellFireThreeInfoEntity hellFireInfo;
	/**
	 * 是否经过初始化
	 */
	private boolean isInit;
	/**
	 * 阶段
	 */
	private int stage;

	public HellFireThreeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HELL_FIRE_THREE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new HellFireThreeActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityHellFireThreeEntity> hellFireEntityList = HawkDBManager.getInstance().query(
				"from ActivityHellFireThreeEntity where playerId=? and termId=? and invalid=0", playerId, termId);
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
		ActivityHellFireThreeEntity entity = new ActivityHellFireThreeEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		entity.setCycleStartTime(0);
		entity.setTargetIdsMap(new HashMap<>());

		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		try {
			if (!this.isShow(playerId)) {
				return;
			}

			HellFireInfoMsg.Builder infoMsg = HellFireInfoMsg.newBuilder();
			Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
			if (!opHellFireEntity.isPresent()) {
				return;
			}
			ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
			ActivityHellFireThreeKVCfg hellFireKVCfg = ActivityHellFireThreeKVCfg.getInstance();
			long endTime = (this.hellFireInfo.getCycleStartTime() + hellFireKVCfg.getEffectiveTime()
					+ hellFireKVCfg.getAccountTime()) * 1000l;
			infoMsg.setCycleId(this.hellFireInfo.getCycleId());
			infoMsg.setEndTime(endTime);
			infoMsg.addAllTargetCfgIdStatus(this.buildKeyValue(hellFireEntity.getTargetIdsMap()));
			infoMsg.setScore(hellFireEntity.getScore());

			HellFireThreeInfoS.Builder sbuilder = HellFireThreeInfoS.newBuilder();
			sbuilder.setInfoMsg(infoMsg);

			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.HELL_FIRE_THREE_INFO_S_VALUE, sbuilder);
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
		Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
		// 周期对不上，就重置数据.
		if (this.hellFireInfo.getCycleStartTime() != hellFireEntity.getCycleStartTime()) {
			this.resetPlayerData(playerId);
		}
	}

	private boolean isPlayerOpen(String playerId) {
		int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
		int unlockLevel = ActivityHellFireThreeKVCfg.getInstance().getUnlockCondition();

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
		ActivityHellFireThreeKVCfg hellFireCfg = ActivityHellFireThreeKVCfg.getInstance();
		long now = HawkTime.getMillisecond();
		long cycleStartTime = this.hellFireInfo.getCycleStartTime() * 1000L;
		if (now <= cycleStartTime + hellFireCfg.getEffectiveTime() * 1000) {
			if (this.stage != HellFireThreeConst.STAGE_EFFECT) {
				onEffectStart();
			}
		} else {
			// 进入下一次循环伴随一次开始动作
			doAsyncOver();
			ActivityState state = this.getActivityEntity().getActivityState();
			// 在end状态也有可能调用该状态.
			if (state == ActivityState.OPEN || state == ActivityState.SHOW) {
				this.toNextCycle();
				this.onEffectStart();
			}
		}

	}

	private void doAsyncOver() {
		this.stage = HellFireThreeConst.STAGE_ACCOUNT;
		HellFireThreeInfoEntity hellFireInfoEntity = this.hellFireInfo;
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

			@Override
			public Object run() {
				HellFireThreeActivity.this.over(hellFireInfoEntity);
				return null;
			}
		});

	}

	private void onEffectStart() {
		logger.info("hell fire three onEffectStart");
		this.stage = HellFireThreeConst.STAGE_EFFECT;
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		int unlockLevel = ActivityHellFireThreeKVCfg.getInstance().getUnlockCondition();
		for (String playerId : playerIds) {
			int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
			if (cityLvl >= unlockLevel) {
				this.callBack(playerId, MsgId.HELL_FIRE_THREE_ACTIVITY_EFFECT_START, () -> {
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
		Optional<ActivityHellFireThreeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity entity = opEntity.get();
		PlayerData4Activity playerData4Activity = this.getDataGeter().getPlayerData4Activity(playerId);
		entity.setCycleStartTime(this.hellFireInfo.getCycleStartTime());
		entity.setScore(0);
		entity.setInitBuildingBattlePoint(playerData4Activity.getBuildingBattlePoint());
		entity.setInitTechBattlePoint(playerData4Activity.getTechBattlePoint());
		entity.setOtherSumScore(0);
		entity.setCycleStartTime(this.hellFireInfo.getCycleStartTime());
		entity.setTargetIdsMap(new HashMap<>());

		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		List<Integer> playerTargetIdList = this.getPlayerTargetIdList(cityLevel);

		playerTargetIdList.stream().forEach(targetId -> entity.getTargetIdsMap().put(targetId, 0));
	}

	private List<Integer> getPlayerTargetIdList(int cityLevel) {
		List<Integer> targetIdList = this.hellFireInfo.getTargetCfgIdList();
		ActivityHellFireThreeTargetCfg targetCfg = null;
		List<Integer> playerTargetIdList = new ArrayList<>();
		for (Integer targetId : targetIdList) {
			targetCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityHellFireThreeTargetCfg.class, targetId);
			if (targetCfg.getLvMin() <= cityLevel && cityLevel <= targetCfg.getLvMax()) {
				playerTargetIdList.add(targetId);
			}
		}

		return playerTargetIdList;
	}

	private void init() {
		HellFireThreeInfoEntity entity = this.getHellInfoEntity();
		if (entity == null) {
			this.toNextCycle();
		} else {
			this.hellFireInfo = entity;
			int currentCycleStartTime = this.calcCurrentCycleStartTime();
			// 当前的周期和数据库存储的周期不一样，这个时候需要处理上个周期的数据.
			if (entity.getCycleStartTime() != currentCycleStartTime) {
				this.over(entity);
				this.toNextCycle();
			}
		}

		this.isInit = true;
	}

	/**
	 * 接口名字和字段名字不变, 把次数换成时间
	 * 
	 * @return
	 */
	private int calcCurrentCycleStartTime() {
		int termId = getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long now = HawkTime.getMillisecond();
		int cycleTimes = (int) ((now - startTime - 1)
				/ (ActivityHellFireThreeKVCfg.getInstance().getEffectiveTime() * 1000
						+ ActivityHellFireThreeKVCfg.getInstance().getAccountTime() * 1000)
				+ 1);
		int cycleStartTime = (int) (startTime / 1000)
				+ (cycleTimes - 1) * (ActivityHellFireThreeKVCfg.getInstance().getEffectiveTime()
						+ ActivityHellFireThreeKVCfg.getInstance().getAccountTime());

		return cycleStartTime;
	}

	private void toNextCycle() {
		HellFireThreeInfoEntity entity = this.hellFireInfo;
		Integer nextCycleId = null;

		if (entity == null) {
			entity = new HellFireThreeInfoEntity();
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
		int cycleStartTime = this.calcCurrentCycleStartTime();
		entity.setCycleStartTime(cycleStartTime);

		ActivityHellFireThreeCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireThreeCycleCfg.class, nextCycleId);
		List<ActivityHellFireThreeTargetCfg> targetCfgList = generateCurrentCycleTargetList(cycleCfg);
		List<Integer> targetIdList = new ArrayList<>(targetCfgList.size());
		targetCfgList.stream().forEach(targetCfg -> targetIdList.add(targetCfg.getId()));
		entity.setTargetCfgIdList(targetIdList);

		this.saveHellFireInfoEntity(entity);
	}

	private Integer getNextCycleId(HellFireThreeInfoEntity hellFireInfoEntity) {
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
		ConfigIterator<ActivityHellFireThreeCycleCfg> cycleConfigIter = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireThreeCycleCfg.class);
		List<ActivityHellFireThreeCycleCfg> cycleList = new ArrayList<>();
		while (cycleConfigIter.hasNext()) {
			cycleList.add(cycleConfigIter.next());
		}

		Collections.shuffle(cycleList);
		List<Integer> cycleIdList = new ArrayList<>();
		cycleList.stream().forEach(cfg -> cycleIdList.add(cfg.getCycleId()));

		return cycleIdList;
	}

	private void saveHellFireInfoEntity(HellFireThreeInfoEntity entity) {
		String str = JSON.toJSONString(entity);
		ActivityLocalRedis.getInstance().set(ActivityRedisKey.HELL_FIRE_3, str);
	}

	private HellFireThreeInfoEntity getHellInfoEntity() {
		String str = ActivityLocalRedis.getInstance().get(ActivityRedisKey.HELL_FIRE_3);
		if (str != null) {
			return JSON.parseObject(str, HellFireThreeInfoEntity.class);
		}

		return null;
	}

	private List<ActivityHellFireThreeTargetCfg> generateCurrentCycleTargetList(
			ActivityHellFireThreeCycleCfg cycleCfg) {
		List<ActivityHellFireThreeTargetCfg> targetList = new ArrayList<>();
		Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> table = this.getDataGeter()
				.getHellThreeTargetCfgTable();
		int[][] difficultly = ActivityHellFireThreeKVCfg.getInstance().getDifficultRndWeightArray();

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
	public void over(HellFireThreeInfoEntity hellFireInfo) {
		this.clearFinishTargetPlayer(hellFireInfo.getCycleStartTime());
	}

	public void addFinishTargetPlayer(int startTime, String playerId, Map<Integer, Integer> map) {
		ActivityLocalRedis.getInstance().hset(ActivityRedisKey.HELL_FIRE_FINISH_3 + ":" + startTime, playerId,
				SerializeHelper.mapToString(map));
	}

	public Map<String, String> getFinishTargetPlayer(int startTime) {
		Map<String, String> map = ActivityLocalRedis.getInstance()
				.hgetAll(ActivityRedisKey.HELL_FIRE_FINISH_3 + ":" + startTime);

		return map;
	}

	public void clearFinishTargetPlayer(int startTime) {
		ActivityLocalRedis.getInstance().del(ActivityRedisKey.HELL_FIRE_FINISH_3 + ":" + startTime);
	}

	public void clearFinishTargetPlayer(int startTime, String playerId) {
		ActivityLocalRedis.getInstance().hDel(ActivityRedisKey.HELL_FIRE_FINISH_3 + ":" + startTime, playerId);
	}

	@Subscribe
	public void onTrainCompleteEvent(TrainSoldierCompleteEvent event) {
		String playerId = event.getPlayerId();
		if (!canTriggerEvent(playerId, HellFireThreeConst.SCORE_TYPE_DRAIN)) {
			return;
		}

		Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
		int scoreCof = this.getScoreCfg(HellFireThreeConst.SCORE_TYPE_DRAIN, event.getLevel());
		int addScore = scoreCof * event.getNum();

		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + addScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + addScore);

		this.onScoreChange(hellFireEntity);
	}

	@Subscribe
	public void onHitNewMonster(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		int triggerEventType = 0;
		if ((event.getMosterType() == MonsterType.TYPE_1_VALUE || event.getMosterType() == MonsterType.TYPE_2_VALUE)
				&& event.isKill()) {
			triggerEventType = HellFireThreeConst.SCORE_TYPE_HIT_OLD_MONSTER;
		} else if (event.getMosterType() == MonsterType.TYPE_7_VALUE) {
			triggerEventType = HellFireThreeConst.SCORE_TYPE_HIT_MONSTER;
		} else {
			return;
		}

		if (!canTriggerEvent(playerId, triggerEventType)) {
			return;
		}

		Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
		int hitMonsterTimes = event.getAtkTimes();
		int monsterLevel = event.getMonsterLevel();
		int newMonsterScore = this.getScoreCfg(triggerEventType, monsterLevel) * hitMonsterTimes;

		hellFireEntity.setOtherSumScore(hellFireEntity.getOtherSumScore() + newMonsterScore);
		setScore(hellFireEntity, hellFireEntity.getScore() + newMonsterScore);

		this.onScoreChange(hellFireEntity);
	}

	private int getScoreCfg(int type, int param) {
		ActivityHellFireThreeConditionCfg conditionCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireThreeConditionCfg.class, type);
		if (conditionCfg == null) {
			return 0;
		}
		Integer score = conditionCfg.getScoreCfgMap().get(param);
		return score == null ? 0 : score;
	}

	@Subscribe
	public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireThreeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity entity = opEntity.get();
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
		if (!canTriggerEvent(playerId, HellFireThreeConst.SCORE_TYPE_RESOURCE)) {
			return;
		}

		Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
		int oldScore = hellFireEntity.getScore();
		int newScore = hellFireEntity.getScore();
		int addScore = 0;
		for (Entry<Integer, Double> entry : collectEvent.getCollectMap().entrySet()) {
			int collectScore = this.getScoreCfg(HellFireThreeConst.SCORE_TYPE_RESOURCE, entry.getKey());
			addScore += Math.ceil((entry.getValue() * collectScore));
			newScore += addScore;
		}

		if (oldScore != newScore) {
			hellFireEntity.setOtherSumScore(addScore + hellFireEntity.getOtherSumScore());
			setScore(hellFireEntity, newScore);
			this.onScoreChange(hellFireEntity);
		}
	}

	@Subscribe
	public void onBattlecChangeEvent(BattlePointChangeEvent event) {
		String playerId = event.getPlayerId();
		Optional<ActivityHellFireThreeEntity> opHellFireEntity = getPlayerDataEntity(playerId);
		if (!opHellFireEntity.isPresent()) {
			return;
		}
		ActivityHellFireThreeEntity hellFireEntity = opHellFireEntity.get();
		int oldScore = hellFireEntity.getScore();
		int techBattlePoint = 0;
		int buildingBattlePoint = 0;

		if (canTriggerEvent(playerId, HellFireThreeConst.SCORE_TYPE_TECH)) {
			int techScoreCof = this.getScoreCfg(HellFireThreeConst.SCORE_TYPE_TECH, 1);
			techBattlePoint = (event.getPowerData().getTechBattlePoint() - hellFireEntity.getInitTechBattlePoint())
					* techScoreCof;
		}

		if (canTriggerEvent(HellFireThreeConst.SCORE_TYPE_BUILDING)) {
			int buildScoreCof = this.getScoreCfg(HellFireThreeConst.SCORE_TYPE_BUILDING, 1);
			buildingBattlePoint = (event.getPowerData().getBuildBattlePoint()
					- hellFireEntity.getInitBuildingBattlePoint()) * buildScoreCof;
		}

		int newScore = techBattlePoint + buildingBattlePoint + hellFireEntity.getOtherSumScore();
		logger.info("playerId:{},techBattlePoint:{},buildingBattlePoint:{},otherSunScore:{}", event.getPlayerId(),
				techBattlePoint, buildingBattlePoint, hellFireEntity.getOtherSumScore());
		if (oldScore != newScore) {
			setScore(hellFireEntity, newScore);
			this.onScoreChange(hellFireEntity);
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
		ActivityHellFireThreeCycleCfg cycleCfg = HawkConfigManager.getInstance()
				.getConfigByKey(ActivityHellFireThreeCycleCfg.class, cycleId);

		int[] conditionTypes = cycleCfg.getConditionTypeArray();
		for (int tmpConditionType : conditionTypes) {
			if (tmpConditionType == conditionType) {
				return true;
			}
		}

		return false;
	}

	private boolean isEffectStage() {
		return this.stage == HellFireThreeConst.STAGE_EFFECT;
	}

	/**
	 * 单次增加的值不可能从最大值到int的最大值 这里只是一个保险
	 * 
	 * @param hellFireEntity
	 * @param newScore
	 */
	private void setScore(ActivityHellFireThreeEntity hellFireThreeEntity, int newScore) {
		newScore = newScore > HellFireThreeConst.MAX_SCORE ? HellFireThreeConst.MAX_SCORE : newScore;
		hellFireThreeEntity.setScore(newScore);
	}

	private void onScoreChange(ActivityHellFireThreeEntity hellFireEntity) {
		boolean finish = false;
		for (Entry<Integer, Integer> entry : hellFireEntity.getTargetIdsMap().entrySet()) {
			// 大于0说明已经完成.
			if (entry.getValue().intValue() > 0) {
				continue;
			}

			ActivityHellFireThreeTargetCfg targetCfg = HawkConfigManager.getInstance()
					.getConfigByKey(ActivityHellFireThreeTargetCfg.class, entry.getKey());
			if (targetCfg.getScore() <= hellFireEntity.getScore()) {
				entry.setValue(HellFireThreeConst.STATUS_FINISH);
				hellFireEntity.notifyUpdate();
				finish = true;
			} else {
				finish = finish | false;
			}
		}

		if (finish) {
			this.addFinishTargetPlayer(hellFireEntity.getCycleStartTime(), hellFireEntity.getPlayerId(),
					hellFireEntity.getTargetIdsMap());
			this.syncActivityDataInfo(hellFireEntity.getPlayerId());
		}
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

		Optional<ActivityHellFireThreeEntity> optionalEntity = this.getPlayerDataEntity(playerId);
		if (!optionalEntity.isPresent()) {
			logger.error("hellfire can not find playerData playerId:{}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		ActivityHellFireThreeEntity entity = optionalEntity.get();
		Integer status = entity.getTargetIdsMap().get(targetId);
		if (status == HellFireThreeConst.STATUS_RECEIVED) {
			return Status.Error.HELL_FIRE_ALEARDY_RECEIVED_VALUE;
		} else if (status == HellFireThreeConst.STATUS_NOT_FINISH) {
			return Status.Error.HELL_FIRE_NOT_FINISH_VALUE;
		}

		HawkConfigManager configManager = HawkConfigManager.getInstance();
		ActivityHellFireThreeTargetCfg targetCfg = configManager.getConfigByKey(ActivityHellFireThreeTargetCfg.class,
				targetId);
		entity.getTargetIdsMap().put(targetId, HellFireThreeConst.STATUS_RECEIVED);
		entity.notifyUpdate();
		this.getDataGeter().takeReward(playerId, targetCfg.getRewardList(), Action.HELL_FIRE_THREE, true);

		int receiveNum = 0;
		for (Entry<Integer, Integer> entry : entity.getTargetIdsMap().entrySet()) {
			if (entry.getValue() == HellFireThreeConst.STATUS_RECEIVED) {
				receiveNum++;
			}
		}
		// 说明已经全部都领奖
		if (receiveNum == entity.getTargetIdsMap().size()) {
			this.clearFinishTargetPlayer(this.hellFireInfo.getCycleStartTime(), playerId);
		} else {
			this.addFinishTargetPlayer(this.hellFireInfo.getCycleStartTime(), playerId, entity.getTargetIdsMap());
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}
}