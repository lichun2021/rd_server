package com.hawk.activity.type.impl.skinPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.skinPlan.cfg.SkinPlanActivityKVCfg;
import com.hawk.activity.type.impl.skinPlan.cfg.SkinPlanActivityPoolCfg;
import com.hawk.activity.type.impl.skinPlan.cfg.SkinPlanRewardCfg;
import com.hawk.activity.type.impl.skinPlan.entity.SkinPlanEntity;
import com.hawk.game.protocol.Activity.SkinPlanInfoResp;
import com.hawk.game.protocol.Activity.SkinPlanRollDiceResp;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;

public class SkinPlanActivity extends ActivityBase {

	public SkinPlanActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<SkinPlanEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SKIN_PLAN_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SkinPlanActivity activity = new SkinPlanActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SkinPlanEntity> queryList = HawkDBManager.getInstance()
				.query("from SkinPlanEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SkinPlanEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SkinPlanEntity entity = new SkinPlanEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	/**
	 * 充值事件，活动期间充值，1元给一个物品
	 * @param event
	 */
	@Subscribe
	public void onEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<SkinPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SkinPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SkinPlanActivityKVCfg.class);
		if (cfg == null) { 
			return;
		}
		
		RewardItem.Builder cfgReward = cfg.getGetItemBuilder();
		int itemId = cfgReward.getItemId();
		int num = event.getDiamondNum() / 10;
		long itemNum = cfgReward.getItemCount() * num;
		RewardItem.Builder reward = RewardItem.newBuilder();
		reward.setItemId(itemId);
		reward.setItemCount(itemNum);
		reward.setItemType(cfgReward.getItemType());
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.add(reward);
		// 邮件发送奖励
		Object[] content;
		content = new Object[1];
		content[0] = getActivityCfg().getActivityName();
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		//发奖
		this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.SKIN_PLAN_RECHARGE, false, RewardOrginType.SKIN_PLAN_REWARD);
		//发邮件
		sendMailToPlayer(playerId, MailConst.MailId.SKIN_PLAN_RECHARGE, title, subTitle, content, rewardList, true);
		logger.info("SkinPlanActivity sendMail addItems from DiamondRechargeEvent ItemId:{}, num:{}", itemId, itemNum);
	}
	/**
	 * 扔骰子逻辑
	 * @param playerId
	 * @return
	 */
	public Result<?> goldTowerRollDice(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<SkinPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SkinPlanEntity entity = opEntity.get();
		//已经领取过最高奖励
		if (entity.getRecvTop() == 1) {
			return Result.fail(Status.Error.SKIN_PLAN_MAX_LEVEL_VALUE);
		}
		//配置取最高分的档位 ，如果之前分已经大于max 则本轮结束
		int limitScore = getMaxScoreCfg();
		//当前分数
		int currentScore = entity.getScore();
		//消耗的物品
		SkinPlanRewardCfg rewardCfg = getRewardCfgByScore(currentScore);
		
		List<RewardItem.Builder> prize = rewardCfg.buildPrize(1);
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.SKIN_PLAN_ROLL_DICE);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//本次是最高奖项
		if (currentScore >= limitScore) {
			entity.setRecvTop(1);
		}
		//扔出的点数 权重
		int diceScore = getRandomDicePoint();
		entity.addScore(diceScore);
		//拿到该档位的奖励
		int level = rewardCfg.getId();
		//正常奖励
		List<RewardItem.Builder> rewardList = getRewardIdByLevel(level);
		//额外奖励
		List<RewardItem.Builder> extRewardList = getExtReward();
		List<RewardItem.Builder> lastRewardList = new ArrayList<>();
		lastRewardList.addAll(rewardList);
		lastRewardList.addAll(extRewardList);
		//发奖
		this.getDataGeter().takeReward(playerId, lastRewardList, 1,  Action.SKIN_PLAN_ROLL_DICE, false, RewardOrginType.SKIN_PLAN_REWARD);
		int afterScore = entity.getScore();
		logger.info("playerId:{}, skin plan roll dice, roolDiceScore:{}, beforeScore:{}, afterScore:{}", playerId, diceScore, currentScore, afterScore);
		//日志
		this.getDataGeter().logSkinPlan(playerId, diceScore, afterScore, currentScore);
		//push客户端消息
		sycGoldTowerResp(playerId, rewardList, diceScore);
		return Result.success();
	}
	/**
	 * 额外奖励
	 */
	public List<Builder> getExtReward(){
		SkinPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SkinPlanActivityKVCfg.class);
		if (cfg == null) {
			HawkLog.errPrintln("SkinPlanActivity, getExtReward cfg == null ");
			return new ArrayList<>() ;
		}
		return cfg.getExtRewardList();
	}
	/**
	 * 根据档位随机id对应的奖励
	 * @param level
	 * @return
	 */
	public List<Builder> getRewardIdByLevel(int level){
		Map<Integer, Integer> weightMap = SkinPlanActivityPoolCfg.getWeightMapByLevel(level);
		int id = HawkRand.randomWeightObject(weightMap);
		SkinPlanActivityPoolCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SkinPlanActivityPoolCfg.class, id);
		if (cfg == null) {
			HawkLog.errPrintln("SkinPlanActivity, getRewardIdByLevel cfg == null, level:{}", level);
			return new ArrayList<>();
		}
		return cfg.getRewardList();
	}
	/**
	 * 随机扔出骰子的点数
	 */
	public int getRandomDicePoint(){
		SkinPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SkinPlanActivityKVCfg.class);
		if (cfg == null) {
			HawkLog.errPrintln("SkinPlanActivity, getRandomDicePoint cfg == null ");
			return 0 ;
		}
		Map<Integer, Integer> weightMap = cfg.getDiceWeightMap();
		int dicePoint = HawkRand.randomWeightObject(weightMap);
		return dicePoint;
	}
	/**
	 * 根据积分判断在那个档位
	 * @param score
	 * @return
	 */
	public SkinPlanRewardCfg getRewardCfgByScore(int score){
		ConfigIterator<SkinPlanRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SkinPlanRewardCfg.class);
		while (configIterator.hasNext()) {
			SkinPlanRewardCfg cfg = configIterator.next();
			String pointStr = cfg.getPoint();
			String[] array = SerializeHelper.split(pointStr, SerializeHelper.ATTRIBUTE_SPLIT);
			int minPoint = SerializeHelper.getInt(array, 0);
			int maxPoint = SerializeHelper.getInt(array, 1);
			if (score >= minPoint && score <= maxPoint) {
				return cfg;
			}
		}
		return null;
	}
	/**
	 * 最高档位积分上限
	 * @return
	 */
	private int getMaxScoreCfg(){
		int size = HawkConfigManager.getInstance().getConfigSize(SkinPlanRewardCfg.class);
		SkinPlanRewardCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(SkinPlanRewardCfg.class, size - 1);
		if (cfg == null) {
			HawkLog.errPrintln("SkinPlanActivity, getMaxScoreCfg size:{}", size);
			return 0;
		}
		String pointStr = cfg.getPoint();
		String[] array = SerializeHelper.split(pointStr, SerializeHelper.ATTRIBUTE_SPLIT);
		int maxPoint = SerializeHelper.getInt(array, 0);
		return maxPoint;
	}
	
	/**
	 * 同步金字塔活动数据
	 * @param playerId
	 */
	public void sycGoldTowerInfo(String playerId) {
		Optional<SkinPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SkinPlanEntity entity = opEntity.get();
		SkinPlanInfoResp.Builder builder = SkinPlanInfoResp.newBuilder();
		builder.setScore(entity.getScore());
		boolean canRoll = entity.getRecvTop() == 0 ? true : false;
		builder.setCanRoll(canRoll);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SKIN_PLAN_INFO_RESP_VALUE, builder));
	}
	/**
	 * 扔骰子同步奖励和积分信息
	 * @param playerId
	 * @param rewardList
	 */
	public void sycGoldTowerResp(String playerId, List<RewardItem.Builder> rewardList, int dicePoint) {
		Optional<SkinPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SkinPlanEntity entity = opEntity.get();
		SkinPlanRollDiceResp.Builder builder = SkinPlanRollDiceResp.newBuilder();
		builder.setScore(entity.getScore());
		builder.setRollPoint(dicePoint); 
		boolean canRoll = entity.getRecvTop() == 0 ? true : false;
		builder.setCanRoll(canRoll);
		if (rewardList != null && !rewardList.isEmpty()) {
			rewardList.forEach(rewardBuilder -> builder.addRewards(rewardBuilder));
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SKIN_PLAN_ROLL_DICE_RESP_VALUE, builder));
	}

}
