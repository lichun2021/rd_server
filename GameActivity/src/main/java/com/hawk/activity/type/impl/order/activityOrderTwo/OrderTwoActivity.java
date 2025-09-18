package com.hawk.activity.type.impl.order.activityOrderTwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.OrderAuthBuyEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.order.IOrderActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoActivityKVCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoAuthorityCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoLevelCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoShopCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoTaskCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.game.protocol.Activity.GetHistoryOrderInfoResp;
import com.hawk.game.protocol.Activity.GetOrderPageInfoResp;
import com.hawk.game.protocol.Activity.OrderBaseInfo;
import com.hawk.game.protocol.Activity.OrderItemPB;
import com.hawk.game.protocol.Activity.OrderItemsInfoSync;
import com.hawk.game.protocol.Activity.OrderRewardAchieveAllResp;
import com.hawk.game.protocol.Activity.OrderShopItemPB;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class OrderTwoActivity extends ActivityBase implements IOrderActivity{

	public OrderTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ORDER_TWO_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OrderTwoActivity activity = new OrderTwoActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<OrderTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from OrderTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			OrderTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		OrderTwoEntity entity = new OrderTwoEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_YURI_ACHIEVE, () -> {
				Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				chechAndInitOrder(playerId, opEntity.get());
			});
		}
	}
	
	
	/**
	 * 检测/初始化战令任务
	 * @param playerId
	 * @param OrderTwoEntity
	 */
	private void chechAndInitOrder(String playerId, OrderTwoEntity dataEntity) {
		if (!isOpening(playerId)) {
			return;
		}
		int weekCycle = dataEntity.getWeekCycle();
		if(weekCycle == 0){
			addExp(dataEntity, 0, EXP_REASON_INIT, 0);
		}
		int currCycle = getBetweenWeeks();
		List<OrderItem> itemList = dataEntity.getOrderList();
		boolean needSync = false;
		// 周数未发生变更
		if (weekCycle == currCycle) {
			// 初始化当前周期的任务
			if (itemList.isEmpty()) {
				List<OrderItem> orderList = initWeekOrder(currCycle);
				dataEntity.resetOrderList(orderList);
				dataEntity.setWeekCycle(currCycle);
				needSync = true;
			} else {
				return;
			}
		} else if (currCycle > weekCycle) {
			List<OrderItem> historyList = dataEntity.getHistoryOrderList();
			// 当前的战令任务存入历史任务列表
			historyList.addAll(itemList);
			for (int i = 1; i < currCycle - weekCycle; i++) {
				List<OrderItem> oldList = initWeekOrder(weekCycle + i);
				historyList.addAll(oldList);
			}

			dataEntity.setWeekCycle(currCycle);
			// 初始化当前周期的任务
			List<OrderItem> orderList = initWeekOrder(currCycle);
			dataEntity.resetOrderList(orderList);
			needSync = true;
		}
		
		if(HawkTime.getMillisecond() > dataEntity.getWeekTime()){
			dataEntity.setWeekTime(nextWeekTime());
			dataEntity.setWeekNumber(0);
			needSync=true;
		}

		// 同步活动界面信息
		if (needSync) {
			GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
			pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
		}

	}
	
	/**
	 * 构建当前活动界面信息PB
	 * @param playerId
	 */
	private GetOrderPageInfoResp.Builder genPageInfoBuilder(String playerId) {
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		OrderTwoEntity dataEntity = opEntity.get();

		GetOrderPageInfoResp.Builder builder = GetOrderPageInfoResp.newBuilder();
		builder.setBaseInfo(genBaseInfoBuilder(playerId));
		builder.addAllItem(genOrderItemList(dataEntity.getOrderList()));
		Map<Integer, Integer> buyMsg = dataEntity.getBuyMsg();
		for(Entry<Integer, Integer> entry : buyMsg.entrySet()){
			OrderShopItemPB.Builder itemInfo = OrderShopItemPB.newBuilder();
			itemInfo.setId(entry.getKey());
			itemInfo.setCount(entry.getValue());
			builder.addShopItem(itemInfo);
		}
		return builder;
	}
	
	/**
	 * 构建战令列表PB
	 * @param orderList
	 * @return
	 */
	private List<OrderItemPB> genOrderItemList(List<OrderItem> orderList) {
		List<OrderItemPB> list = new ArrayList<>();
		for(OrderItem item : orderList){
			list.add(item.build());
		}
		return list;
	}

	/**
	 * 构建战令活动基础信息PB
	 * @param playerId
	 * @return
	 */
	private OrderBaseInfo.Builder genBaseInfoBuilder(String playerId) {
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		OrderTwoEntity dataEntity = opEntity.get();
		
		OrderBaseInfo.Builder builder = OrderBaseInfo.newBuilder();
		builder.setCurrCycle(dataEntity.getWeekCycle());
		builder.setLevel(dataEntity.getLevel());
		builder.setExp(calcShowExp(dataEntity));
		builder.setAuthorityId(getOrder(dataEntity.getAuthorityId()));
		builder.setWeekNumber(dataEntity.getWeekNumber());
		
		List<Integer> rewardNormalLevel = new ArrayList<>();
		List<Integer> rewardAdvanceLevel = new ArrayList<>();
		rewardNormalLevel.addAll(dataEntity.getRewardNormalLevelMap().keySet());
		rewardAdvanceLevel.addAll(dataEntity.getRewardAdvanceLevelMap().keySet());
		builder.addAllRewardNormalLevel(rewardNormalLevel);
		builder.addAllRewardAdvancedLevel(rewardAdvanceLevel);
		return builder;
	}
	
	/**
	 * 获取战令等阶
	 * @param authorityId
	 * @return
	 */
	private int getOrder(int authorityId) {
		if (authorityId == 0) {
			return 0;
		}
		OrderTwoAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, authorityId);
		if (cfg == null) {
			return 0;
		}
		return cfg.getOrder();

	}
	
	/**
	 * 计算展示经验
	 * @param dataEntity
	 * @return
	 */
	private int calcShowExp(OrderTwoEntity dataEntity) {
		int showExp = 0;
		for (int lvl = 1; lvl <= dataEntity.getLevel(); lvl++) {
			OrderTwoLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, lvl);
			if (dataEntity.getExp() - cfg.getLevelUpExp() >= 0) {
				showExp = dataEntity.getExp() - cfg.getLevelUpExp();
			} else {
				break;
			}

		}
		return showExp;
	}

	/**
	 * 根据周数初始化战令任务列表
	 * @param currCycle
	 * @return
	 */
	private List<OrderItem> initWeekOrder(int currCycle) {
		List<OrderItem> list = new CopyOnWriteArrayList<>();
		ConfigIterator<OrderTwoTaskCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderTwoTaskCfg.class);
		for(OrderTwoTaskCfg cfg : its){
			if(cfg.getCycle() != currCycle){
				continue;
			}
			OrderItem item = new OrderItem();
			item.setOrderId(cfg.getId());
			list.add(item);
		}
		return list;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderTwoEntity dataEntity = opEntity.get();
		chechAndInitOrder(playerId, dataEntity);
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
	}

	/**
	 * 获取本期活动的当前周数
	 * @return
	 */
	public int getBetweenWeeks() {
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long baseTime = ActivityConst.BASE_MONDAY_TIME;
		long weekTime = ActivityConst.WEEK_MILLI_SECONDS;
		long startWeek = (startTime - baseTime) / weekTime;
		long currWeek = (HawkTime.getMillisecond() - baseTime) / weekTime;
		return (int) Math.max(0, currWeek - startWeek + 1);
	}
	
	
	@Subscribe
	public void onEvent(OrderEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		long now = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		// 已过活动开启时间,不再接受活动积分时间,积分不再增长
		if (now >= endTime) {
			return;
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderTwoEntity dataEntity = opEntity.get();

		// 阶段检测
		chechAndInitOrder(playerId, dataEntity);

		List<OrderTaskParser<?>> parsers = OrderTaskContext.getParser(event.getClass());
		if (parsers == null) {
			logger.info("OrderTaskParser not found, eventClass: {}", event.getClass().getName());
			return;
		}

		List<OrderItem> orderList = dataEntity.getOrderList();
		boolean update = false;
		List<OrderItem> changeList = new ArrayList<>();
		for (OrderTaskParser<?> parser : parsers) {
			for (OrderItem orderItem : orderList) {
				OrderTwoTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoTaskCfg.class, orderItem.getOrderId());
				if (cfg == null) {
					continue;
				}

				// 判定任务类型是否一致
				if (!cfg.getTaskType().equals(parser.getTaskType())) {
					continue;
				}

				// 完全完成的任务不做处理
				if (parser.finished(orderItem, cfg)) {
					continue;
				}
				if (parser.onEventUpdate(dataEntity, cfg, orderItem, event.convert())) {
					changeList.add(orderItem);
					update = true;
				}
			}
		}

		if (update) {
			dataEntity.notifyUpdate();
			OrderItemsInfoSync.Builder builder = OrderItemsInfoSync.newBuilder();
			builder.addAllItem(genOrderItemList(orderList));
			pushToPlayer(playerId, HP.code.ORDER_TWO_ITEM_INFO_CHANGE_SYNC_VALUE, builder);
			pushToPlayer(playerId, HP.code.ORDER_TWO_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		}
	}
	
	
	@Subscribe
	public void onAuthBuyEvent(OrderAuthBuyEvent event) {
		String playerId = event.getPlayerId();
		int id = Integer.valueOf(event.getPayGiftId());
		OrderTwoAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, id);
		if (cfg == null) {
			return;
		}

		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		OrderTwoEntity dataEntity = opEntity.get();
		int currAuth = dataEntity.getAuthorityId();
		boolean isSupply = cfg.isSupply();
		if (currAuth != 0) {
			OrderTwoAuthorityCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, currAuth);
			// 配置异常
			if (currCfg == null) {
				HawkLog.logPrintln("OrderActivity buy auth failed ,cfg error, playerId:{}, currAuthorityId: {}, payGiftId: {}", dataEntity.getPlayerId(),
						dataEntity.getAuthorityId(), id);
				return;
			}
			// 当前已是最高等阶
			else if (currCfg.getOrder() == 2) {
				HawkLog.logPrintln("OrderActivity buy auth failed ,higest order, playerId:{}, authorityId: {}, payGiftId: {}", playerId, dataEntity.getAuthorityId(), id);
				return;
			}
			// 购买的不是差价直购
			else if (!isSupply) {
				HawkLog.logPrintln("OrderActivity buy auth failed ,repeated buy, playerId:{}, authorityId: {}, payGiftId: {}", playerId, dataEntity.getAuthorityId(), id);
				return;
			}
		}
		dataEntity.setAuthorityId(id);
		if(!this.inVersion20240905Time()){
			//如果没在此时间之前，还是采取邮件发奖励的形式，在此时间之后就手动领奖了
			// 首次进阶,补发进价奖励
			if (!isSupply) {
				int lvl = dataEntity.getLevel();

				List<RewardItem.Builder> rewardList = new ArrayList<>();
				for (int i = 1; i <= lvl; i++) {
					OrderTwoLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, i);
					rewardList.addAll(lvlCfg.getAdvRewardList());
				}

				// 发放进阶奖励
				if (!rewardList.isEmpty()) {
					Object[] content;
					content = new Object[1];
					content[0] = lvl;
					sendMailToPlayer(dataEntity.getPlayerId(), MailId.ORDER_ACTIVITY_ADVANCE_REWARD, null, null, content, rewardList);
				}
			}
		}
		
		// 增加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_AUTH, id);
		
		// 流水记录
		getDataGeter().logBuyOrderAuth(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId());
		HawkLog.logPrintln("OrderActivity buy auth success, playerId:{}, authorityId: {}, addExp: {}, level: {}", playerId, id, cfg.getExp(), dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_TWO_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
	}
	
	/**
	 * 增加战令经验
	 * @param dataEntity
	 * @param addExp
	 */
	@Override
	public void addExp(IOrderDateEntity orderDataEntity, int addExp, int reason, int reasonId) {
		if(!(orderDataEntity instanceof OrderTwoEntity)){
			return;
		}
		OrderTwoEntity dataEntity = (OrderTwoEntity) orderDataEntity;
		String playerId = dataEntity.getPlayerId();
		if (addExp < 0) {
			return;
		}
		int oldExp = dataEntity.getExp();
		int oldLvl = dataEntity.getLevel();

		int newExp = oldExp + addExp;
		int newLvl = calcLevel(newExp);
		if (newLvl > oldLvl) {
			if(!this.inVersion20240905Time()){
				//如果没在此时间之前，还是采取邮件发奖励的形式，在此时间之后就手动领奖了
				for (int lvl = oldLvl + 1; lvl <= newLvl; lvl++) {
					OrderTwoLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, lvl);
					List<RewardItem.Builder> rewardList = cfg.getNormalRewardList();
					if (dataEntity.isAdvance()) {
						rewardList.addAll(cfg.getAdvRewardList());
					}
					if (!rewardList.isEmpty()) {
						// 发送等级奖励邮件
						Object[] content;
						content = new Object[2];
						content[0] = lvl;
						content[1] = dataEntity.isAdvance();
						sendMailToPlayer(playerId, MailId.ORDER_ACTIVITY_LVL_REWARD, null, null, content, rewardList);
					}
				}
			}
			dataEntity.setLevel(newLvl);
		}
		dataEntity.setExp(newExp);
		// 流水记录
		getDataGeter().logOrderExpChange(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), addExp, dataEntity.getExp(), dataEntity.getLevel(), reason, reasonId);
		HawkLog.logPrintln("OrderActivity  expAdd, playerId: {}, expBef: {}, expAft: {}, lvlBef: {}, lvlAft: {}, reason: {}, reasonId: {}", playerId, oldExp, newExp, oldLvl, newLvl, reason, reasonId);
	}
	
	
	@Override
	public void logOrderFinishId(IOrderDateEntity orderDataEntity, IOrderTaskCfg cfg, OrderItem orderItem, int addTimes) {
		if(!(orderDataEntity instanceof OrderTwoEntity)){
			return;
		}
		OrderTwoEntity dataEntity = (OrderTwoEntity) orderDataEntity;
		getDataGeter().logOrderFinishId(dataEntity.getPlayerId(), dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId(),addTimes,orderItem.getFinishTimes());
	}
	/**
	 * 计算等级
	 * @param exp
	 * @return
	 */
	private int calcLevel(int exp) {
		int level = 0;
		ConfigIterator<OrderTwoLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderTwoLevelCfg.class);
		for (OrderTwoLevelCfg cfg : its) {
			if (exp < cfg.getLevelUpExp()) {
				break;
			} else {
				level = cfg.getLevel();
			}
		}
		return level;
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderTwoEntity dataEntity = opEntity.get();
		chechAndInitOrder(playerId, dataEntity);
	}
	

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	/**
	 * 一周时间
	 */
	private final long WEEK_BASE = 1 * 60 * 60 * 24 * 7 * 1000; 
	private long nextWeekTime(){
		//下周一0点时间
		return HawkTime.getFirstDayOfCurWeek().getTime() + WEEK_BASE;
	}
	
	/**
	 * 获取历史任务信息
	 * @param playerId
	 * @param cycleId
	 */
	public void getGetHistoryInfo(String playerId, int cycleId) {
		if (!isOpening(playerId)) {
			sendErrorAndBreak(playerId, HP.code.ORDER_TWO_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}

		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			sendErrorAndBreak(playerId, HP.code.ORDER_TWO_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		OrderTwoEntity dataEntity = opEntity.get();
		List<OrderItem> itemList = new ArrayList<>();
		int currCycle = dataEntity.getWeekCycle();
		if (currCycle < cycleId) {
			sendErrorAndBreak(playerId, HP.code.ORDER_TWO_GET_HISTORY_INFO_C_VALUE, Status.Error.ORDER_WEEK_ERROR_VALUE);
			return;
		} else if (currCycle == cycleId) {
			itemList = dataEntity.getOrderList();
		} else {
			for (OrderItem item : dataEntity.getHistoryOrderList()) {
				OrderTwoTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoTaskCfg.class, item.getOrderId());
				if (cfg == null || cfg.getCycle() != cycleId) {
					continue;
				}
				itemList.add(item);
			}
		}
		GetHistoryOrderInfoResp.Builder builder = GetHistoryOrderInfoResp.newBuilder();
		builder.setCycleId(cycleId);
		builder.addAllItem(genOrderItemList(itemList));
		pushToPlayer(playerId, HP.code.ORDER_TWO_GET_HISTORY_INFO_S_VALUE, builder);
	}
	
	
	public Result<?> buyOrderLevel(String playerId, int level){
		if(this.inVersion20240905Time()){
			//购买到多少级
			return this.buyAuthLvlMultiple(playerId, level);
		}else{
			//只购买一级
			return this.buyAuthLvl(playerId, level);
		}
		
	}

	public Result<?> buyAuthLvl(String playerId, int currLvl) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderTwoEntity dataEntity = opEntity.get();
		OrderTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		if(dataEntity.getWeekNumber() >= kvCfg.getUnlockTime()){
			return Result.fail(Status.Error.ORDER_WEEK_ORDER_VALUE);
		}
		
		if (dataEntity.getLevel() != currLvl) {
			return Result.fail(Status.Error.ORDER_LVL_CHANGED_VALUE);
		}

		OrderTwoLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, currLvl + 1);
		if (lvlCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		int addExp = lvlCfg.getLevelUpExp() - dataEntity.getExp();

		float unlockRate = (float) (kvCfg.getUnlockGold() / 10000f);
		int needGold = (int) (addExp * unlockRate);
		needGold = Math.max(1, needGold);
		Reward.RewardItem.Builder costBuilder = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, kvCfg.getUnlockCostId(), needGold);
		boolean flag = getDataGeter().cost(playerId, Arrays.asList(costBuilder), 1, Action.ORDER_TWO_LVL_BUY, true);
		if (!flag) {
			return Result.fail(Status.Error.DIAMONDS_NOT_ENOUGH_VALUE);
		}

		// 加经验
		addExp(dataEntity, addExp, EXP_REASON_BUY, currLvl);
		dataEntity.addWeekNumber();
		dataEntity.notifyUpdate();

		// 流水记录
		getDataGeter().logBuyOrderExp(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), currLvl, addExp);
		HawkLog.logPrintln("OrderActivity buyOrderExp success, playerId: {}, currLvl: {}, addExp: {}, level: {}", playerId, currLvl, addExp, dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_TWO_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		return Result.success();
	}
	
	
	public Result<?> buyAuthLvlMultiple(String playerId, int toLevel) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderTwoEntity dataEntity = opEntity.get();
		OrderTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		if (dataEntity.getLevel() >= toLevel) {
			return Result.fail(Status.Error.ORDER_LVL_CHANGED_VALUE);
		}
		int unlockLevel = toLevel - dataEntity.getLevel();
		if(dataEntity.getWeekNumber() + unlockLevel > kvCfg.getUnlockTime()){
			return Result.fail(Status.Error.ORDER_WEEK_ORDER_VALUE);
		}
		int curLevel = dataEntity.getLevel();
		for(int i= curLevel+1; i<= toLevel; i++){
			OrderTwoLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, i);
			if (lvlCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		OrderTwoLevelCfg toLvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, toLevel);
		int addExp = toLvlCfg.getLevelUpExp() - dataEntity.getExp();
		float unlockRate = (float) (kvCfg.getUnlockGold() / 10000f);
		int needGold = (int) (addExp * unlockRate);
		needGold = Math.max(1, needGold);
		Reward.RewardItem.Builder costBuilder = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, kvCfg.getUnlockCostId(), needGold);
		boolean flag = getDataGeter().cost(playerId, Arrays.asList(costBuilder), 1, Action.ORDER_TWO_LVL_BUY, true);
		if (!flag) {
			return Result.fail(Status.Error.DIAMONDS_NOT_ENOUGH_VALUE);
		}
		// 加经验
		addExp(dataEntity, addExp, EXP_REASON_BUY, curLevel);
		//增加解锁记录
		int weekNum = dataEntity.getWeekNumber() + unlockLevel;
		dataEntity.setWeekNumber(weekNum);
		dataEntity.notifyUpdate();
		// 流水记录
		getDataGeter().logBuyOrderExp(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), curLevel, addExp);
		HawkLog.logPrintln("OrderActivity buyOrderExp success, playerId: {}, currLvl: {}, addExp: {}, level: {}", playerId, curLevel, addExp, dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_TWO_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		return Result.success();
	}

	public Result<?> orderShopBuy(String playerId, OrderShopItemPB itemInfo) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderTwoEntity dataEntity = opEntity.get();
		int shopId = itemInfo.getId();
		int buyCnt = itemInfo.getCount();
		OrderTwoShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoShopCfg.class, shopId);
		if (cfg == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		if (dataEntity.getLevel() < cfg.getLevel()) {
			return Result.fail(Status.Error.ORDER_LVL_NOT_ENOUGH_VALUE);
		}
		int order = getOrder(dataEntity.getAuthorityId());
		if (cfg.getOrder() > order) {
			return Result.fail(Status.Error.ORDER_NOT_ENOUGH_VALUE);
		}
		int boughtCnt = dataEntity.getBaughtCnt(shopId);
		if (boughtCnt + buyCnt > cfg.getNum()) {
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}

		List<RewardItem.Builder> itemList = cfg.getCostList();
		for (RewardItem.Builder item : itemList) {
			item.setItemCount(item.getItemCount() * buyCnt);
		}
		boolean flag = this.getDataGeter().cost(playerId, cfg.getCostList(), buyCnt, Action.ORDER_TWO_SHOP_BUY, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		dataEntity.buyShopItem(shopId, buyCnt);
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getItemList(), buyCnt, Action.ORDER_TWO_SHOP_BUY, true, RewardOrginType.ACTIVITY_REWARD);
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
		return Result.success();
	}
	
	
	/**
	 * 领普通奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> orderRewardNormal(String playerId,List<Integer> rewardLevels) {
		if(!this.inVersion20240905Time()){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if(rewardLevels.isEmpty()){
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);  
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (rewardLevels.size() != 1) {
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		int awardLevel = rewardLevels.get(0);
		OrderTwoEntity dataEntity = opEntity.get();
		int level = dataEntity.getLevel();
		Map<Integer, Long> rewardLevelMap = dataEntity.getRewardNormalLevelMap();
		if(awardLevel > level){
			//是否超过了当前等级
			return Result.fail(Status.SysError.DATA_ERROR_VALUE); 
		}
		if(rewardLevelMap.containsKey(awardLevel)){
			//此等级奖励已经领过
			return Result.fail(Status.SysError.DATA_ERROR_VALUE); 
		}
		OrderTwoLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, awardLevel);
		List<RewardItem.Builder> rewardList = cfg.getNormalRewardList();
		//添加领奖记录
		long rewardTime = HawkTime.getMillisecond();
		dataEntity.addRewardNormalLevel(awardLevel, rewardTime);
		// 添加物品
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.ORDER_TWO_REWARD_NORMAL, true, RewardOrginType.ACTIVITY_REWARD);
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
		HawkLog.logPrintln("OrderActivity  orderRewardNormal, playerId: {}, formLevel: {}, curLevel: {}", playerId, awardLevel, level);
		return Result.success();
	}
	
	
	/**
	 * 领进阶奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> orderRewardAdvance(String playerId,List<Integer> rewardLevels) {
		if(!this.inVersion20240905Time()){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if(rewardLevels.isEmpty()){
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);  
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderTwoEntity dataEntity = opEntity.get();
		if (!dataEntity.isAdvance()) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		int authorityId = dataEntity.getAuthorityId();
		OrderTwoAuthorityCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, authorityId);
		if(Objects.isNull(currCfg)){
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);   
		}
		if (rewardLevels.size() != 1) {
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		int awardLevel = rewardLevels.get(0);
		int level = dataEntity.getLevel();
		Map<Integer, Long> rewardLevelMap = dataEntity.getRewardAdvanceLevelMap();
		if(awardLevel > level){
			//是否超过了当前等级
			return Result.fail(Status.SysError.DATA_ERROR_VALUE); 
		}
		if(rewardLevelMap.containsKey(awardLevel)){
			//此等级奖励已经领过
			return Result.fail(Status.SysError.DATA_ERROR_VALUE); 
		}
		
		OrderTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		OrderTwoLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoLevelCfg.class, awardLevel);
		List<RewardItem.Builder> rewardList = cfg.getAdvRewardList();
		//只有高等级的特权才会暴击
		int randomNum = HawkRand.randInt(1, 100);
		if(currCfg.getOrder() == 2 && 
				randomNum <= kvCfg.getDoubleProbability()){
			for(RewardItem.Builder rbuilder : rewardList){
				long count = rbuilder.getItemCount() * 2;
				rbuilder.setItemCount(count);
				HawkLog.logPrintln("OrderActivity  orderRewardAdvance, rewardDouble:playerId:{}, rewardLevel: {}, curLevel:{},itemId:{},itemNum:{}", 
						playerId, awardLevel, level,rbuilder.getItemId(),rbuilder.getItemCount());
			}
			
		}
		//设置领奖等级
		long rewardTime = HawkTime.getMillisecond();
		dataEntity.addRewardAdvanceLevel(awardLevel, rewardTime);
		//发奖励
		if(currCfg.getOrder() == 2 && randomNum <= kvCfg.getDoubleProbability()){
			this.getDataGeter().takeReward(playerId, rewardList, 1, Action.ORDER_TWO_REWARD_ADVANCE_DOUBLE, true, RewardOrginType.ORDER_ACTIVITY_DOUBLE_REWARD);
		}else{
			this.getDataGeter().takeReward(playerId, rewardList, 1, Action.ORDER_TWO_REWARD_ADVANCE, true, RewardOrginType.ACTIVITY_REWARD);
		}
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
		HawkLog.logPrintln("OrderActivity  orderRewardAdvance, playerId: {}, rewardLevel: {}, curLevel: {}", playerId, awardLevel, level);
		return Result.success();
	}
	
	
	/**
	 * 一键领奖
	 * 
	 * 这段是后加的代码  也就不动上面原来的代码了，直接复制上面的逻辑
	 */
	public Result<?> orderRewardAll(String playerId){
		if(!this.inVersion20240905Time()){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OrderTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderTwoEntity dataEntity = opEntity.get();
		int level = dataEntity.getLevel();
		int authorityId = dataEntity.getAuthorityId();
		long rewardTime = HawkTime.getMillisecond();
		OrderTwoAuthorityCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, authorityId);
		OrderTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		//正常奖励
		List<RewardItem.Builder> onceRewardList = new ArrayList<>();
		//暴击双倍奖励
		List<RewardItem.Builder> doubleRewardList = new ArrayList<>();
		List<OrderTwoLevelCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(OrderTwoLevelCfg.class).toList();
		for(OrderTwoLevelCfg cfg : cfgs){
			int awardLevel = cfg.getLevel();
			if(awardLevel > level){
				continue;
			}
			//普通奖励
			Map<Integer, Long> normalRewardLevelMap = dataEntity.getRewardNormalLevelMap();
			if(!normalRewardLevelMap.containsKey(awardLevel)){
				dataEntity.addRewardNormalLevel(awardLevel, rewardTime);
				List<RewardItem.Builder> rewardList = cfg.getNormalRewardList();
				onceRewardList.addAll(rewardList);
				HawkLog.logPrintln("OrderActivity  orderRewardAll orderRewardNormal, playerId: {}, formLevel: {}, curLevel: {}", playerId, awardLevel, level);
			}
			//高级奖励
			Map<Integer, Long> rewardLevelMap = dataEntity.getRewardAdvanceLevelMap();
			if(!rewardLevelMap.containsKey(awardLevel) && Objects.nonNull(currCfg)){
				dataEntity.addRewardAdvanceLevel(awardLevel, rewardTime);
				List<RewardItem.Builder> rewardList = cfg.getAdvRewardList();
				//只有高等级的特权才会暴击
				int randomNum = HawkRand.randInt(1, 100);
				if(currCfg.getOrder() == 2 && randomNum <= kvCfg.getDoubleProbability()){
					for(RewardItem.Builder rbuilder : rewardList){
						long count = rbuilder.getItemCount() * 2;
						rbuilder.setItemCount(count);
						HawkLog.logPrintln("OrderActivity orderRewardAll orderRewardAdvance, rewardDouble:playerId:{}, rewardLevel: {}, curLevel:{},itemId:{},itemNum:{}", 
								playerId, awardLevel, level,rbuilder.getItemId(),rbuilder.getItemCount());
					}
					doubleRewardList.addAll(rewardList);
				}else{
					onceRewardList.addAll(rewardList);
				}
				
			}
		}
		//发奖
		if(!onceRewardList.isEmpty()){
			this.getDataGeter().takeReward(playerId, onceRewardList, 1, Action.ORDER_TWO_REWARD_ADVANCE, false, RewardOrginType.ACTIVITY_REWARD);
		}
		if(!doubleRewardList.isEmpty()){
			this.getDataGeter().takeReward(playerId, doubleRewardList, 1, Action.ORDER_TWO_REWARD_ADVANCE_DOUBLE, false, RewardOrginType.ORDER_ACTIVITY_DOUBLE_REWARD);
		}
		//同步消息
		OrderRewardAchieveAllResp.Builder respBuilder = OrderRewardAchieveAllResp.newBuilder();
		for(RewardItem.Builder ib : onceRewardList){
			respBuilder.addNormalRewards(ib);
		}
		for(RewardItem.Builder ib : doubleRewardList){
			respBuilder.addDoubleRewards(ib);
		}
		pushToPlayer(playerId, HP.code2.ORDER_TWO_REWARD_ACHIEVE_ALL_S_VALUE, respBuilder);
		// 添加物品
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_TWO_PAGE_INFO_SYNC_VALUE, builder);
		return Result.success();
		
	}
	
	/**
	 * 是否已经开始生效
	 * @return
	 */
	public boolean inVersion20240905Time(){
		OrderTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		long workTime = HawkTime.parseTime(kvCfg.getVersion20240905Time());
		long curTime = HawkTime.getMillisecond();
		if(curTime > workTime){
			return true;
		}
		return false;
	}
}
