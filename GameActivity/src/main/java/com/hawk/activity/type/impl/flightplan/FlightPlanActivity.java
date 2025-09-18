package com.hawk.activity.type.impl.flightplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple3;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.celebrationShop.cfg.CelebrationShopExchangeCfg;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanAchieveCfg;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanActivityKVCfg;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanCellAwardCfg;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanShopCfg;
import com.hawk.activity.type.impl.flightplan.entity.FlightPlanEntity;
import com.hawk.game.protocol.Activity.CellMoveResp;
import com.hawk.game.protocol.Activity.FlightPlanInfoPB;
import com.hawk.game.protocol.Activity.FlightPlanShopGoodsInfoPB;
import com.hawk.game.protocol.Activity.RollDiceType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 威龙庆典-飞行计划活动
 * 
 * @author lating
 *
 */
public class FlightPlanActivity extends ActivityBase implements AchieveProvider, IExchangeTip<FlightPlanShopCfg> {
	// 正常奖励
	static final int CELL_AWARD_NORMAL  = 1;
	// 多倍奖励
	static final int CELL_AWARD_MULTI  = 2;
	// 空奖励
	static final int CELL_AWARD_NONE  = 3;

	public FlightPlanActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.FLIGHT_PLAN_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.FLIGHT_PLAN_ACHIEVE_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FlightPlanActivity activity = new FlightPlanActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FlightPlanEntity> queryList = HawkDBManager.getInstance()
				.query("from FlightPlanEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FlightPlanEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FlightPlanEntity entity = new FlightPlanEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		FlightPlanEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		
		syncCellInfo(playerId);
	}
	
