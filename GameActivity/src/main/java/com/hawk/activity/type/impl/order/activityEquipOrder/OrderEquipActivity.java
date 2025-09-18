package com.hawk.activity.type.impl.order.activityEquipOrder;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.order.IOrderActivity;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipActivityKVCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipAuthorityCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipLevelCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipTaskCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.entity.OrderEquipEntity;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.game.protocol.Activity.GetHistoryOrderEquipInfoResp;
import com.hawk.game.protocol.Activity.GetOrderEquipPageInfoResp;
import com.hawk.game.protocol.Activity.OrderEquipBaseInfo;
import com.hawk.game.protocol.Activity.OrderEquipItemsInfoSync;
import com.hawk.game.protocol.Activity.OrderItemPB;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class OrderEquipActivity extends ActivityBase implements IOrderActivity{

	public OrderEquipActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ORDER_EQUIP_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OrderEquipActivity activity = new OrderEquipActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<OrderEquipEntity> queryList = HawkDBManager.getInstance()
				.query("from OrderEquipEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			OrderEquipEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		OrderEquipEntity entity = new OrderEquipEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ORDER_EQUIP_INIT, () -> {
				Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
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
	 * @param dataEntity
	 */
	private void chechAndInitOrder(String playerId, OrderEquipEntity dataEntity) {
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
			GetOrderEquipPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
			pushToPlayer(playerId, HP.code.ORDER_EQUIP_PAGE_INFO_SYNC_VALUE, builder);
		}

	}
	
	/**
	 * 构建当前活动界面信息PB
	 * @param playerId
	 */
	private GetOrderEquipPageInfoResp.Builder genPageInfoBuilder(String playerId) {
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		OrderEquipEntity dataEntity = opEntity.get();

		GetOrderEquipPageInfoResp.Builder builder = GetOrderEquipPageInfoResp.newBuilder();
		builder.setBaseInfo(genBaseInfoBuilder(playerId));
		builder.addAllItems(genOrderItemList(dataEntity.getOrderList()));
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
	private OrderEquipBaseInfo.Builder genBaseInfoBuilder(String playerId) {
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		OrderEquipEntity dataEntity = opEntity.get();
		OrderEquipBaseInfo.Builder builder = OrderEquipBaseInfo.newBuilder();
		builder.setCurrCycle(dataEntity.getWeekCycle());
		builder.setLevel(dataEntity.getLevel());
		builder.setExp(calcShowExp(dataEntity));
		builder.setAuthorityId(dataEntity.getAuthorityId());
		builder.setWeekNumber(dataEntity.getWeekNumber());
		return builder;
	}
	
	
	/**
	 * 计算展示经验
	 * @param dataEntity
	 * @return
	 */
	private int calcShowExp(OrderEquipEntity dataEntity) {
		int showExp = 0;
		for (int lvl = 1; lvl <= dataEntity.getLevel(); lvl++) {
			OrderEquipLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipLevelCfg.class, lvl);
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
		ConfigIterator<OrderEquipTaskCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderEquipTaskCfg.class);
		for(OrderEquipTaskCfg cfg : its){
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
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderEquipEntity dataEntity = opEntity.get();
		chechAndInitOrder(playerId, dataEntity);
		GetOrderEquipPageInfoResp.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.ORDER_EQUIP_PAGE_INFO_SYNC_VALUE, builder);
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
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderEquipEntity dataEntity = opEntity.get();

		// 阶段检测
		chechAndInitOrder(playerId, dataEntity);

		List<OrderTaskParser<?>> parsers = OrderTaskContext.getParser(event.getClass());
		if (parsers == null) {
			logger.info("OrderEquipTaskParser not found, eventClass: {}", event.getClass().getName());
			return;
		}

		List<OrderItem> orderList = dataEntity.getOrderList();
		boolean update = false;
		List<OrderItem> changeList = new ArrayList<>();
		for (OrderTaskParser<?> parser : parsers) {
			for (OrderItem orderItem : orderList) {
				OrderEquipTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipTaskCfg.class, orderItem.getOrderId());
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
			OrderEquipItemsInfoSync.Builder builder = OrderEquipItemsInfoSync.newBuilder();
			builder.addAllItem(genOrderItemList(orderList));
			pushToPlayer(playerId, HP.code.ORDER_EQUIP_ITEM_INFO_CHANGE_SYNC_VALUE, builder);
			pushToPlayer(playerId, HP.code.ORDER_EQUIP_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		}
	}
	
	
	@Subscribe
	public void onAuthBuyEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		int id = Integer.valueOf(event.getGiftId());
		OrderEquipAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipAuthorityCfg.class, id);
		if (cfg == null) {
			return;
		}

		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		OrderEquipEntity dataEntity = opEntity.get();
		int currAuth = dataEntity.getAuthorityId();
		if (currAuth != 0) {
			HawkLog.logPrintln("OrderEquipActivity buy auth failed ,cfg error, playerId:{}, currAuthorityId: {}, payGiftId: {}", dataEntity.getPlayerId(),
					dataEntity.getAuthorityId(), id);
			return;
		}
		dataEntity.setAuthorityId(1);
		// 首次进阶,补发进价奖励
		int lvl = dataEntity.getLevel();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int i = 1; i <= lvl; i++) {
			OrderEquipLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipLevelCfg.class, i);
			rewardList.addAll(lvlCfg.getAdvRewardList());
		}
		// 发放进阶奖励
		if (!rewardList.isEmpty()) {
			Object[] content;
			content = new Object[1];
			content[0] = lvl;
			sendMailToPlayer(dataEntity.getPlayerId(), MailId.ORDER_EQUIP_ACTIVITY_ADVANCE_REWARD, null, null, content, rewardList);
		}
		// 增加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_AUTH, id);
		// 流水记录
		getDataGeter().logBuyOrderEquipAuth(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId());
		pushToPlayer(playerId, HP.code.ORDER_EQUIP_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		HawkLog.logPrintln("OrderEquipActivity buy auth success, authorityId: {}, addExp: {}, level: {}", id, cfg.getExp(), dataEntity.getLevel());
	}
	
	/**
	 * 增加战令经验
	 * @param orderDataEntity
	 * @param addExp
	 * @param reason
	 * @param reasonId
	 */
	@Override
	public void addExp(IOrderDateEntity orderDataEntity, int addExp, int reason, int reasonId) {
		if(!(orderDataEntity instanceof OrderEquipEntity)){
			return;
		}
		OrderEquipEntity dataEntity = (OrderEquipEntity) orderDataEntity;
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
				OrderEquipLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipLevelCfg.class, lvl);
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
					sendMailToPlayer(playerId, MailId.ORDER_EQUIP_ACTIVITY_LVL_REWARD, null, null, content, rewardList);
				}
			}
			dataEntity.setLevel(newLvl);
		}
		dataEntity.setExp(newExp);
		// 流水记录
		getDataGeter().logOrderEquipExpChange(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), addExp, dataEntity.getExp(), dataEntity.getLevel(), reason, reasonId);
		HawkLog.logPrintln("OrderEquipActivity  expAdd, expBef: {}, expAft: {}, lvlBef: {}, lvlAft: {}, reason: {}, reasonId: {}", oldExp, newExp, oldLvl, newLvl, reason, reasonId);
	}
	
	
	@Override
	public void logOrderFinishId(IOrderDateEntity orderDataEntity, IOrderTaskCfg cfg, OrderItem orderItem, int addTimes) {
		if(!(orderDataEntity instanceof OrderEquipEntity)){
			return;
		}
		OrderEquipEntity dataEntity = (OrderEquipEntity) orderDataEntity;
		getDataGeter().logOrderEquipFinishId(dataEntity.getPlayerId(), dataEntity.getTermId(), dataEntity.getWeekCycle(), cfg.getId(),addTimes,orderItem.getFinishTimes());
	}
	/**
	 * 计算等级
	 * @param exp
	 * @return
	 */
	private int calcLevel(int exp) {
		int level = 0;
		ConfigIterator<OrderEquipLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(OrderEquipLevelCfg.class);
		for (OrderEquipLevelCfg cfg : its) {
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
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		OrderEquipEntity dataEntity = opEntity.get();
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
			sendErrorAndBreak(playerId, HP.code.ORDER_EQUIP_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}

		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			sendErrorAndBreak(playerId, HP.code.ORDER_EQUIP_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		OrderEquipEntity dataEntity = opEntity.get();
		List<OrderItem> itemList = new ArrayList<>();
		int currCycle = dataEntity.getWeekCycle();
		if (currCycle < cycleId) {
			sendErrorAndBreak(playerId, HP.code.ORDER_EQUIP_GET_HISTORY_INFO_C_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		} else if (currCycle == cycleId) {
			itemList = dataEntity.getOrderList();
		} else {
			for (OrderItem item : dataEntity.getHistoryOrderList()) {
				OrderEquipTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipTaskCfg.class, item.getOrderId());
				if (cfg == null || cfg.getCycle() != cycleId) {
					continue;
				}
				itemList.add(item);
			}
		}
		GetHistoryOrderEquipInfoResp.Builder builder = GetHistoryOrderEquipInfoResp.newBuilder();
		builder.setCycleId(cycleId);
		builder.addAllItems(genOrderItemList(itemList));
		pushToPlayer(playerId, HP.code.ORDER_EQUIP_GET_HISTORY_INFO_S_VALUE, builder);
	}

	public Result<?> buyAuthLvl(String playerId, int currLvl) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<OrderEquipEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		OrderEquipEntity dataEntity = opEntity.get();
		OrderEquipActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(OrderEquipActivityKVCfg.class);
		if(dataEntity.getWeekNumber() >= kvCfg.getUnlockTime()){
			return Result.fail(Status.Error.ORDER_EQUIP_WEEK_ORDER_VALUE);
		}
		
		if (dataEntity.getLevel() != currLvl) {
			return Result.fail(Status.Error.ORDER_EQUIP_LVL_CHANGED_VALUE);
		}

		OrderEquipLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipLevelCfg.class, currLvl + 1);
		if (lvlCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		int addExp = lvlCfg.getLevelUpExp() - dataEntity.getExp();

		float unlockRate = (float) (kvCfg.getUnlockGold() / 10000f);
		int needGold = (int) (addExp * unlockRate);
		needGold = Math.max(1, needGold);
		Reward.RewardItem.Builder costBuilder = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, kvCfg.getUnlockCostId(), needGold);
		boolean flag = getDataGeter().cost(playerId, Arrays.asList(costBuilder), 1, Action.ORDER_EQUIP_LVL_BUY, true);
		if (!flag) {
			return Result.fail(Status.Error.DIAMONDS_NOT_ENOUGH_VALUE);
		}

		// 加经验
		addExp(dataEntity, addExp, EXP_REASON_BUY, currLvl);
		dataEntity.addWeekNumber();
		dataEntity.notifyUpdate();

		// 流水记录
		getDataGeter().logBuyOrderEquipExp(playerId, dataEntity.getTermId(), dataEntity.getWeekCycle(), currLvl, addExp);
		HawkLog.logPrintln("OrderEquipActivity buyOrderExp success, currLvl: {}, addExp: {}, level: {}", currLvl, addExp, dataEntity.getLevel());
		pushToPlayer(playerId, HP.code.ORDER_EQUIP_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(playerId));
		return Result.success();
	}

}
