package com.hawk.activity.type.impl.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
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
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.cfg.OrderAuthorityCfg;
import com.hawk.activity.type.impl.order.cfg.OrderExpShopCfg;
import com.hawk.activity.type.impl.order.cfg.OrderLevelCfg;
import com.hawk.activity.type.impl.order.cfg.OrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.ActivityOrderEntity;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.game.protocol.Activity.GetHistoryOrderInfoResp;
import com.hawk.game.protocol.Activity.GetOrderPageInfoResp;
import com.hawk.game.protocol.Activity.OrderBaseInfo;
import com.hawk.game.protocol.Activity.OrderItemPB;
import com.hawk.game.protocol.Activity.OrderItemsInfoSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class OrderActivity extends ActivityBase implements IOrderActivity{
	/**
	 * 经验变化来源
	 */
//	public static final int EXP_REASON_INIT = 0;
//	public static final int EXP_REASON_TASK = 1;
//	public static final int EXP_REASON_AUTH = 2;
//	public static final int EXP_REASON_BUY = 3;

	public OrderActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ORDER_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OrderActivity activity = new OrderActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityOrderEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityOrderEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityOrderEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityOrderEntity entity = new ActivityOrderEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_YURI_ACHIEVE, () -> {
				Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
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
	 * @param activityOrderEntity
	 */
	private void chechAndInitOrder(String playerId, ActivityOrderEntity dataEntity) {
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
			dataEntity.resetExpBuyInfo();
			needSync = true;
		}

		// 同步活动界面信息
		if (needSync) {
			GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
			pushToPlayer(playerId, HP.code.ORDER_PAGE_INFO_SYNC_VALUE, builder);
		}

	}
	
	/**
	 * 构建当前活动界面信息PB
	 * @param playerId
	 */
	private GetOrderPageInfoResp.Builder genPageInfoBuilder(String playerId) {
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		ActivityOrderEntity dataEntity = opEntity.get();

		GetOrderPageInfoResp.Builder builder = GetOrderPageInfoResp.newBuilder();
		builder.setBaseInfo(genBaseInfoBuilder(playerId));
		builder.addAllItem(genOrderItemList(dataEntity.getOrderList()));
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
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		ActivityOrderEntity dataEntity = opEntity.get();
		OrderBaseInfo.Builder builder = OrderBaseInfo.newBuilder();
		builder.setCurrCycle(dataEntity.getWeekCycle());
		builder.setLevel(dataEntity.getLevel());
		builder.setExp(calcShowExp(dataEntity));
		builder.setAuthorityId(dataEntity.getAuthorityId());
		List<Integer> buyList = dataEntity.getExpBuyList();
		if (!buyList.isEmpty()) {
			builder.addAllBuyExpId(buyList);
		}
		return builder;
	}
	
	/**
	 * 计算展示经验
	 * @param dataEntity
	 * @return
	 */
	private int calcShowExp(ActivityOrderEntity dataEntity) {
		int showExp = 0;
		for (int lvl = 1; lvl <= dataEntity.getLevel(); lvl++) {
			OrderLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderLevelCfg.class, lvl);
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
		ConfigIterator<OrderTaskCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderTaskCfg.class);
		for(OrderTaskCfg cfg : its){
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
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityOrderEntity dataEntity = opEntity.get();
		chechAndInitOrder(playerId, dataEntity);
		GetOrderPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_PAGE_INFO_SYNC_VALUE, builder);
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
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityOrderEntity dataEntity = opEntity.get();

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
				OrderTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTaskCfg.class, orderItem.getOrderId());
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
			pushToPlayer(playerId, HP.code.ORDER_ITEM_INFO_CHANGE_SYNC_VALUE, builder);
			pushToPlayer(playerId, HP.code.ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		}
	}
	
	
	@Subscribe
	public void onAuthBuyEvent(OrderAuthBuyEvent event) {
		String playerId = event.getPlayerId();
		int id = Integer.valueOf(event.getPayGiftId());
		OrderAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderAuthorityCfg.class, id);
		if (cfg == null) {
			return;
		}

		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		ActivityOrderEntity dataEntity = opEntity.get();
		if (dataEntity.isAdvance()) {
			HawkLog.logPrintln("OrderActivity buy auth failed ,repeated buy, authorityId: {}, payGiftId: {}", dataEntity.getAuthorityId(), id);
			return;
		}
		dataEntity.setAuthorityId(id);
		int lvl = dataEntity.getLevel();
		
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int i = 1; i <= lvl; i++) {
			OrderLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderLevelCfg.class, i);
			rewardList.addAll(lvlCfg.getAdvRewardList());
		}

		// 发放进阶奖励
		if (!rewardList.isEmpty()) {
			Object[] content;
			content = new Object[1];
			content[0] = lvl;
			sendMailToPlayer(dataEntity.getPlayerId(), MailId.ORDER_ACTIVITY_ADVANCE_REWARD, null, null, content, rewardList);
		}
		
		// 增加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_AUTH, id);
		
		// 流水记录
		getDataGeter().logBuyOrderAuth(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId());
		HawkLog.logPrintln("OrderActivity buy auth success, authorityId: {}, addExp: {}, level: {}", id, cfg.getExp(), dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
	}
	
	/**
	 * 增加战令经验
	 * @param dataEntity
	 * @param addExp
	 */
	@Override
	public void addExp(IOrderDateEntity orderDataEntity, int addExp, int reason, int reasonId) {
		if(!(orderDataEntity instanceof ActivityOrderEntity)){
			return;
		}
		ActivityOrderEntity dataEntity = (ActivityOrderEntity) orderDataEntity;
		String playerId = dataEntity.getPlayerId();
		if (addExp < 0) {
			return;
		}
		int oldExp = dataEntity.getExp();
		int oldLvl = dataEntity.getLevel();

		int newExp = oldExp + addExp;
		int newLvl = calcLevel(newExp);
		if (newLvl > oldLvl) {
			for (int lvl = oldLvl + 1; lvl <= newLvl; lvl++) {
				OrderLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderLevelCfg.class, lvl);
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
			dataEntity.setLevel(newLvl);
		}
		dataEntity.setExp(newExp);
		// 流水记录
		getDataGeter().logOrderExpChange(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), addExp, dataEntity.getExp(), dataEntity.getLevel(), reason, reasonId);
		HawkLog.logPrintln("OrderActivity  expAdd, expBef: {}, expAft: {}, lvlBef: {}, lvlAft: {}, reason: {}, reasonId: {}", oldExp, newExp, oldLvl, newLvl, reason, reasonId);
	}
	
	
	@Override
	public void logOrderFinishId(IOrderDateEntity orderDataEntity, IOrderTaskCfg cfg, OrderItem orderItem, int addTimes) {
		if(!(orderDataEntity instanceof ActivityOrderEntity)){
			return;
		}
		ActivityOrderEntity dataEntity = (ActivityOrderEntity) orderDataEntity;
		getDataGeter().logOrderFinishId(dataEntity.getPlayerId(), dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId(),addTimes,orderItem.getFinishTimes());
	}
	/**
	 * 计算等级
	 * @param exp
	 * @return
	 */
	private int calcLevel(int exp) {
		int level = 0;
		ConfigIterator<OrderLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderLevelCfg.class);
		for (OrderLevelCfg cfg : its) {
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
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityOrderEntity dataEntity = opEntity.get();
		chechAndInitOrder(playerId, dataEntity);
	}
	

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	/**
	 * 购买战令经验
	 * @param playerId
	 * @return
	 */
	public Result<?> buyOrderExp(String playerId, int expId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}

		OrderExpShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderExpShopCfg.class, expId);
		if (cfg == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}

		ActivityOrderEntity dataEntity = opEntity.get();
		// 已经买过
		if (dataEntity.getExpBuyList().contains(expId)) {
			return Result.fail(Status.Error.ORDER_EXP_BUY_REPEATED_VALUE);
		}

		List<RewardItem.Builder> itemList = cfg.getCostList();
		boolean consumeResult = getDataGeter().consumeItems(playerId, itemList, HP.code.ORDER_BUY_EXP_C_VALUE, Action.ORDER_EXP_BUY_CONSUME);
		if (consumeResult == false) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		dataEntity.getExpBuyList().add(expId);
		dataEntity.notifyUpdate();
		// 加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_BUY, expId);
		
		// 流水记录
		getDataGeter().logBuyOrderExp(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId(), cfg.getExp());
		HawkLog.logPrintln("OrderActivity buyOrderExp success, expId: {}, addExp: {}, level: {}", expId, cfg.getExp(), dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		return Result.success();
	}
	
	/**
	 * 获取历史任务信息
	 * @param playerId
	 * @param cycleId
	 */
	public void getGetHistoryInfo(String playerId, int cycleId) {
		if (!isOpening(playerId)) {
			sendErrorAndBreak(playerId, HP.code.ORDER_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}

		Optional<ActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			sendErrorAndBreak(playerId, HP.code.ORDER_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		ActivityOrderEntity dataEntity = opEntity.get();
		List<OrderItem> itemList = new ArrayList<>();
		int currCycle = dataEntity.getWeekCycle();
		if (currCycle < cycleId) {
			sendErrorAndBreak(playerId, HP.code.ORDER_GET_HISTORY_INFO_C_VALUE, Status.Error.ORDER_WEEK_ERROR_VALUE);
			return;
		} else if (currCycle == cycleId) {
			itemList = dataEntity.getOrderList();
		} else {
			for (OrderItem item : dataEntity.getHistoryOrderList()) {
				OrderTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTaskCfg.class, item.getOrderId());
				if (cfg == null || cfg.getCycle() != cycleId) {
					continue;
				}
				itemList.add(item);
			}
		}
		GetHistoryOrderInfoResp.Builder builder = GetHistoryOrderInfoResp.newBuilder();
		builder.setCycleId(cycleId);
		builder.addAllItem(genOrderItemList(itemList));
		pushToPlayer(playerId, HP.code.ORDER_GET_HISTORY_INFO_S_VALUE, builder);
	}
}