	/**
	 * 同步信息
	 * 
	 * @param entity
	 */
	public void syncCellInfo(String playerId) {
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		FlightPlanEntity entity = opEntity.get();
		FlightPlanInfoPB.Builder builder = FlightPlanInfoPB.newBuilder();
		builder.setCellId(entity.getCellId() + 1);
		int fragmentCount = getFragmentCount(playerId);
		builder.setFragment(fragmentCount);
		for (Entry<Integer, Integer> entry : entity.getGoodsExchangeMap().entrySet()) {
			FlightPlanShopGoodsInfoPB.Builder goodsInfo = FlightPlanShopGoodsInfoPB.newBuilder();
			goodsInfo.setGoodsId(entry.getKey());
			goodsInfo.setExhangeTimes(entry.getValue());
			builder.addGoods(goodsInfo);
		}
		builder.addAllTips(getTips(FlightPlanShopCfg.class, entity.getTipSet()));
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.FLIGHT_PLAN_INFO_PUSH_VALUE, builder));
	}
	
	/**
	 * 获取碎片数量
	 * 
	 * @param playerId
	 * @return
	 */
	private int getFragmentCount(String playerId) {
		return this.getDataGeter().getItemNum(playerId, FlightPlanShopCfg.getConsumeItemId());
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_FLIGHT_PLAN, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		FlightPlanEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
				
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<FlightPlanAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FlightPlanAchieveCfg.class);
		while (configIterator.hasNext()) {
			FlightPlanAchieveCfg achieveCfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			itemList.add(item);
		}

		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);	
		
		syncCellInfo(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		
		FlightPlanEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public FlightPlanAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(FlightPlanAchieveCfg.class, achieveId);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		FlightPlanEntity entity = opEntity.get();
		FlightPlanInfoPB.Builder builder = FlightPlanInfoPB.newBuilder();
		builder.setCellId(entity.getCellId() + 1);
		int fragmentCount = getFragmentCount(playerId);
		builder.setFragment(fragmentCount);
		for (Entry<Integer, Integer> entry : entity.getGoodsExchangeMap().entrySet()) {
			FlightPlanShopGoodsInfoPB.Builder goodsInfo = FlightPlanShopGoodsInfoPB.newBuilder();
			goodsInfo.setGoodsId(entry.getKey());
			goodsInfo.setExhangeTimes(entry.getValue());
			builder.addGoods(goodsInfo);
		}
		builder.addAllTips(getTips(FlightPlanShopCfg.class, entity.getTipSet()));
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.FLIGHT_PLAN_INFO_PUSH_VALUE, builder));
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		FlightPlanEntity entity = opEntity.get();
		if (!event.isCrossDay()) {
			return;
		}
		
		List<AchieveItem> items = new ArrayList<AchieveItem>();
		ConfigIterator<FlightPlanAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(FlightPlanAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			FlightPlanAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
	}
	
	/**
	 * 摇骰子获取奖励
	 * 
	 * @param playerId
	 */
	public int onRollDice(String playerId, RollDiceType type, int protoType) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		int count = HawkConfigManager.getInstance().getConfigSize(FlightPlanCellAwardCfg.class);
		if (count == 0) {
			HawkLog.errPrintln("FlightPlanActivity cell move failed, config count error, playerId: {}", playerId);
			return Status.SysError.CONFIG_ERROR_VALUE;	
		}
		
		FlightPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FlightPlanActivityKVCfg.class);
		if (cfg == null) {
			HawkLog.errPrintln("FlightPlanActivity cell move failed, FlightPlanActivityKVCfg config not exist, playerId: {}", playerId);
			return Status.SysError.CONFIG_ERROR_VALUE;	
		}
		
		int times = type.getNumber();
		// 消耗
		List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
		int needBuyCount = getRollDiceConsume(playerId, times, consumeItemList);
		boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.FLIGHT_PLAN_CONSUME);
		if (!success) {
			HawkLog.errPrintln("FlightPlanActivity cell move consume not enought, playerId: {}", playerId);
			return 0;
		}
		
		FlightPlanEntity entity = opEntity.get();
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		int cellId = 0, point = 0, ratio = 0;
		for (int i = 0; i < times; i++) {
			HawkTuple3<Integer, Integer, Integer> result = rollDiceOnce(entity, rewardList);
			cellId = result.first;
			point = result.second;
			ratio = result.third;
		}
		
		if (needBuyCount > 0) {
			List<RewardItem.Builder> extRewardItems = RewardHelper.toRewardItemList(cfg.getExtReward());
			extRewardItems.forEach(e -> e.setItemCount(e.getItemCount() * needBuyCount));
			rewardList.addAll(extRewardItems);
		}
		
		int fragmentCount = getFragmentCount(playerId);
		if (!rewardList.isEmpty()) {
			fragmentCount += rewardList.stream().filter(e -> e.getItemId() == FlightPlanShopCfg.getConsumeItemId()).mapToLong(e -> e.getItemCount()).sum();
			ActivityReward reward = new ActivityReward(rewardList, Action.FLIGHT_PLAN_REWARD);
			reward.setAlert(false);
			postReward(playerId, reward, false);
		}
		
		rollDiceResponse(playerId, type, cellId, point, ratio, fragmentCount, rewardList);
		
		HawkLog.logPrintln("FlightPlanActivity roll dice success, playerId: {}, type: {}, cellId: {}, point: {}, awardRatio: {}", playerId, times, cellId, point, ratio);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 计算飞行消耗
	 * 
	 * @param playerId
	 * @param rollTimes
	 * @param consumeItemList
	 * 
	 * @return
	 */
	private int getRollDiceConsume(String playerId, int rollTimes, List<RewardItem.Builder> consumeItemList) {
		FlightPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FlightPlanActivityKVCfg.class);
		RewardItem.Builder rollConsumeItem = RewardHelper.toRewardItem(cfg.getSinglePrice());
		
		int diceCount = this.getDataGeter().getItemNum(playerId, cfg.getFlightItemId());
		int totalCount = rollTimes * (int)rollConsumeItem.getItemCount();
		int needBuyCount = totalCount - diceCount;
		if (needBuyCount > 0) {
			RewardItem.Builder buyItemConsume = RewardHelper.toRewardItem(cfg.getItemPrice());
			buyItemConsume.setItemCount(buyItemConsume.getItemCount() * needBuyCount);
			consumeItemList.add(buyItemConsume);
			if (diceCount > 0) {
				rollConsumeItem.setItemCount(diceCount);
				consumeItemList.add(rollConsumeItem);
			}
		} else {
			rollConsumeItem.setItemCount(totalCount);
			consumeItemList.add(rollConsumeItem);
		}
		
		return needBuyCount;
	}
	
	/**
	 * 单次飞行
	 * 
	 * @param entity
	 * @param cfg
	 * @param count
	 * @return
	 */
	private HawkTuple3<Integer, Integer, Integer> rollDiceOnce(FlightPlanEntity entity, List<RewardItem.Builder> rewardList) {
		int count = HawkConfigManager.getInstance().getConfigSize(FlightPlanCellAwardCfg.class);
		FlightPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FlightPlanActivityKVCfg.class);
		
		int point = HawkRand.randInt(1, cfg.getStepOnceMax());
		// 最终的目标格子 = 起始位置 + 随机出的步数
		int endCell = entity.getCellId() + point;
		int cellId = endCell % count;
		entity.setCellId(cellId);
		FlightPlanCellAwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByIndex(FlightPlanCellAwardCfg.class, cellId);
		if (awardCfg == null || awardCfg.getEvent() == CELL_AWARD_NONE) {
			HawkLog.errPrintln("FlightPlanActivity cell move failed, config error, playerId: {}, cellId: {}", entity.getPlayerId(), cellId);
			return new HawkTuple3<Integer, Integer, Integer>(cellId, point, 0);
		}
		
		int awardRatio = 1;
		List<RewardItem.Builder> rewardOnce = RewardHelper.toRewardItemList(awardCfg.getRewards());
		if (awardCfg.getEvent() == CELL_AWARD_MULTI) {
			int ratio = cfg.getCellAwardRatio();
			awardRatio = ratio;
			rewardOnce.stream().forEach(e -> e.setItemCount(e.getItemCount() * ratio));
		}
		
		rewardList.addAll(rewardOnce);
		
		return new HawkTuple3<Integer, Integer, Integer>(cellId, point, awardRatio);
	}
	
	/**
	 * 摇色子返还信息同步
	 * 
	 * @param playerId
	 * @param cellId
	 * @param point
	 * @param ratio
	 */
	private void rollDiceResponse(String playerId, RollDiceType type, int cellId, int point, int ratio, int fragmentCount, List<RewardItem.Builder> rewardList) {
		CellMoveResp.Builder resp = CellMoveResp.newBuilder();
		resp.setType(type);
		resp.setCellId(cellId + 1);
		resp.setFragment(fragmentCount);
		rewardList.forEach(e -> resp.addRewards(e));
		if (type == RollDiceType.ONE_TIMES) {
			resp.setPoint(point);
			resp.setRatio(ratio);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.FLIGHT_PLAN_CELL_MOVE_RESP_VALUE, resp));
	}
	
	/**
	 * 物品兑换请求
	 * 
	 * @param playerId
	 * @param goodsId  物品ID
	 * @param count    兑换个数
	 * 
	 * @return
	 */
	public int onExchange(String playerId, int goodsId, int count, int protoType) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<FlightPlanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		// 参数错误
		if (count <= 0) {
			HawkLog.errPrintln("FlightPlanActivity exchange failed, count error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;	
		}
		
		FlightPlanShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(FlightPlanShopCfg.class, goodsId);
		if (shopCfg == null) {
			HawkLog.errPrintln("FlightPlanActivity exchange failed, shop config error, playerId: {}, goodsId: {}", playerId, goodsId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		FlightPlanEntity entity = opEntity.get();
		int afterCount = entity.getExchangeTimes(goodsId) + count; 
		// 总兑换次数大于上限了
		if (afterCount > shopCfg.getTotal()) {
			HawkLog.errPrintln("FlightPlanActivity exchange failed, exchange total uplimit, playerId: {}, goodsId: {}, count: {}, afterCount: {}", 
					playerId, goodsId, count, afterCount);
			return Status.Error.EXCHANGE_TIMES_UPLIMIT_VALUE;
		}
		
		// 兑换消耗
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemList(shopCfg.getPrice());
		if (count > 1) {
			consumeItems.stream().forEach(e -> e.setItemCount(e.getItemCount() * count));
		}
		
		boolean success = getDataGeter().consumeItems(playerId, consumeItems, protoType, Action.FLIGHT_PLAN_EXCHANGE_CONSUME);
		if (!success) {
			HawkLog.errPrintln("FlightPlanActivity exchange failed, comsume not enough, playerId: {}, goodsId: {}, count: {}", playerId, goodsId, count);
			return 0;
		}
			
		// 更新商品的兑换次数
		entity.getGoodsExchangeMap().put(goodsId, afterCount);
		entity.notifyUpdate();
		
		// 兑换获得
		List<RewardItem.Builder> award = RewardHelper.toRewardItemList(shopCfg.getGoods());
		if (count > 1) {
			award.stream().forEach(e -> e.setItemCount(e.getItemCount() * count));
		}
		
		ActivityReward reward = new ActivityReward(award, Action.FLIGHT_PLAN_EXCHANGE_AWARD);
		reward.setAlert(true);
		postReward(playerId, reward, false);
		
		syncCellInfo(playerId);
		
		getDataGeter().logFlightPlanExchange(playerId, shopCfg.getId(), shopCfg.getAwardItemId(), count, shopCfg.getConsumeCount() * count, afterCount);
		HawkLog.logPrintln("FlightPlanActivity exchange success, playerId: {}, goodsId: {}, count: {}, afterCount: {}", playerId, goodsId, count, afterCount);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
