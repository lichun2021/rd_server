package com.hawk.activity.type.impl.dressuptwo.firereignitetwo;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg.FireReigniteTwoActivityKVCfg;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg.FireReigniteTwoBoxCfg;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg.FireReigniteTwoExchangeCfg;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.entity.FireReigniteTwoEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 圣诞节系列活动二:冬日装扮活动
 * @author hf
 */
public class FireReigniteTwoActivity extends ActivityBase {

	public FireReigniteTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIRE_REIGNITE_TWO_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FireReigniteTwoActivity activity = new FireReigniteTwoActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FireReigniteTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from FireReigniteTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FireReigniteTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FireReigniteTwoEntity entity = new FireReigniteTwoEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<FireReigniteTwoEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_FIRE_REIGNITE_TWO, ()-> {
				Optional<FireReigniteTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on FireReigniteTwoActivity open init FireReigniteTwoEntity error, no entity created:{}" , playerId);
					return;
				}
				syncActivityInfo(playerId, opEntity.get());
			});
		}
	}

	/**
	 * 购买物品
	 * @param playerId
	 * @param num
	 * @return
	 */
	public Result<Integer> exchangeFireReigniteItem(String playerId, int index, int num) {
		FireReigniteTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteTwoActivityKVCfg.class);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<FireReigniteTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		FireReigniteTwoEntity entity = opEntity.get();

		FireReigniteTwoExchangeCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(FireReigniteTwoExchangeCfg.class, index);
		//检测需要金条补齐兑换消耗的资源
		List<Reward.RewardItem.Builder> consumeItemList = new ArrayList<>();
		//需要用金条买几份
		int needBuyCount = 0;
		if (index == 2){
			needBuyCount = getExchangeConsume(playerId, num, consumeItemList, exchangeCfg);
			//index = 2是金条兑换有次数限制
			if (entity.getExchangeNum() + needBuyCount > cfg.getExchangeLimit()){
				return Result.fail(Status.Error.FIRE_REIGNITE_TWO_EXCHANGE_LIMIT_VALUE);
			}
		}else{
			Reward.RewardItem.Builder onceConsumeItem = RewardHelper.toRewardItem(exchangeCfg.getPay());
			onceConsumeItem.setItemCount(onceConsumeItem.getItemCount() * num);
			consumeItemList.add(onceConsumeItem);
		}
		//购买消耗物品检测
		boolean flag = this.getDataGeter().cost(playerId, consumeItemList ,1, Action.FIRE_REIGNITE_TWO_BUY_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//暴击后的数量 普通的无暴击
		int multiple = num;
		//index = 2是金条兑换修改次数
		if (index == 2){
			entity.addExchangeNum(needBuyCount);
			multiple = calculateMultipleNum(cfg, num);
		}
		
		//玩家增加经验
		int addExp = exchangeCfg.getExpGain() * num;
		entity.addExp(addExp);
		// 添加物品
		List<Reward.RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(exchangeCfg.getGain());
		getDataGeter().takeReward(playerId, rewardList, multiple, Action.FIRE_REIGNITE_TWO_BUY_REWARD, true, Reward.RewardOrginType.FIRE_REIGNITE_REWARD_TWO);
		logger.info("FireReigniteTwoActivity exchangeFireReigniteItem playerId:{}, index:{}, num:{}, exchangeNum:{},multiple:{},needBuyCount:{}", playerId, index, num, entity.getExchangeNum(), multiple, needBuyCount);
		//同步
		syncActivityInfo(playerId, entity);
		return Result.success();
	}

	/**
	 * 计算暴击的倍数
	 * @param cfg
	 * @param num
	 * @return
	 */
	public int calculateMultipleNum(FireReigniteTwoActivityKVCfg cfg, int num){
		Map<Integer, Integer> multipleMap = cfg.getStrengthMap();
		int multiple = 0;
		for (int i = 0; i < num; i++) {
			int random = HawkRand.randomWeightObject(multipleMap);
			multiple += random;
		}
		return multiple;
	}
	/**
	 * 领取宝箱
	 * @param playerId
	 * @return
	 */
	public Result<Integer> receiveBoxReward(String playerId) {
		FireReigniteTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteTwoActivityKVCfg.class);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<FireReigniteTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		FireReigniteTwoEntity entity = opEntity.get();
  		//经验值
		int exp = entity.getExp();
		//等级超过上限后,每多少经验还可以另一个宝箱
		HawkTuple2<Integer, Integer> boxTuple = getMaxBoxIdByExp(exp);
		int maxBoxId = boxTuple.first;
		int leftExp = boxTuple.second;
		//可领取的宝箱数
		int canRewardBoxNum = maxBoxId + leftExp / cfg.getBoxExp();
		//已经领取的宝箱数
		int receiveBoxNum = entity.getRecBoxNum();
		if (receiveBoxNum >= canRewardBoxNum){
			return Result.fail(Status.Error.FIRE_REIGNITE_TWO_REWARD_BOX_LIMIT_VALUE);
		}
		//领取个数为已领取宝箱个数+1 宝箱是挨个领取的
		int boxId = receiveBoxNum + 1;
		entity.addRecBox(boxId);
		// 添加物品
		List<Reward.RewardItem.Builder> reward;
		FireReigniteTwoBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(FireReigniteTwoBoxCfg.class, boxId);
		//超等级上限的宝箱为固定奖励
		if (boxCfg != null){
			reward = boxCfg.getLevelRewardsList();
		}else {
			reward = cfg.getBoxRewardList();
		}
		getDataGeter().takeReward(playerId, reward, 1, Action.FIRE_REIGNITE_TWO_BOX_REWARD, true, Reward.RewardOrginType.FIRE_REIGNITE_REWARD_TWO);
		//tlog
		getDataGeter().logFireReigniteReceiveBoxTwo(playerId, getActivityTermId(), boxId);
		logger.info("FireReigniteTwoActivity receiveBoxReward playerId:{}, exp:{},canRewardBoxNum:{},receiveBoxNum:{}", playerId, exp, canRewardBoxNum, receiveBoxNum);
		syncActivityInfo(playerId, entity);
		return Result.success();
	}

	/**
	 * 根据经验计算最大宝箱Id和剩余经验
	 * @param exp
	 * @return
	 */
	public HawkTuple2<Integer, Integer> getMaxBoxIdByExp(int exp){
		ConfigIterator<FireReigniteTwoBoxCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FireReigniteTwoBoxCfg.class);
		int boxId = 0;
		int leftExp = exp;
		while (configIterator.hasNext()){
			FireReigniteTwoBoxCfg cfg = configIterator.next();
			if (exp >= cfg.getConditionExp()){
				boxId = cfg.getBoxId();
				leftExp = exp - cfg.getConditionExp();
			}
		}
		return new HawkTuple2<>(boxId,leftExp);
	}
	/**
	 * 同步消息
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, FireReigniteTwoEntity entity) {
		int exp = entity.getExp();
		int recBoxNum = entity.getRecBoxNum();
		int exchangeNum = entity.getExchangeNum();
		Activity.FireReigniteInfoSync.Builder builder = Activity.FireReigniteInfoSync.newBuilder();
		builder.setExp(exp);
		builder.setRecBoxNum(recBoxNum);
		builder.setExchangeNum(exchangeNum);
		pushToPlayer(playerId, HP.code.FIRE_REIGNITE_TWO_INFO_SYNC_VALUE, builder);
	}

	/**抽奖消耗
	 * @param playerId
	 * @param times
	 * @param consumeItemList
	 * @return
	 */
	private int getExchangeConsume(String playerId, int times, List<Reward.RewardItem.Builder> consumeItemList,FireReigniteTwoExchangeCfg exchangeCfg) {
		FireReigniteTwoActivityKVCfg fireReigniteTwoActivityKVCfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteTwoActivityKVCfg.class);
		//单次消耗
		Reward.RewardItem.Builder onceConsumeItem = RewardHelper.toRewardItem(exchangeCfg.getPay());
		int consumeItemId = onceConsumeItem.getItemId();
		int haveDrewCount = this.getDataGeter().getItemNum(playerId, consumeItemId);

		int totalCount = times *  (int)onceConsumeItem.getItemCount();
		//需要购买的次数
		int needBuyCount = totalCount - haveDrewCount;

		if (needBuyCount > 0) {
			Reward.RewardItem.Builder buyCounsumeItem = RewardHelper.toRewardItem(fireReigniteTwoActivityKVCfg.getItemPrice());
			buyCounsumeItem.setItemCount(buyCounsumeItem.getItemCount() * needBuyCount);
			consumeItemList.add(buyCounsumeItem);
			if (haveDrewCount > 0) {
				onceConsumeItem.setItemCount(haveDrewCount);
				consumeItemList.add(onceConsumeItem);
			}
		}else{
			onceConsumeItem.setItemCount(totalCount);
			consumeItemList.add(onceConsumeItem);
		}
		return Math.max(needBuyCount, 0);
	}
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}


}
