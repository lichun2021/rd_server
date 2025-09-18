package com.hawk.activity.type.impl.hiddenTreasure;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HiddenTreasureOpenBoxEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureActivityKVCfg;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureAwardPoolCfg;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureBoxAchieveCfg;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureExchangeAwardCfg;
import com.hawk.activity.type.impl.hiddenTreasure.entity.HiddenTreasureBox;
import com.hawk.activity.type.impl.hiddenTreasure.entity.HiddenTreasureEntity;
import com.hawk.game.protocol.Activity.PBHiddenTreasureBox;
import com.hawk.game.protocol.Activity.PBHiddenTreasureExchange;
import com.hawk.game.protocol.Activity.PBHiddenTreasureInfo;
import com.hawk.game.protocol.Activity.PBHiddenTreasureOpenboxReq;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class HiddenTreasureActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");

	public HiddenTreasureActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HIDDEN_TREASURE_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HiddenTreasureActivity activity = new HiddenTreasureActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		super.onPlayerLogin(playerId);
		if (this.isOpening(playerId)) {
			Optional<HiddenTreasureEntity> opEntity = getPlayerDataEntity(playerId);
			if (opEntity.isPresent()) {
				opEntity.get().recordLoginDay();
			}
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (this.isOpening(playerId)) {
			Optional<HiddenTreasureEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			HiddenTreasureEntity entity = opEntity.get();
			PBHiddenTreasureInfo.Builder builder = PBHiddenTreasureInfo.newBuilder();
			builder.setNextFree(entity.getNextFree());
			builder.setRefreshtimes(entity.getRefreshtimes());
			builder.setPurchaseItemTimes(entity.getPurchaseItemTimes());
			builder.setLottoryCount(entity.getLottoryCount());
			for (HiddenTreasureBox box : entity.getNineBox()) {
				builder.addNineBox(PBHiddenTreasureBox.newBuilder().setPoolCfgId(box.getPoolCfgId()).setOpen(box.isOpen()));
			}

			Map<Integer, Integer> emap = entity.getExchangeNumMap();

			for (Entry<Integer, Integer> entry : emap.entrySet()) {
				PBHiddenTreasureExchange.Builder ebuilder = PBHiddenTreasureExchange.newBuilder();
				ebuilder.setExchangeId(entry.getKey());
				ebuilder.setNum(entry.getValue());
				builder.addExchanges(ebuilder);
			}

			// push
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HIDDEN_TREASURE_INFO_SYNC, builder));
		}
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HiddenTreasureEntity> queryList = HawkDBManager.getInstance()
				.query("from HiddenTreasureEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HiddenTreasureEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HiddenTreasureEntity entity = new HiddenTreasureEntity(playerId, termId);
		return entity;
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<HiddenTreasureEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		HiddenTreasureEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public void onOpen() {
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		for (String playerId : playerIds) {
			this.callBack(playerId, GameConst.MsgId.ACHIEVE_INIT_MEDAL_TREASURE, () -> {
				initAchieve(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	// 初始化成就
	private void initAchieve(String playerId) {
		Optional<HiddenTreasureEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		// 为空则初始化
		HiddenTreasureEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<HiddenTreasureBoxAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HiddenTreasureBoxAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			HiddenTreasureBoxAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.recordLoginDay();
		entity.setItemList(itemList);

		HiddenTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(HiddenTreasureActivityKVCfg.class);
		for (Integer bid : kvcfg.getFirstExtractionList()) {
			HiddenTreasureBox box = new HiddenTreasureBox();
			box.setPoolCfgId(bid);
			entity.getNineBox().add(box);
		}
		
		ConfigIterator<HiddenTreasureAwardPoolCfg> it = HawkConfigManager.getInstance().getConfigIterator(HiddenTreasureAwardPoolCfg.class);
		for (int i = entity.getNineBox().size(); i < 9; i++) {
			HiddenTreasureAwardPoolCfg cfg = HawkRand.randomWeightObject(it.toList());
			HiddenTreasureBox box = new HiddenTreasureBox();
			box.setPoolCfgId(cfg.getRewardId());
			entity.getNineBox().add(box);
		}
		entity.notifyUpdate();
		// 初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		HiddenTreasureBoxAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(HiddenTreasureBoxAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<HiddenTreasureEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	/***
	 * 抽奖
	 */
	public Result<?> lottery(String playerId, PBHiddenTreasureOpenboxReq req) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<HiddenTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HiddenTreasureEntity entity = opEntity.get();

		List<HiddenTreasureBox> all = entity.getNineBox();
		List<HiddenTreasureBox> toOpen = new ArrayList<>();
		if (req.getOpenAll()) {// 全开 
			for (HiddenTreasureBox box : all) {
				if (!box.isOpen()) {
					toOpen.add(box);
				}
			}
		} else {
			HiddenTreasureBox box = all.get(req.getIndex() - 1);
			if (!box.isOpen()) {
				toOpen.add(box);
			}
		}

		if (toOpen.isEmpty()) {
			return Result.success(null);
		}

		HiddenTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(HiddenTreasureActivityKVCfg.class);
		int modelType = getDataGeter().getShowDress(playerId, DressType.TITLE_VALUE);
		boolean chenHao = modelType == kvcfg.getModelType();
		List<RewardItem.Builder> allReward = new ArrayList<>();
		for (HiddenTreasureBox box : toOpen) {
			box.setOpen(true);
			HiddenTreasureAwardPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(HiddenTreasureAwardPoolCfg.class, box.getPoolCfgId());
			String rewards = getDataGeter().getItemAward(poolCfg.getReward());
			allReward.addAll(RewardHelper.toRewardItemList(rewards));
			if (chenHao) {
				allReward.addAll(RewardHelper.toRewardItemList(poolCfg.getExtraReward()));
			}
		}

		// 消耗道具
		RewardItem.Builder cost = RewardHelper.toRewardItem(kvcfg.getOpenTreasureCost());
		cost.setItemCount(cost.getItemCount() * toOpen.size());
		// 已有道具数量
		int itemCnt = getDataGeter().getItemNum(playerId, cost.getItemId());
		if (itemCnt < cost.getItemCount()) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		List<RewardItem.Builder> consumeList = new ArrayList<>();
		if (itemCnt >= cost.getItemCount()) {
			consumeList.add(cost);
		} else {
			// 还需要道具数量
			int needBuyCnt = (int) (cost.getItemCount() - itemCnt);
			// 单个物品价格
			RewardItem.Builder price = RewardHelper.toRewardItem(kvcfg.getPurchaseItemCost());
			// 总价格
			price.setItemCount(price.getItemCount() * needBuyCnt);
			consumeList.add(price);
			if (itemCnt > 0) {
				cost.setItemCount(itemCnt);
				consumeList.add(cost);
			}
		}
		boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code2.HIDDEN_TREASURE_OPEN_BOX_REQ_VALUE, Action.HIDDEN_TREASURE_LOTTERY_COST);
		if (!consumeResult) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		this.getDataGeter().takeReward(playerId, allReward, 1, Action.HIDDEN_TREASURE_LOTTERY, true);

		entity.setLottoryCount(entity.getLottoryCount() + toOpen.size());
		// // 抽奖事件
		ActivityManager.getInstance().postEvent(new HiddenTreasureOpenBoxEvent(playerId, entity.getLottoryCount()));
		
		// 通报
		for (RewardItem.Builder send : allReward) {
			if (send.getItemId() == kvcfg.getNoticeItemPB().getItemId() && send.getItemCount() >= kvcfg.getNoticeItemPB().getItemCount()) {
				String playerName = getDataGeter().getPlayerName(playerId);
				this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.HIDDEN_TREASURE_BIG_RAWARD, null, playerName, send.getItemCount());
			}
		}
		
		// // 抽奖打点
		// getDataGeter().logMedalTreasureLottery(playerId, lotCnt);
		logger.info("HiddenTreasureActivity lottery lotCnt:{}, itemCnt:{}, modelType:{}", playerId, toOpen.size(), itemCnt, modelType);

		boolean openAll = true;
		for (HiddenTreasureBox box : all) {
			if(!box.isOpen()){
				openAll = false;
			}
		}
		if(openAll){
			refreshNineBox(playerId, true);
		}
		
		syncActivityDataInfo(playerId);
		return Result.success(null);
	}

	/***
	 * 刷新
	 */
	public Result<?> refreshNineBox(String playerId, boolean sysRefresh) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<HiddenTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}

		HiddenTreasureEntity entity = opEntity.get();
		HiddenTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(HiddenTreasureActivityKVCfg.class);
		if (entity.getRefreshtimes() >= kvcfg.getRefreshTimes()) {
			return Result.fail(Status.Error.SUPER_DISCOUNT_REFRESH_TIMES_VALUE);
		}

		boolean freeTime = entity.getNextFree() < HawkTime.getMillisecond();
		if (!sysRefresh && !freeTime) {
			List<RewardItem.Builder> cost = RewardHelper.toRewardItemList(kvcfg.getRefreshCost());
			boolean consumeResult = getDataGeter().consumeItems(playerId, cost, HP.code2.HIDDEN_TREASURE_REFRESH_BOX_REQ_VALUE, Action.HIDDEN_TREASURE_REFRESH);
			if (!consumeResult) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}

		}

		entity.getNineBox().clear();
		ConfigIterator<HiddenTreasureAwardPoolCfg> it = HawkConfigManager.getInstance().getConfigIterator(HiddenTreasureAwardPoolCfg.class);
		for (int i = 0; i < 9; i++) {
			HiddenTreasureAwardPoolCfg cfg = HawkRand.randomWeightObject(it.toList());
			HiddenTreasureBox box = new HiddenTreasureBox();
			box.setPoolCfgId(cfg.getRewardId());
			entity.getNineBox().add(box);
		}
		entity.notifyUpdate();

		entity.setRefreshtimes(entity.getRefreshtimes() + 1);
		if (!sysRefresh && freeTime) {
			entity.setNextFree(HawkTime.getMillisecond() + kvcfg.getFreeRefreshTimes() * 1000);
		}

		return Result.success(null);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<HiddenTreasureEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		HiddenTreasureEntity entity = optional.get();
		entity.recordLoginDay();
		this.syncActivityDataInfo(playerId);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(HiddenTreasureBoxAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.HIDDEN_TREASURE_REWARD;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	public Result<?> purchaseItemCost(String playerId, int number) {

		// # 道具购买花费
		// purchaseItemCost = 10000_1000_10
		//
		// # 道具可购买次数
		// purchaseItemTimes = 30
		//
		// # 道具ID
		// accelerateItemId = 30000_2980001_1

		Optional<HiddenTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HiddenTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(HiddenTreasureActivityKVCfg.class);
		HiddenTreasureEntity entity = opEntity.get();
		if (entity.getPurchaseItemTimes() + number > kvcfg.getPurchaseItemTimes()) {
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}

		// 消耗道具
		RewardItem.Builder cost = RewardHelper.toRewardItem(kvcfg.getPurchaseItemCost());
		cost.setItemCount(cost.getItemCount() * number);

		boolean consumeResult = getDataGeter().consumeItems(playerId, Arrays.asList(cost), HP.code2.HIDDEN_TREASURE_BOX_ITEM_BUY_REQ_VALUE, Action.HIDDEN_TREASURE_BUY);
		if (!consumeResult) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		RewardItem.Builder get = RewardHelper.toRewardItem(kvcfg.getAccelerateItemId());
		get.setItemCount(get.getItemCount() * number);
		this.getDataGeter().takeReward(playerId, Arrays.asList(get), 1, Action.HIDDEN_TREASURE_BUY, true);

		entity.setPurchaseItemTimes(entity.getPurchaseItemTimes() + number);
		this.syncActivityDataInfo(playerId);
		return Result.success(null);
	}

	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId, int exchangeId, int exchangeCount, int protocolType) {
		HiddenTreasureExchangeAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(HiddenTreasureExchangeAwardCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		Optional<HiddenTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HiddenTreasureEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeId);
		if (eCount + exchangeCount > config.getLimittimes()) {
			logger.info("HiddenTreasureActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
			return;
		}

		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemImmutableList(config.getExchangerequirements());
		boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount, Action.HIDDEN_TREASURE_EXCAHNGE, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}

		// 增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		// 发奖励
		this.getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(config.getExchangeobtain()),
				exchangeCount, Action.HIDDEN_TREASURE_EXCAHNGE, true);
		// 同步
		this.syncActivityDataInfo(playerId);
		if (config.getNoticeId() > 0) {
			String playerName = getDataGeter().getPlayerName(playerId);
			this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.valueOf(config.getNoticeId()), null, playerName);
		}
		logger.info("HiddenTreasureActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);

	}

}
