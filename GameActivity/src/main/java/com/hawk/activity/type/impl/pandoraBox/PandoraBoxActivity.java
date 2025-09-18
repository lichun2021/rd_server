package com.hawk.activity.type.impl.pandoraBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.OpenPandoraBoxEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.pandoraBox.cfg.PandoraBoxConfig;
import com.hawk.activity.type.impl.pandoraBox.cfg.PandoraBoxRewardRateConfig;
import com.hawk.activity.type.impl.pandoraBox.cfg.PandoraStoreConfig;
import com.hawk.activity.type.impl.pandoraBox.entity.PandoraBoxEntity;
import com.hawk.game.protocol.Activity.PandoraGoodsMsg;
import com.hawk.game.protocol.Activity.PandoraLotteryInfoResp;
import com.hawk.game.protocol.Activity.PandoraStoreInfoResp;
import com.hawk.game.protocol.Activity.PandoraStoreInfoUpdate;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class PandoraBoxActivity extends ActivityBase {

	public PandoraBoxActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PANDORA_BOX;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		// 仅仅为玩家构建数据表
		if (isOpening(playerId)) {
			Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.PANDORA_OPEN, () -> {
				playerInitEntity(playerId);
			});
		}
	}

	private void playerInitEntity(String playerId) {
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
	}

	/***
	 * 玩家抽奖
	 * 
	 * @param playerId
	 * @param count
	 *            抽奖次数
	 * @return
	 */
	public Result<?> pandoraBoxLottery(String playerId, int count) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		PandoraBoxEntity entity = opEntity.get();
		PandoraBoxConfig config = PandoraBoxConfig.getInstance();
		if (count + entity.getLotteryCount() > config.getMaxDailyLotteryTimes()) {
			return Result.fail(Status.Error.PANDORA_LOTTERY_TIMES_NOT_ENOUGH_VALUE);
		}
		logger.info("pandoraLottery playerId:{}, count:{}", playerId, count);
		if (count == GameConst.PANDORA_NUM_ONE) {
			return lotteryOnce(playerId, entity);
		} else if (count == GameConst.PANDORA_NUM_TEN) {
			return lotteryTenth(playerId, entity);
		} else {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
	}

	private Result<?> lotteryOnce(String playerId, PandoraBoxEntity entity) {
		PandoraBoxConfig config = PandoraBoxConfig.getInstance();
		boolean free = false;

		if (entity.getFreeCount() < config.getFree()) {
			free = true;
		}
		
		PandoraBoxRewardRateConfig rateConfig = getRate();		
		int costNum = 0;
		if (free) {
			entity.setFreeCount(entity.getFreeCount() + 1);
		} else {
			costNum = 1;
			List<RewardItem.Builder> cost = null;
			int itemNum = this.getDataGeter().getItemNum(playerId, config.getCostItemId());
			// 物品够就消耗物品，否则消耗配置的价格
			if (itemNum >= config.getCostOneNum()) {
				cost = config.getItemOneList();
			} else {
				cost = config.getItemOnecePriceList();
			}
			// 扣金条
			boolean flag = this.getDataGeter().cost(playerId, cost, Action.PANDORA_BOX_REWARD);
			if (!flag) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
		}			

		logger.info("playerId:{}, lottery onece costNum:{}", playerId, costNum);
		
		// 构造奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.addAll(rateConfig.getRewardList());
		rewardList.addAll(config.getExtRewardList());
		// 获得积分，扣除次数.
		int score = config.getScoreOnce();
		entity.setScore(entity.getScore() + score);
		entity.setLotteryCount(entity.getLotteryCount() + GameConst.PANDORA_NUM_ONE);
		ActivityManager.getInstance().postEvent(new OpenPandoraBoxEvent(playerId, GameConst.PANDORA_NUM_ONE));
		// 发奖励,抽奖+额外奖励.
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.PANDORA_BOX_REWARD, false,
				RewardOrginType.PANDORA_LOTTERY_REWARD);

		// 日志
		this.getDataGeter().logPandoraLottery(playerId, costNum);

		// 额外奖励不推送给前端.
		this.synPandoraLotteryInfo(playerId, rateConfig.getRewardList());

		return Result.success();
	}

	private Result<?> lotteryTenth(String playerId, PandoraBoxEntity entity) {
		PandoraBoxConfig config = PandoraBoxConfig.getInstance();
		//所有的奖励包含额外奖励
		List<RewardItem.Builder> allRewardList = new ArrayList<>();
		//推送给前端的奖励没有额外奖励.
		List<RewardItem.Builder> clientRewardList = new ArrayList<>();
		for (int i = 0; i < GameConst.PANDORA_NUM_TEN; i++) {
			PandoraBoxRewardRateConfig rateConfig = getRate();
			
			allRewardList.addAll(rateConfig.getRewardList());			
			allRewardList.addAll(config.getExtRewardList());
			
			clientRewardList.addAll(rateConfig.getRewardList());
		}
		int itemNum = this.getDataGeter().getItemNum(playerId, config.getCostItemId());
		int onceItemNum = config.getCostOneNum();
		// 用道具抽奖的次数
		int itemTimes = (itemNum / onceItemNum);
		itemTimes = itemTimes > GameConst.PANDORA_NUM_TEN ? GameConst.PANDORA_NUM_TEN : itemTimes;
		int buyTimes = (GameConst.PANDORA_NUM_TEN - itemTimes);
		List<RewardItem.Builder> cost = new ArrayList<>();
		if (buyTimes > 0) {
			for (RewardItem.Builder builder : config.getItemOnecePriceList()) {
				RewardItem.Builder newBuilder = RewardItem.newBuilder();
				newBuilder.setItemId(builder.getItemId());
				newBuilder.setItemType(builder.getItemType());
				newBuilder.setItemCount(builder.getItemCount() * buyTimes);
				cost.add(newBuilder);
			}
		}
		if (itemTimes > 0) {
			for (RewardItem.Builder builder : config.getItemOneList()) {
				RewardItem.Builder newBuilder = RewardItem.newBuilder();
				newBuilder.setItemId(builder.getItemId());
				newBuilder.setItemType(builder.getItemType());
				newBuilder.setItemCount(builder.getItemCount() * itemTimes);
				cost.add(newBuilder);
			}
		}
		boolean flag = this.getDataGeter().cost(playerId, cost, Action.PANDORA_BOX_REWARD);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		ActivityManager.getInstance().postEvent(new OpenPandoraBoxEvent(playerId, GameConst.PANDORA_NUM_TEN));
		//发奖.
		this.getDataGeter().takeReward(playerId, allRewardList, 1, Action.PANDORA_BOX_REWARD, false,
				RewardOrginType.PANDORA_LOTTERY_REWARD);

		logger.info("playerId:{}, lottery ten ", playerId);
		// 获得积分
		int score = config.getScoreTen();
		entity.setScore(entity.getScore() + score);
		entity.setLotteryCount(entity.getLotteryCount() + GameConst.PANDORA_NUM_TEN);
		
		//记录日志.
		this.getDataGeter().logPandoraLottery(playerId, GameConst.PANDORA_NUM_TEN);
		
		//同步客户端
		this.synPandoraLotteryInfo(playerId, clientRewardList);
		
		return Result.success();
	}

	/***
	 * 随机抽取函数
	 * 
	 * @param ita
	 * @return
	 */
	private PandoraBoxRewardRateConfig getRate() {
		ConfigIterator<PandoraBoxRewardRateConfig> configItrator = HawkConfigManager.getInstance()
				.getConfigIterator(PandoraBoxRewardRateConfig.class);
		Map<PandoraBoxRewardRateConfig, Integer> map = new HashMap<>();
		while (configItrator.hasNext()) {
			PandoraBoxRewardRateConfig cfg = configItrator.next();
			map.put(cfg, cfg.getRate());
		}
		PandoraBoxRewardRateConfig chose = HawkRand.randomWeightObject(map);
		if (chose == null) {
			throw new RuntimeException("can not found PandoraBoxRewardRateConfig:" + map);
		}
		return chose;
	}

	/***
	 * 玩家兑换
	 * 
	 * @param playerId
	 * @param configId
	 * @param count
	 * @return
	 */
	public Result<?> pandoraBoxExchange(String playerId, int configId, int count) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		PandoraStoreConfig config = HawkConfigManager.getInstance().getConfigByKey(PandoraStoreConfig.class, configId);
		if (config == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		PandoraBoxEntity entity = opEntity.get();
		if (entity.getScore() < config.getPrice() * count) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		// 看库存够不够
		int boughtNum = entity.getBuyNum(configId);
		if (boughtNum + count > config.getTotal()) {
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}

		entity.addBuyNum(configId, count);
		// 给奖励，扣积分
		entity.setScore(entity.getScore() - (config.getPrice() * count));
		this.getDataGeter().takeReward(playerId, config.getGoodsList(), count, Action.PANDORA_BOX_REWARD, true,
				RewardOrginType.ACTIVITY_REWARD);
		this.synPandoraLotteryInfo(playerId);
		this.updatePandoraStoreInfo(playerId, configId, entity.getBuyNum(configId));
		this.getDataGeter().logPandoraExchange(playerId, configId, count);
		logger.info("pandoraBoxExchange playerId:{}, cfgId:{}", playerId, configId);

		return Result.success();
	}

	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		PandoraBoxConfig kvCfg = PandoraBoxConfig.getInstance();
		if (!isOpening(event.getPlayerId())) {
			return;
		}

		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PandoraBoxEntity entity = opEntity.get();
		if (kvCfg.getFree() > 0) {
			entity.setFreeCount(0);
		}
		entity.setLotteryCount(0);
		/*
		 * //通知客户度红点 PlayerPushHelper.getInstance().pushToPlayer(playerId,
		 * HawkProtocol.valueOf(HP.code.PANDORA_REFRESH_COUNT_RESP_VALUE));
		 */
		if (!event.isLogin()) {
			this.synPandoraLotteryInfo(playerId);
		}

		if (kvCfg.storeReset()) {
			// 刷新商城
			entity.getStoreInfos().clear();
			entity.notifyUpdate();
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PandoraBoxActivity activity = new PandoraBoxActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PandoraBoxEntity> queryList = HawkDBManager.getInstance()
				.query("from PandoraBoxEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PandoraBoxEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PandoraBoxEntity entity = new PandoraBoxEntity(playerId, termId);
		entity.setFreeCount(0);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	public void synPandoraLotteryInfo(String playerId, List<RewardItem.Builder> rewardList) {
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		PandoraLotteryInfoResp.Builder builder = PandoraLotteryInfoResp.newBuilder();
		builder.setFreeCount(opEntity.get().getFreeCount());
		builder.setScore(opEntity.get().getScore());
		builder.setLotteryCount(opEntity.get().getLotteryCount());
		if (rewardList != null && !rewardList.isEmpty()) {
			rewardList.forEach(rewardBuilder -> builder.addRewards(rewardBuilder));
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.PANDORA_LOTTERY_INFO_RESP_VALUE, builder));
	}

	public void synPandoraLotteryInfo(String playerId) {
		this.synPandoraLotteryInfo(playerId, null);
	}

	public void updatePandoraStoreInfo(String playerId, int id, int num) {
		PandoraStoreInfoUpdate.Builder updateBuilder = PandoraStoreInfoUpdate.newBuilder();
		PandoraGoodsMsg.Builder goodsBuilder = PandoraGoodsMsg.newBuilder();
		goodsBuilder.setId(id);
		goodsBuilder.setBuyNum(num);
		updateBuilder.setGoodsMsg(goodsBuilder.build());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.PANDORA_STORE_INFO_UPDATE_VALUE, updateBuilder));
	}

	public void synPandoraStoreInfo(String playerId) {
		Optional<PandoraBoxEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PandoraBoxEntity entity = opEntity.get();
		PandoraStoreInfoResp.Builder build = PandoraStoreInfoResp.newBuilder();
		PandoraGoodsMsg.Builder goodsBuilder = null;
		for (Entry<Integer, Integer> entry : entity.getStoreInfos().entrySet()) {
			goodsBuilder = PandoraGoodsMsg.newBuilder();
			goodsBuilder.setId(entry.getKey());
			goodsBuilder.setBuyNum(entry.getValue());
			build.addGoods(goodsBuilder);
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.PANDORA_STORE_INFO_RESP_VALUE, build));
	}

	/**
	 * 同步活动内容数据
	 * 
	 * @param playerId
	 */
	public void syncActivityDataInfo(String playerId) {
		this.synPandoraLotteryInfo(playerId);
	}
}
