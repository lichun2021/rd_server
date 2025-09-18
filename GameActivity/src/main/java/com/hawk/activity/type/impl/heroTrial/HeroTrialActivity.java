package com.hawk.activity.type.impl.heroTrial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.hawk.uuid.HawkUUIDGenerator;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.heroTrial.cfg.HeroTrialActivityKVCfg;
import com.hawk.activity.type.impl.heroTrial.cfg.HeroTrialMissionCfg;
import com.hawk.activity.type.impl.heroTrial.entity.HeroTrialActivityEntity;
import com.hawk.activity.type.impl.heroTrial.temp.HeroTrialTemplate;
import com.hawk.game.protocol.Activity.HeroTrialInfo;
import com.hawk.game.protocol.Activity.HeroTrialPageInfoResp;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 英雄试炼
 * 
 * @author golden
 *
 */
public class HeroTrialActivity extends ActivityBase {

	/**
	 * 构造
	 */
	public HeroTrialActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_TRIAL;
	}

	/**
	 * 实例
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroTrialActivity activity = new HeroTrialActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	/**
	 * 加载db数据
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroTrialActivityEntity> queryList = HawkDBManager.getInstance().query("from HeroTrialActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId,
				termId);
		if (queryList != null && queryList.size() > 0) {
			HeroTrialActivityEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建db数据
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroTrialActivityEntity entity = new HeroTrialActivityEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	@Override
	public void onPlayerLogin(String playerId) {
		checkMissionRefresh(playerId);
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		opEntity.get().setAcceptTimes(0);
		opEntity.get().setRefreshTimes(0);
		pushPageInfo(playerId);
	}

	/**
	 * 推界面信息
	 */
	public void pushPageInfo(String playerId) {
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HERO_TRIAL_PAGE_INFO_RESP, getPageInfo(playerId)));
	}

	/**
	 * 获取活动页面信息
	 */
	public HeroTrialPageInfoResp.Builder getPageInfo(String playerId) {
		
		checkMissionRefresh(playerId);
		
		HeroTrialPageInfoResp.Builder builder = HeroTrialPageInfoResp.newBuilder();

		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			builder.setAcceptTimes(0);
			return builder;
		}

		// 所有试炼中的英雄id
		Set<Integer> trialHeroIds = new HashSet<>();

		HeroTrialActivityEntity entity = opEntity.get();

		// 已经接受任务的次数
		builder.setAcceptTimes(entity.getAcceptTimes());

		for (HeroTrialTemplate mission : entity.getMissionSet()) {
			HeroTrialMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTrialMissionCfg.class, mission.getMissionId());
			if (cfg == null) {
				continue;
			}

			boolean hasAccept = mission.getReceiveTime() > 0L;

			HeroTrialInfo.Builder missionBuilder = HeroTrialInfo.newBuilder();
			// 任务id
			missionBuilder.setMissionCfgId(mission.getMissionId());
			// 是否已经接受任务
			missionBuilder.setHasAccept(hasAccept);

			missionBuilder.setMissionUUid(mission.getUuid());

			if (hasAccept) {

				long raminTime = getMissionRaminTime(mission, cfg);

				// 是否有进阶奖励
				missionBuilder.setHasAdvance(mission.isHasAdvanced());
				// 剩余时间
				missionBuilder.setRaminTime(raminTime);
				// 添加试炼英雄id
				for (Integer heroId : mission.getHeroList()) {
					missionBuilder.addHeroId(heroId);
					trialHeroIds.add(heroId);
				}

				missionBuilder.setCanAwardTime(mission.getReceiveTime() + cfg.getContinueTime());
			}
			builder.addInfo(missionBuilder);
		}

		// 所有试炼中的英雄id
		for (int heroId : trialHeroIds) {
			builder.addTrialHeroIds(heroId);
		}

		return builder;
	}

	/**
	 * 检测任务刷新
	 */
	public void checkMissionRefresh(String playerId) {
		boolean needRefresh = needRefresh(playerId);
		if (needRefresh) {
			refreshMission(playerId);
		}
	}

	public boolean needRefresh(String playerId) {
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		long lastRefreshTime = entity.getLastRefreshTime();
		long currentTime = HawkTime.getMillisecond();

		// 异常情况，上次刷新时间等于当前时间，不处理
		if (lastRefreshTime == currentTime) {
			return false;
		}

		// 异常情况，上次刷新时间大于当前时间，则刷新重置为当前时间
		if (lastRefreshTime > currentTime) {
			return true;
		}

		// 不是一天，直接刷新
		if (!HawkTime.isSameDay(lastRefreshTime, currentTime)) {
			return true;
		}

		int[] refreshTimeArray = HeroTrialActivityKVCfg.getInstance().getRefreshTime();
		for (int i = 0; i < refreshTimeArray.length; i++) {
			long refreshTime = HawkTime.getHourOfDayTime(currentTime, refreshTimeArray[i]);
			if (lastRefreshTime < refreshTime && currentTime >= refreshTime) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 刷新任务
	 */
	public void refreshMission(String playerId) {

		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		// 移除过期任务
		removeTimeOutMission(entity.getMissionSet());

		// 生成新任务
		genNewMission(entity.getMissionSet());

		entity.setLastRefreshTime(HawkTime.getMillisecond());
		
		// tlog
		for (HeroTrialTemplate mission : entity.getMissionSet()) {
			getDataGeter().logHeroTrialRefreshMission(playerId, mission.getMissionId());
		}
	}

	/**
	 * 移除过期任务
	 */
	public void removeTimeOutMission(Set<HeroTrialTemplate> missionSet) {
		Iterator<HeroTrialTemplate> iterator = missionSet.iterator();
		while (iterator.hasNext()) {
			HeroTrialTemplate mission = iterator.next();
			// 任务已经被接受，不移除
			if (mission.getReceiveTime() > 0) {
				continue;
			}
			iterator.remove();
		}
	}

	/**
	 * 生成新任务
	 */
	public void genNewMission(Set<HeroTrialTemplate> missionSet) {
		// 池子任务数量
		int missionCount = HawkConfigManager.getInstance().getConfigSize(HeroTrialMissionCfg.class);

		// 刷新数量
		int refreshCount = HeroTrialActivityKVCfg.getInstance().getRefreshCount();

		if (missionCount <= refreshCount) {
			ConfigIterator<HeroTrialMissionCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(HeroTrialMissionCfg.class);
			while (iterator.hasNext()) {
				HeroTrialMissionCfg missionCfg = iterator.next();
				if (missionCfg != null) {
					HeroTrialTemplate mission = new HeroTrialTemplate(HawkUUIDGenerator.genUUID(), missionCfg.getMissionId());
					missionSet.add(mission);
				}
			}
		} else {
			// 已经随机到的不再随机
			Set<Integer> hasRand = new HashSet<>();
			for (int i = 0; i < refreshCount; ) {
				HeroTrialMissionCfg missionCfg = randomCfg();
				if (missionCfg == null) {
					continue;
				}
				if (hasRand.contains(missionCfg.getMissionId())) {
					continue;
				}
				HeroTrialTemplate mission = new HeroTrialTemplate(HawkUUIDGenerator.genUUID(), missionCfg.getMissionId());
				missionSet.add(mission);
				hasRand.add(missionCfg.getMissionId());
				i++;
			}
		}
	}

	/**
	 * 接收任务
	 */
	public void goTrial(String playerId, int hpCode, String missionUUid, List<Integer> heros) {
		logger.info("heroTrail go trial begin, playerId:{}, uuid:{}, heros:{}", playerId, missionUUid, Arrays.toString(heros.toArray()));
		
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		// 试炼英雄数量判断
		int trialCountLimit = HeroTrialActivityKVCfg.getInstance().getOneTrialHeroLimit();
		if (heros.isEmpty() || heros.size() > trialCountLimit) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_COUNT_ERROR_VALUE);
			return;
		}

		// 任务
		HeroTrialTemplate mission = null;

		// 找到接受的任务
		for (HeroTrialTemplate entry : entity.getMissionSet()) {
			if (!entry.getUuid().equals(missionUUid)) {
				continue;
			}
			mission = entry;
		}

		// 没找到任务
		if (mission == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_FOUND_VALUE);
			return;
		}

		// 任务配置不存在
		HeroTrialMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTrialMissionCfg.class, mission.getMissionId());
		if (cfg == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_CFG_NOT_FOUND_VALUE);
			return;
		}

		// 英雄在试炼中
		for (Integer hero : heros) {
			if (entity.getAllTrialHeroIds().contains(hero)) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_HERO_ALREADY_TRIAL_VALUE);
				return;
			}
		}

		// 基础条件不满足
		for (List<Integer> condition : cfg.getBaseConditionList()) {
			if (!getDataGeter().touchHeroTrial(playerId, heros, condition)) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_BASE_CONDITION_FAIL_VALUE);
				return;
			}
		}

		int beforeAcceptTimes = entity.getAcceptTimes();
		int trialLimit = HeroTrialActivityKVCfg.getInstance().getTrialLimit();
		if (beforeAcceptTimes >= trialLimit) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_ACCEPT_MISSION_LIMIT_VALUE);
			return;
		}

		boolean hasAdvanced = true;
		for (List<Integer> condition : cfg.getAdvConditionList()) {
			if (!getDataGeter().touchHeroTrial(playerId, heros, condition)) {
				hasAdvanced = false;
			}
		}

		mission.setHasAdvanced(hasAdvanced);
		mission.setTrialHeros(heros);
		mission.setReceiveTime(HawkTime.getMillisecond());
		entity.setAcceptTimes(entity.getAcceptTimes() + 1);
		
		// 推送信息
		pushPageInfo(playerId);

		// tlog
		getDataGeter().logHeroTrialReceive(playerId, mission.getMissionId());
		
		logger.info("heroTrail go trial success, playerId:{}, uuid:{}, heros:{}", playerId, missionUUid, Arrays.toString(heros.toArray()));
	}

	/**
	 * 刷新界面
	 */
	public void refreshPage(String playerId, int hpCode) {
		logger.info("heroTrail refreshPage begin, playerId:{}", playerId);
		
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			logger.info("heroTrail refreshPage entity not exit, playerId:{}", playerId);
			return;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		// 刷新次数限制判断
		int beforeRefreshTimes = entity.getRefreshTimes();
		int refreshTimesLimit = HeroTrialActivityKVCfg.getInstance().getRefresTimesLimit();
		if (beforeRefreshTimes >= refreshTimesLimit) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_REFRESH_TIMES_LIMIT_VALUE);
			logger.info("heroTrail refreshPage times limit, playerId:{}, beforeRefreshTimes:{}, refreshTimesLimit:{}", playerId, beforeRefreshTimes, refreshTimesLimit);
			return;
		}

		// 消耗
		boolean costSuccess = getDataGeter().consumeItemsIsGold(playerId, HeroTrialActivityKVCfg.getInstance().getRefreshCostBuilder(), hpCode, Action.HERO_TRIAL_REFRESH);
		if (!costSuccess) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			logger.info("heroTrail refreshPage cost failed, playerId:{}", playerId);
			return;
		}

		// 刷新次数+1
		entity.setRefreshTimes(beforeRefreshTimes + 1);
		// 刷新
		refreshMission(playerId);
		// 推送界面信息
		pushPageInfo(playerId);
		
		// tlog
		getDataGeter().logHeroTrialCostRefresh(playerId, HeroTrialActivityKVCfg.getInstance().getRefreshCost());
		
		logger.info("heroTrail refreshPage success, playerId:{}, refreshTimes:{}", playerId, entity.getRefreshTimes());
	}

	/**
	 * 加速
	 */
	public void speedUp(String playerId, int hpCode, String uuid) {
		logger.info("heroTrail speedUp begin, playerId:{}, uuid:{}", playerId, uuid);
		
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			logger.info("heroTrail speedUp entity not exit, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		// 任务
		HeroTrialTemplate mission = null;

		// 找到接受的任务
		for (HeroTrialTemplate entry : entity.getMissionSet()) {
			if (!entry.getUuid().equals(uuid)) {
				continue;
			}
			mission = entry;
			break;
		}

		// 没找到任务
		if (mission == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_FOUND_VALUE);
			logger.info("heroTrail speedUp mission not exit, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		// 任务未接受
		if (mission.getReceiveTime() == 0L) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_ACCEPT_VALUE);
			logger.info("heroTrail speedUp mission not received, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		// 任务配置不存在
		HeroTrialMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTrialMissionCfg.class, mission.getMissionId());
		if (cfg == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_CFG_NOT_FOUND_VALUE);
			logger.info("heroTrail speedUp cfg not found, playerId:{}, uuid:{}, missionId:{}", playerId, uuid, mission.getMissionId());
			return;
		}

		// 任务剩余时间
		long raminTime = getMissionRaminTime(mission, cfg);

		// 需要金币
		int costGold = getDataGeter().caculateTimeGold(raminTime, SpeedUpTimeWeightType.HERO_TRIAL);

		// 消耗
		boolean costSuccess = getDataGeter().consumeGold(playerId, costGold, hpCode, Action.HERO_TRIAL_SPEED_UP);
		if (!costSuccess) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			logger.info("heroTrail speedUp cost failed, playerId:{}", playerId);
			return;
		}

		// 发奖
		if (mission.isHasAdvanced()) {
			getDataGeter().takeReward(playerId, cfg.getBaseAndAdvAward(), Action.HERO_TRIAL_TAKE_AWARD, true);
		} else {
			getDataGeter().takeReward(playerId, cfg.getBaseAwardList(), Action.HERO_TRIAL_TAKE_AWARD, true);
		}

		// 删除任务
		entity.removeMission(uuid);

		// 推送界面信息
		pushPageInfo(playerId);
		
		// tlog
		getDataGeter().logHeroTrialComplete(playerId, mission.getMissionId());
		getDataGeter().logHeroTrialCostSpeed(playerId, costGold);
		
		logger.info("heroTrail speedUp success, mission remove, playerId:{}, uuid:{}", playerId, uuid);
	}

	/**
	 * 获取任务剩余时间
	 */
	public long getMissionRaminTime(HeroTrialTemplate mission, HeroTrialMissionCfg cfg) {
		// 接受任务时间
		long receiveTime = mission.getReceiveTime();
		// 任务持续时间
		long continueTime = cfg.getContinueTime();
		// 还剩多久可以领奖励
		long raminTime = Math.max(receiveTime + continueTime - HawkTime.getMillisecond(), 0L);
		return raminTime;
	}

	/**
	 * 领取任务奖励
	 */
	public void getMissionReward(String playerId, int hpCode, String uuid) {
		logger.info("heroTrail get mission award begin, mission remove, playerId:{}, uuid:{}", playerId, uuid);
		
		Optional<HeroTrialActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			logger.info("heroTrail get mission award, entity not exit, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		HeroTrialActivityEntity entity = opEntity.get();

		// 任务
		HeroTrialTemplate mission = null;

		// 找到接受的任务
		for (HeroTrialTemplate entry : entity.getMissionSet()) {
			if (!entry.getUuid().equals(uuid)) {
				continue;
			}
			mission = entry;
			break;
		}

		// 没找到任务
		if (mission == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_FOUND_VALUE);
			logger.info("heroTrail get mission award, mission not found, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		// 任务未接受
		if (mission.getReceiveTime() == 0L) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_ACCEPT_VALUE);
			logger.info("heroTrail get mission award, mission not received, playerId:{}, uuid:{}", playerId, uuid);
			return;
		}

		// 任务配置不存在
		HeroTrialMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroTrialMissionCfg.class, mission.getMissionId());
		if (cfg == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_CFG_NOT_FOUND_VALUE);
			logger.info("heroTrail get mission award, cfg not found, playerId:{}, uuid:{}, missionId:{}", playerId, uuid, mission.getMissionId());
			return;
		}

		// 任务剩余时间
		long raminTime = getMissionRaminTime(mission, cfg);

		// 任务未完成
		if (raminTime > 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, Status.Error.HERO_TRIAL_MISSOIN_NOT_COMPLETE_VALUE);
			logger.info("heroTrail get mission, raminTime > 0, playerId:{}, uuid:{}, raminTime:{}", playerId, uuid, raminTime);
			return;
		}

		// 发奖
		if (mission.isHasAdvanced()) {
			getDataGeter().takeReward(playerId, cfg.getBaseAndAdvAward(), Action.HERO_TRIAL_TAKE_AWARD, true);
		} else {
			getDataGeter().takeReward(playerId, cfg.getBaseAwardList(), Action.HERO_TRIAL_TAKE_AWARD, true);
		}

		// 删除任务
		entity.removeMission(uuid);

		// 推送界面信息
		pushPageInfo(playerId);
		
		// tlog
		getDataGeter().logHeroTrialComplete(playerId, mission.getMissionId());
		
		logger.info("heroTrail get mission award success, mission remove, playerId:{}, uuid:{}", playerId, uuid);
	}

	public HeroTrialMissionCfg randomCfg() {
		HeroTrialMissionCfg result = null;

		try {
			int sumWeight = 0;
			ConfigIterator<HeroTrialMissionCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroTrialMissionCfg.class);
			while (configIterator.hasNext()) {
				HeroTrialMissionCfg cfg = configIterator.next();
				sumWeight += cfg.getWeight();
			}

			int molecular = HawkRand.randInt(sumWeight);

			configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroTrialMissionCfg.class);
			while (configIterator.hasNext()) {
				HeroTrialMissionCfg cfg = configIterator.next();
				molecular = molecular - cfg.getWeight();
				if (molecular < 0) {
					result = cfg;
					break;
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return result;
	}

}
