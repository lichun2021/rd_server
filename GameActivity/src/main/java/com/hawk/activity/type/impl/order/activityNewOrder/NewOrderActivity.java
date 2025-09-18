package com.hawk.activity.type.impl.order.activityNewOrder;

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
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.order.IOrderActivity;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderAuthorityCfg;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderExpShopCfg;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderLevelCfg;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderTaskCfg;
import com.hawk.activity.type.impl.order.activityNewOrder.entity.NewActivityOrderEntity;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.game.protocol.Activity.GetNewOrderPageInfoResp;
import com.hawk.game.protocol.Activity.NewOrderBaseInfo;
import com.hawk.game.protocol.Activity.OrderItemPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class NewOrderActivity extends ActivityBase implements IOrderActivity{
	
	public NewOrderActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.NEW_ORDER_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		NewOrderActivity activity = new NewOrderActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<NewActivityOrderEntity> queryList = HawkDBManager.getInstance()
				.query("from NewActivityOrderEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			NewActivityOrderEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		NewActivityOrderEntity entity = new NewActivityOrderEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.NEW_ORDER_INIT, () -> {
				Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				chechAndInitOrder(playerId, opEntity.get());
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		this.chechAndInitOrder(playerId, opEntity.get());
	}
	
	/**
	 * 检测/初始化战令任务
	 * @param playerId
	 * @param activityOrderEntity
	 */
	private void chechAndInitOrder(String playerId, NewActivityOrderEntity dataEntity) {
		if(!this.isOpening(playerId)){
			return;
		}
		if(!dataEntity.getOrderList().isEmpty()){
			return;
		}
		List<OrderItem>  olist = this.initOrder();
		dataEntity.resetOrderList(olist);
	}
	
	/**
	 * 构建当前活动界面信息PB
	 * @param playerId
	 */
	private GetNewOrderPageInfoResp.Builder genPageInfoBuilder(NewActivityOrderEntity dataEntity) {
		GetNewOrderPageInfoResp.Builder builder = GetNewOrderPageInfoResp.newBuilder();
		builder.setBaseInfo(genBaseInfoBuilder(dataEntity));
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
	private NewOrderBaseInfo.Builder genBaseInfoBuilder(NewActivityOrderEntity dataEntity) {
		HawkTuple2<Integer, Integer> rlt = this.calLevelAndExp(dataEntity.getExp());
		NewOrderBaseInfo.Builder builder = NewOrderBaseInfo.newBuilder();
		builder.setLevel(rlt.first);
		builder.setExp(rlt.second);
		builder.setAuthorityId(dataEntity.getAuthorityId());
		List<Integer> rewardList = dataEntity.getRewardList();
		if(!rewardList.isEmpty()){
			builder.addAllRewardLevel(rewardList);
		}
		List<Integer> buyList = dataEntity.getExpBuyList();
		if (!buyList.isEmpty()) {
			builder.addAllBuyExpId(buyList);
		}
		return builder;
	}
	

	public HawkTuple2<Integer, Integer> calLevelAndExp(int exp){
		List<NewOrderLevelCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(NewOrderLevelCfg.class).toList();
		int level = 0;
		int expShow = 0;
		for(NewOrderLevelCfg cfg : list){
			if(exp >= cfg.getLevelUpExp() && cfg.getLevel() > level){
				level = cfg.getLevel();
				expShow =(exp - cfg.getLevelUpExp());
			}
			
		}
		return new HawkTuple2<Integer, Integer>(level,expShow);
	}
	
	

	/**
	 * 根据周数初始化战令任务列表
	 * @param currCycle
	 * @return
	 */
	private List<OrderItem> initOrder() {
		List<OrderItem> list = new CopyOnWriteArrayList<>();
		ConfigIterator<NewOrderTaskCfg> its = HawkConfigManager.getInstance().
				getConfigIterator(NewOrderTaskCfg.class);
		for(NewOrderTaskCfg cfg : its){
			OrderItem item = new OrderItem();
			item.setOrderId(cfg.getId());
			list.add(item);
		}
		return list;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GetNewOrderPageInfoResp.Builder builder = genPageInfoBuilder(opEntity.get());
		pushToPlayer(playerId, HP.code.NEW_ORDER_PAGE_INFO_SYNC_VALUE, builder);
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
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		NewActivityOrderEntity dataEntity = opEntity.get();
		// 阶段检测
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
				NewOrderTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderTaskCfg.class, orderItem.getOrderId());
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
			GetNewOrderPageInfoResp.Builder builder = genPageInfoBuilder(dataEntity);
			pushToPlayer(playerId, HP.code.NEW_ORDER_PAGE_INFO_SYNC_VALUE, builder);;
		}
	}
	
	
	@Subscribe
	public void onAuthBuyEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		int id = Integer.valueOf(event.getGiftId());
		NewOrderAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderAuthorityCfg.class, id);
		if (cfg == null) {
			return;
		}
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		NewActivityOrderEntity dataEntity = opEntity.get();
		if (dataEntity.isAdvance()) {
			HawkLog.logPrintln("OrderActivity,buy,auth,failed,repeated buy, authorityId: {}, payGiftId: {}", dataEntity.getAuthorityId(), id);
			return;
		}
		dataEntity.setAuthorityId(id);
		HawkTuple2<Integer, Integer>  level = this.calLevelAndExp(dataEntity.getExp());
		List<Integer> rewardRecord = dataEntity.getRewardList();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int i = 1; i <= level.first; i++) {
			if(!rewardRecord.contains(i)){
				continue;
			}
			NewOrderLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderLevelCfg.class, i);
			rewardList.addAll(lvlCfg.getAdvRewardList());
		}
		// 发放进阶奖励
		if (!rewardList.isEmpty()) {
			Object[] content;
			content = new Object[1];
			content[0] = level.first;
			sendMailToPlayer(dataEntity.getPlayerId(), MailId.NEW_ORDER_ACTIVITY_ADVANCE_REWARD, null, null, content, rewardList);
		}
		// 增加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_AUTH, id);
		// 流水记录
		getDataGeter().logNewBuyOrderAuth(playerId, dataEntity.getTermId(),cfg.getId());
		HawkLog.logPrintln("OrderActivity,onAuthBuyEvent,success, authorityId: {}, addExp: {}, level: {}", id, cfg.getExp(), level.first);
		pushToPlayer(playerId, HP.code.NEW_ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(dataEntity));
	}
	
	/**
	 * 增加战令经验
	 * @param dataEntity
	 * @param addExp
	 */
	@Override
	public void addExp(IOrderDateEntity orderDataEntity, int addExp, int reason, int reasonId) {
		if(!(orderDataEntity instanceof NewActivityOrderEntity)){
			return;
		}
		NewActivityOrderEntity dataEntity = (NewActivityOrderEntity) orderDataEntity;
		String playerId = dataEntity.getPlayerId();
		if (addExp < 0) {
			return;
		}
		int oldExp = dataEntity.getExp();
		int newExp = oldExp + addExp;
		dataEntity.setExp(newExp);
		HawkTuple2<Integer, Integer> rlt = this.calLevelAndExp(newExp);
		// 流水记录
		getDataGeter().logNewOrderExpChange(playerId, dataEntity.getTermId(), addExp, newExp,rlt.second,rlt.first, reason, reasonId);
		HawkLog.logPrintln("NewOrderActivity,expAdd, expBef: {}, expAft: {},lvAft: {},lvExpAft: {},  reason: {}, reasonId: {}", oldExp, newExp,rlt.first, rlt.second,reason, reasonId);
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
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}

		NewOrderExpShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderExpShopCfg.class, expId);
		if (cfg == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		NewActivityOrderEntity dataEntity = opEntity.get();
		// 已经买过
		if (dataEntity.getExpBuyList().contains(expId)) {
			return Result.fail(Status.Error.NEW_ORDER_EXP_BUY_LIMIT_VALUE);
		}

		List<RewardItem.Builder> itemList = cfg.getCostList();
		boolean consumeResult = getDataGeter().consumeItems(playerId, itemList, HP.code.NEW_ORDER_BUY_EXP_C_VALUE, Action.NEW_ORDER_EXP_BUY_CONSUME);
		if (consumeResult == false) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		dataEntity.getExpBuyList().add(expId);
		dataEntity.notifyUpdate();
		// 加经验
		addExp(dataEntity, cfg.getExp(), EXP_REASON_BUY, expId);
		// 流水记录
		getDataGeter().logNewBuyOrderExp(playerId, dataEntity.getTermId(), cfg.getId(), cfg.getExp());
		HawkLog.logPrintln("OrderActivity,buyOrderExp,success, expId: {}, addExp: {}, totalExp: {}", expId, cfg.getExp(), dataEntity.getExp());
		pushToPlayer(playerId, HP.code.NEW_ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(dataEntity));
		return Result.success();
	}
	
	
	@Override
	public void logOrderFinishId(IOrderDateEntity orderDataEntity, IOrderTaskCfg cfg, OrderItem orderItem, int addTimes) {
		if(!(orderDataEntity instanceof NewActivityOrderEntity)){
			return;
		}
		NewActivityOrderEntity dataEntity = (NewActivityOrderEntity) orderDataEntity;
		getDataGeter().logNewOrderFinishId(dataEntity.getPlayerId(), dataEntity.getTermId(),cfg.getId(),addTimes,orderItem.getFinishTimes());
	}
	
	
	public void getLevelReward(String playerId,int level){
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		NewActivityOrderEntity dataEntity = opEntity.get();
		HawkTuple2<Integer, Integer>  levelRlt = this.calLevelAndExp(dataEntity.getExp());
		int curLevel = levelRlt.first;
		if(level > curLevel){
			HawkLog.logPrintln("NewOrderActivity,getLevelReward,fail,playerId:{}, level: {},curLevel:{}", playerId, level, curLevel);
			return;
		}
		List<Integer> rewardRecord = dataEntity.getRewardList();
		if(rewardRecord.contains(level)){
			HawkLog.logPrintln("NewOrderActivity,getLevelReward,achived,playerId:{}, level: {},curLevel:{}", playerId, level, curLevel);
			return;
		}
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		NewOrderLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderLevelCfg.class, level);
		rewardList.addAll(lvlCfg.getNormalRewardList());
		if(dataEntity.isAdvance()){
			rewardList.addAll(lvlCfg.getAdvRewardList());
		}
		dataEntity.addRewardAchive(level);
		this.getDataGeter().takeReward(playerId, rewardList, Action.NEW_ORDER_LEVEL_REWARD, true);
		pushToPlayer(playerId, HP.code.NEW_ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(dataEntity));
		HawkLog.logPrintln("NewOrderActivity,getLevelReward,sucess,playerId:{},authorityId: {}, level: {},curLevel:{}", 
				playerId,dataEntity.getAuthorityId(),level, curLevel);
	}

	public void getLevelRewardOneKey(String playerId){
		Optional<NewActivityOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		NewActivityOrderEntity dataEntity = opEntity.get();
		HawkTuple2<Integer, Integer>  levelRlt = this.calLevelAndExp(dataEntity.getExp());
		int curLevel = levelRlt.first;
		List<Integer> rewardRecord = dataEntity.getRewardList();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int i = 1; i <= curLevel; i++) {
			if(rewardRecord.contains(i)){
				continue;
			}
			NewOrderLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(NewOrderLevelCfg.class, i);
			rewardList.addAll(lvlCfg.getNormalRewardList());
			if(dataEntity.isAdvance()){
				rewardList.addAll(lvlCfg.getAdvRewardList());
			}
			dataEntity.addRewardAchive(i);
		}
		this.getDataGeter().takeReward(playerId, rewardList, Action.NEW_ORDER_LEVEL_REWARD, true);
		pushToPlayer(playerId, HP.code.NEW_ORDER_BASE_INFO_SYNC_VALUE, genBaseInfoBuilder(dataEntity));
		HawkLog.logPrintln("NewOrderActivity,getLevelReward,sucess,playerId:{},authorityId: {}, level: {},curLevel:{}",
				playerId,dataEntity.getAuthorityId(),1, curLevel);
	}
}
