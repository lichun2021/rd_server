package com.hawk.game.module;

import java.util.List;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.serializer.HawkDBChecker;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.QueueSpeedUpEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpQueueEvent;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.plantsoldier.advance.msg.PlantSoldierAdvanceQueueSpeedUpEvent;
import com.hawk.game.msg.EquipQueueSpeedMsg;
import com.hawk.game.msg.QueueBeHelpedMsg;
import com.hawk.game.msg.QueueSpeedMsg;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.queue.QueueCustomData;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.PaidQueuePayReq;
import com.hawk.game.protocol.Queue.QueueCancelReq;
import com.hawk.game.protocol.Queue.QueueFinishFreeReq;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.protocol.Queue.SpeedItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUseItemSpeed;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.GsConst.TimeLimitStoreTriggerType;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 建筑模块
 *
 * @author julia
 */
public class PlayerQueueModule extends PlayerModule {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerQueueModule(Player player) {
		super(player);
	}

	/**
	 * 更新
	 *
	 * @return
	 */
	@Override
	public boolean onTick() {
		QueueService.getInstance().refreshPlayerAllQueue(player, true, false);
		return super.onTick();
	}

	/**
	 * 组装
	 */
	@Override
	protected boolean onPlayerAssemble() {
		return true;
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 先刷新状态

		QueueService.getInstance().refreshPlayerAllQueue(player, false, true);
		// 同步队列新
		player.getPush().syncQueueEntityInfo();
		// 同步付费队列数据
		player.getPush().syncSecondaryBuildQueue();
		//同步队列额外数据
		this.syncQuequeCustomData();
		return true;
	}

	/**
	 * 统一检查队列id，如果有实体则返回
	 * @param hpCode 协议号
	 * @param id 队列id
	 * @return 队列实时
	 */
	private QueueEntity checkReqGetQueueEntity(int hpCode, String id) {
		if (HawkOSOperator.isEmptyString(id)) {
			sendError(hpCode, Status.SysError.PARAMS_INVALID);
			return null;
		}
		QueueEntity queueEntity = player.getData().getQueueEntity(id);
		if (queueEntity == null) {
			sendError(hpCode, Status.Error.QUEUE_NOT_EXIST);
			return null;
		}
		return queueEntity;
	}

	/**
	 * 取消队列
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_CANCEL_C_VALUE)
	private boolean onQueueCancel(HawkProtocol protocol) {
		QueueCancelReq req = protocol.parseProtocol(QueueCancelReq.getDefaultInstance());
		QueueEntity queueEntity = checkReqGetQueueEntity(protocol.getType(), req.getId());
		if (queueEntity == null) {
			logger.error("cancel queue failed, playerId: {}, protocol: {}, queueId: {}, entity: {}", player.getId(), protocol.getType(), req.getId(), queueEntity);
			return false;
		}
		
		// 超时空急救站冷却恢复队列不能取消
		if (queueEntity.getQueueType() == QueueType.BUILDING_RECOVER_QUEUE_VALUE) {
			logger.error("queue speed failed, building recovering queue cannot cancel, playerId: {}, queue entity: {}", player.getId(), queueEntity);
			sendError(protocol.getType(), Status.Error.BUILDING_RECOVER_QUEUE_CANCEL_ERR);
			return false;
		}

		return QueueService.getInstance().cancelOneQueue(player, queueEntity);
	}

	/**
	 * 队列最后几分钟免费完成
	 * @return
	 */
	@ProtocolHandler(code = HP.code.QUEUE_FININSH_FREE_C_VALUE)
	private boolean onQueueFinishFree(HawkProtocol protocol) {
		QueueFinishFreeReq req = protocol.parseProtocol(QueueFinishFreeReq.getDefaultInstance());
		QueueEntity queueEntity = checkReqGetQueueEntity(protocol.getType(), req.getId());
		if (queueEntity == null) {
			logger.error("finish queue failed, playerId: {}, protocol: {}, queueId: {}, entity: {}", player.getId(), protocol.getType(), req.getId(), queueEntity);
			return false;
		}
		
		QueueType queueType = QueueType.valueOf(queueEntity.getQueueType());
		// 只有普通建筑/科技研究队列才能免费加速
		if (queueType == null || (queueType != QueueType.BUILDING_QUEUE && queueType != QueueType.SCIENCE_QUEUE 
									&& queueType != QueueType.PLANT_SCIENCE_QUEUE)) {
			logger.error("finish queue failed, playerId: {}, queueType: {}", player.getId(), queueEntity.getQueueType());
			sendError(protocol.getType(), Status.Error.BUILDING_QUEUE_ONLY);
			return false;
		}

		// 队列已完成
		long now = HawkTime.getMillisecond();
		if (queueEntity.getEndTime() <= now) {
			logger.error("finish queue failed, playerId: {}, endTime: {}", player.getId(), queueEntity.getEndTime());
			return false;
		}
		
		// 免费时间
		int freeTime = queueType == QueueType.BUILDING_QUEUE ? player.getFreeBuildingTime() : player.getFreeTechTime();
		//连续升级时，免费时间翻倍
		if(queueType == QueueType.BUILDING_QUEUE ){
			int multi = queueEntity.getMultiply();
			freeTime *= multi;
		}
		long remainTime = queueEntity.getEndTime() - now;
		if (remainTime / 1000 > freeTime) {
			logger.error("finish queue failed, playerId: {}, remainTime: {}s, freeTime: {}s", player.getId(), remainTime / 1000, freeTime);
			sendError(protocol.getType(), Status.Error.QUEUE_FREE_LACK);
			return false;
		}

		queueEntity.setEndTime(now - 1);
		QueueService.getInstance().finishOneQueue(player, queueEntity, false);
		
		return true;
	}
	
	/**
	 * 开启第二建造队列
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PAID_QUEUE_PAY_C_VALUE)
	private boolean onOpenSecondaryBuildQueue(HawkProtocol protocol) {
		PaidQueuePayReq req = protocol.parseProtocol(PaidQueuePayReq.getDefaultInstance());
		boolean consumeGold = req.getConsumeGold();
		if (player.getData().isSecondBuildUnlock()) {
			logger.error("open paidQueue failed, the queue has opened permanently, playerId: {}, useGold: {}", player.getId(), consumeGold);
			sendError(protocol.getType(), Status.Error.BUILD_QUEUE2_UNLOCKED_PERMANENT);
			if (!consumeGold) {
				backGold(req);
			}
			return false;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		long openTimeLong = 0;
		// 使用水晶开启付费队列
		if(consumeGold) {
			ItemInfo openQueueCost = ConstProperty.getInstance().getPaidQueueOpenCost();
			consume.addConsumeInfo(openQueueCost, false);
			openTimeLong = ConstProperty.getInstance().getPaidQueueTimeLong() * 1000L;
		} else {
			String itemUuid = req.getItemUuid();
			int itemCount = req.getCount();
			HawkAssert.checkPositive(itemCount);
			
			ItemEntity itemEntity = player.getData().getItemById(itemUuid);
			if (itemEntity == null) {
				logger.error("open paidQueue failed, playerId: {}, itemId: {}, entity: {}", player.getId(), itemUuid, itemEntity);
				sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
				return false;
			}
			
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
			if(itemCfg == null) {
				logger.error("open paidQueue itemCfg error, playerId: {}, itemId: {}", player.getId(), itemEntity.getItemId());
				sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
				return false;
			}
			
			// 道具类型判断
			if(itemCfg.getItemType() != ToolType.BUILD_QUEUE_UNLOCK_VALUE) {
				logger.error("open paidQueue itemType error, playerId: {}, itemId: {}, itemType: {}", player.getId(), itemEntity.getItemId(), itemCfg.getItemType());
				sendError(protocol.getType(), Status.Error.ITEM_TYPE_ERROR);
				return false;
			}
			
			BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, itemCfg.getBuffId());
			if(buffCfg == null) {
				logger.error("open paidQueue buffCfg error, playerId: {}, buffId: {}", player.getId(), itemCfg.getBuffId());
				sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
				return false;
			}
			
			openTimeLong = buffCfg.getTime() * 1000L * itemCount;
			consume.addConsumeInfo(ItemType.TOOL, itemUuid, itemEntity.getItemId(), itemCount);
		}
		
		if(!consume.checkConsume(player, protocol.getType())) {
			logger.error("open paidQueue consume error, playerId: {}", player.getId());
			return false;
		}
		
		consume.consumeAndPush(player, Action.OPEN_PAID_QUEUE);
		QueueEntity queueEntity = player.getData().getPaidQueue();
		// 防止初始化失败，再初始化一次
		if(queueEntity == null) {
			queueEntity = QueueService.getInstance().initPaidQueue(player);
		} 
		
		long paidQueueEndTime = queueEntity.getEnableEndTime();
		long now = HawkTime.getMillisecond();
		if(paidQueueEndTime <= now) {
			queueEntity.setEnableEndTime(now + openTimeLong);
		} else {
			queueEntity.setEnableEndTime(paidQueueEndTime + openTimeLong);
		}
		
		// 同步付费队列数据
		player.getPush().syncSecondaryBuildQueue();
		player.responseSuccess(protocol.getType());
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.OPEN_PAID_QUEUE,
     				Params.valueOf("consumeGold", consumeGold),
     				Params.valueOf("itemUuid", consumeGold ? "" : req.getItemUuid()),
     				Params.valueOf("itemCount", consumeGold ? 0 : req.getCount()),
     				Params.valueOf("openTimeLong", openTimeLong),
     				Params.valueOf("enableEndTime", queueEntity.getEnableEndTime()));
		return true;
	}
	
	/**
	 * 第二建造队列永久开启时，使用第二建造队列开启道具直接返还相应数量的黄金
	 * 
	 * @param request
	 */
	private void backGold(PaidQueuePayReq request) {
		if (request.getConsumeGold()) {
			return;
		}
		
		// 数量是否正确
		if (request.getCount() <= 0) {
			logger.error("back gold failed, playerId: {}, itemCount: {}", player.getId(), request.getCount());
			return;
		}
		
		// 道具实体是否存在
		ItemEntity itemEntity = player.getData().getItemById(request.getItemUuid());
		if (itemEntity == null) {
			logger.error("back gold failed, playerId: {}, itemUuid: {}, entity: null", player.getId(), request.getItemUuid());
			return;
		}
		
		// 道具配置是否存在
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
		if(itemCfg == null || itemCfg.getSellPrice() <= 0) {
			logger.error("back gold failed, playerId: {}, itemId: {}, item price: {}", player.getId(), itemEntity.getItemId(), itemCfg == null ? -1 : itemCfg.getSellPrice());
			return;
		}
		
		// 道具类型判断
		if(itemCfg.getItemType() != ToolType.BUILD_QUEUE_UNLOCK_VALUE) {
			logger.error("back gold failed, playerId: {}, itemType: {}", player.getId(), itemCfg.getItemType());
			return;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(ItemType.TOOL, request.getItemUuid(), itemEntity.getItemId(), request.getCount());
		if(!consume.checkConsume(player)) {
			logger.error("back gold failed, item consume error, playerId: {}", player.getId());
			return;
		}
		
		int backGold = itemCfg.getSellPrice() * request.getCount();
		int toolsBackGoldsToday = player.getToolBackGoldToday(backGold);
		if (toolsBackGoldsToday < 0) {
			return;
		}
		
		consume.consumeAndPush(player, Action.OPEN_PAID_QUEUE);
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addGold(backGold);
		awardItem.rewardTakeAffectAndPush(player, Action.BUILDING_ITEM_BACK_GOLD, true);
		
		LocalRedis.getInstance().updateToolBackGold(player.getId(), toolsBackGoldsToday);
	}
	
	
	
	/**
	 * 开启第二建造队列
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.PAID_QUEUE_OPEN_DATA_FREE_REQ_VALUE)
	private boolean onOpenSecondaryBuildQueueFree(HawkProtocol protocol) {
		if (player.getData().isSecondBuildUnlock()) {
			logger.error("onOpenSecondaryBuildQueueFree paidQueue failed, the queue has opened permanently, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.BUILD_QUEUE2_UNLOCKED_PERMANENT);
			return false;
		}
		
		QueueCustomData data = this.getQueueCustomData();
		if(data.isFreeUse()){
			return false;
		}
		int freeHours = ConstProperty.getInstance().getFirstFreeBuildQueue();
		if(freeHours <= 0){
			return false;
		}
		
		long openTimeLong = freeHours * HawkTime.HOUR_MILLI_SECONDS;
		// 使用水晶开启付费队列
		QueueEntity queueEntity = player.getData().getPaidQueue();
		// 防止初始化失败，再初始化一次
		if(queueEntity == null) {
			queueEntity = QueueService.getInstance().initPaidQueue(player);
		} 
		
		long paidQueueEndTime = queueEntity.getEnableEndTime();
		long now = HawkTime.getMillisecond();
		if(paidQueueEndTime <= now) {
			queueEntity.setEnableEndTime(now + openTimeLong);
		} else {
			queueEntity.setEnableEndTime(paidQueueEndTime + openTimeLong);
		}
		data.setFreeUse(true);
		data.setOpenTime(now);
		data.setEndTime(now + openTimeLong);
		this.saveQueueCustomData(data);
		
		// 同步付费队列数据
		player.getPush().syncSecondaryBuildQueue();
		player.responseSuccess(protocol.getType());
		this.syncQuequeCustomData();
        // 行为日志
		LogUtil.logSecondaryBuildQueueFreeUse(player, freeHours);
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.OPEN_PAID_QUEUE,
     				Params.valueOf("consumeGold", 0),
     				Params.valueOf("itemUuid", ""),
     				Params.valueOf("itemCount", 0),
     				Params.valueOf("openTimeLong", openTimeLong),
     				Params.valueOf("enableEndTime", queueEntity.getEnableEndTime()));
		return true;
	}

	
	public void syncQuequeCustomData(){
		QueueCustomData data = this.getQueueCustomData();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PAID_QUEUE_OPEN_DATA_RESP, data.toBuilder()));
	}
	
	/**
	 * 第二建造队列免费开放信息
	 * @return
	 */
	public QueueCustomData getQueueCustomData() {
		QueueCustomData data = new QueueCustomData();
		CustomDataEntity customData = this.player.getData().getCustomDataEntity(GsConst.QUEUE_CUSTOM_DATA_KEY);
		if(Objects.isNull(customData)){
			return data;
		}
		data.parseObject(customData.getArg());
		return data;
	}
	
	
	/**
	 * @param data
	 * @return
	 */
	public void saveQueueCustomData(QueueCustomData data) {
		CustomDataEntity customData = this.player.getData().getCustomDataEntity(GsConst.QUEUE_CUSTOM_DATA_KEY);
		if(Objects.isNull(customData)){
			customData = player.getData().createCustomDataEntity(GsConst.QUEUE_CUSTOM_DATA_KEY, 0, data.toDataString());
		}else{
			customData.setArg(data.toDataString());
		}
	}
	/**
	 * 队列加速
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_SPEED_UP_C_VALUE)
	public boolean onQueueSpeedUp(HawkProtocol protocol) {
		QueueSpeedUpReq req = protocol.parseProtocol(QueueSpeedUpReq.getDefaultInstance());
		QueueEntity queueEntity = checkReqGetQueueEntity(protocol.getType(), req.getId());
		if (queueEntity == null) {
			logger.error("queue speed failed, playerId: {}, protocol: {}, queueId: {}, entity: {}", player.getId(), protocol.getType(), req.getId(), queueEntity);
			return false;
		}
		
		// 超时空急救站冷却恢复队列不能加速
		if (queueEntity.getQueueType() == QueueType.BUILDING_RECOVER_QUEUE_VALUE) {
			logger.error("queue speed failed, building recovering queue cannot speed, playerId: {}, queue entity: {}", player.getId(), queueEntity);
			sendError(protocol.getType(), Status.Error.BUILDING_RECOVER_QUEUE_SPEED_ERR);
			return false;
		}

		// 队列已完成
		if (queueEntity.getEndTime() <= HawkTime.getMillisecond()) {
			logger.error("queue speed failed, playerId: {}, endTime: {}", player.getId(), queueEntity.getEndTime());
			return false;
		}
		
		if (isZeroEarningControlType(queueEntity.getQueueType()) && player.isZeroEarningState()) {
			logger.error("queue speedup failed, player on zero earning status, playerId: {}", player.getId());
        	sendError(protocol.getType(), Status.SysError.ZERO_EARNING_STATE);
        	return false;
		}

		boolean result = true;
		if (req.getIsGold()) {
			result = queueSpeedByGold(queueEntity, protocol.getType());
		} else {
			result = queueSpeedByItem(queueEntity, protocol.getType(), req);
		}
		
		if(!result) {
			return false;
		}
		
		player.responseSuccess(protocol.getType());
		if (queueEntity.getEndTime() > HawkTime.getMillisecond()) {
			// 队列未结束推送刷新
			QueuePB.Builder update = BuilderUtil.genQueueBuilder(queueEntity);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_UPDATE_PUSH_VALUE, update));
		} else {
			// 队列结束,直接走完成协议
			QueueService.getInstance().finishOneQueue(player, queueEntity, true);
		}
		return true;
	}
	
	/**
	 * 判断队列类型是否受零收益限制
	 * @param queueType
	 * @return
	 */
	private boolean isZeroEarningControlType(int queueType) {
		return QueueType.SOILDER_QUEUE_VALUE == queueType || QueueType.TRAP_QUEUE_VALUE == queueType;
	}
	
	/**
	 * 金币加速
	 * @param queueEntity
	 * @return
	 */
	private boolean queueSpeedByGold(QueueEntity queueEntity, int protoType) {
		long needTime = (queueEntity.getEndTime() - HawkTime.getMillisecond()) / 1000L;
		QueueType queueType = QueueType.valueOf(queueEntity.getQueueType());
		if (queueType == QueueType.BUILDING_QUEUE) {
			needTime -= player.getFreeBuildingTime() * queueEntity.getMultiply();
		} else if (queueType == QueueType.SCIENCE_QUEUE) {
			needTime -= player.getFreeTechTime();
		} else if (queueType == QueueType.PLANT_SCIENCE_QUEUE){
			needTime -= player.getFreeTechTime();
		}
		
		QueueStatus queueStatus = QueueStatus.valueOf(queueEntity.getStatus());
		int costGold = GameUtil.caculateTimeGold(needTime, queueType, queueStatus);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, costGold);
		return queueSpeedUp(queueEntity, consume, protoType, -1);
	}
	
	/**
	 * 道具加速
	 * @param queueEntity
	 * @param protoType
	 * @param itemUuid
	 * @param itemCount
	 * @return
	 */
	private boolean queueSpeedByItem(QueueEntity queueEntity, int protoType, QueueSpeedUpReq req) {
		List<SpeedItem> speedItems = req.getSpeedItemList();
		if (speedItems.isEmpty()) {
			throw new RuntimeException("queue speed items empty");
		}
		
		long needTime = queueEntity.getEndTime() - HawkTime.getMillisecond();
		QueueType queueType = QueueType.valueOf(queueEntity.getQueueType());
		if (queueType == QueueType.BUILDING_QUEUE) {
			needTime -= player.getFreeBuildingTime() * 1000;
		} else if (queueType == QueueType.SCIENCE_QUEUE) {
			needTime -= player.getFreeTechTime() * 1000;
		}else if (queueType == QueueType.PLANT_SCIENCE_QUEUE){
			needTime -= player.getFreeTechTime()  * 1000;
		}
		
		boolean onceSpeed = req.hasOnceSpeed() ? req.getOnceSpeed() : false;
		if (!onceSpeed) {
			needTime += ConstProperty.getInstance().getItemSpeedUpTimeThresholdValue() * 1000;
		}
		
		int itemCount = 0;
		long reduceTime = 0;
		ConsumeItems consume = ConsumeItems.valueOf();
		for (SpeedItem speedItem : speedItems) {
			if (speedItem.getCount() <= 0) {
				throw new RuntimeException("queue speed item count negative");
			}
			
			ItemCfg itemCfg = getSpeedItemCfg(speedItem.getItemUUid(), queueEntity.getQueueType(), protoType);
			if (itemCfg == null) {
				return false;
			}
			if (itemCfg.getTblyUse() == 1 ) {
				if (!player.isInDungeonMap()) {
					return false;
				}
			}
			
			itemCount += speedItem.getCount();
			reduceTime += 1000L * itemCfg.getSpeedUpTime() * speedItem.getCount();
			consume.addConsumeInfo(ItemType.TOOL, speedItem.getItemUUid(), itemCfg.getId(), speedItem.getCount());
			if (reduceTime > needTime) {
				break;
			}
		}
		
		boolean result = queueSpeedUp(queueEntity, consume, protoType, reduceTime);
		if (result) {
			RedisProxy.getInstance().idipDailyStatisAdd(player.getId(), IDIPDailyStatisType.ITEM_SPEED_TIME, (int)(reduceTime/1000));
			touchPushGiftMsg(queueEntity, (int)(reduceTime/1000));
		}
		
		return result;
	}
	
	private void touchPushGiftMsg(QueueEntity queueEntity, int reduceTime) {
		if (queueEntity.getQueueType() != QueueType.EQUIP_RESEARCH_QUEUE_VALUE) {
			HawkApp.getInstance().postMsg(player, QueueSpeedMsg.valueOf(queueEntity.getQueueType(), reduceTime));
			return;
		} 
		
		try {
			String itemIdParam = queueEntity.getItemId();
			if (HawkOSOperator.isEmptyString(itemIdParam)) {
				return;
			}
			
			String[] paramArray = itemIdParam.trim().split("_");
			if (paramArray.length < 2) {
				return;
			}
			
			
			int researchId = Integer.parseInt(paramArray[0]);
			int level = Integer.parseInt(paramArray[1]);
			EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, level + 1);
			if (cfg == null) {
				return;
			}
			
			ItemInfo itemInfo = ItemInfo.valueOf(cfg.getCost());
			if (itemInfo == null || itemInfo.getCount() <= 0) {
				return;
			}
			
			HawkApp.getInstance().postMsg(player, EquipQueueSpeedMsg.valueOf(reduceTime, itemInfo.getItemId(), (int)itemInfo.getCount()));
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 道具检测
	 * 
	 * @param itemUuid
	 * @param queueType
	 * @param protoType
	 * @return
	 */
	private ItemCfg getSpeedItemCfg(String itemUuid, int queueType, int protoType) {
		ItemEntity itemEntity = player.getData().getItemById(itemUuid);
		if (itemEntity == null) {
			logger.error("queue speed failed, playerId: {}, itemId: {}, entity: {}", player.getId(), itemUuid, itemEntity);
			sendError(protoType, Status.Error.ITEM_NOT_FOUND);
			return null;
		}

		// 非加速道具
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
		if (itemCfg.getItemType() != ToolType.SPEED_UP_VALUE) {
			logger.error("queue speed failed, playerId: {}, itemType: {}", player.getId(), itemCfg.getItemType());
			sendError(protoType, Status.Error.ITEM_TYPE_ERROR);
			return null;
		}

		// 不是通用加速道具只能用在指定功能上
		if (!itemCfg.speedUpAble(queueType)) {
			logger.error("queue speed failed, playerId: {}, item speedType: {}", player.getId(), itemCfg.getSpeedUpType());
			sendError(protoType, Status.Error.ITEM_TYPE_ERROR);
			return null;
		}
		
		return itemCfg;
	}
	
	/**
	 * 队列加速
	 * 
	 * @param queueEntity
	 * @param consume
	 * @param protoType
	 * @param reduceTime
	 * @param logParam
	 * @return
	 */
	private boolean queueSpeedUp(QueueEntity queueEntity, ConsumeItems consume, int protoType, long reduceTime) {
		if (!consume.checkConsume(player, protoType)) {
			return false;
		}

		// 消耗
		consume.consumeAndPush(player, GameUtil.queueTypeToAction(queueEntity.getQueueType()));
		long speedTime = reduceTime;
		
		// 减少时间
		if (reduceTime >= 0) {
			queueEntity.setEndTime(queueEntity.getEndTime() - reduceTime);
			queueEntity.setTotalReduceTime(queueEntity.getTotalReduceTime() + reduceTime);
		} else {
			long now = HawkTime.getMillisecond();
			speedTime = queueEntity.getEndTime() - now;
			queueEntity.setEndTime(now - 1);
		}
		
		// 活动事件
		ActivityManager.getInstance().postEvent(new QueueSpeedUpEvent(player.getId(), queueEntity.getQueueType(), speedTime));
		ActivityManager.getInstance().postEvent(new UseItemSpeedUpEvent(player.getId(), (int)(reduceTime / 1000 / 60)));
		ActivityManager.getInstance().postEvent(new UseItemSpeedUpQueueEvent(player.getId(), queueEntity.getQueueType(), (int)(reduceTime / 1000 / 60)));
		MissionManager.getInstance().postMsg(player, new EventUseItemSpeed((int)(reduceTime / 1000 / 60)));
		if (queueEntity.getQueueType() == QueueType.BUILDING_QUEUE_VALUE) {
			HawkApp.getInstance().postMsg(player, new TimeLimitStoreTriggerMsg(TimeLimitStoreTriggerType.BUILDING_SPEED, (int)(reduceTime/1000)));
		}
		
		if (queueEntity.getQueueType() == QueueType.PLANT_ADVANCE_QUEUE_VALUE) {
			HawkApp.getInstance().postMsg(player, new PlantSoldierAdvanceQueueSpeedUpEvent(reduceTime));
		}
	
		logger.info("player queue speed up, playerId: {}, sppeedTime: {}, queueId: {}, queueItemInfo: {}, queueType: {}, endTime: {}, totalReduceTime: {}", player.getId(), 
			speedTime/1000, queueEntity.getId(), queueEntity.getItemId(), queueEntity.getQueueType(), queueEntity.getEndTime(), queueEntity.getTotalReduceTime());
		
		return true;
	}
	/**
	 * 更新队列的数据
	 * @param queueEntity
	 * @param reduceTime
	 * @return
	 */
	@MessageHandler
	public boolean onBeHelped(QueueBeHelpedMsg msg) {
		String queueId = msg.getQueueId();
		long assistTime= msg.getAssistTime();
		QueueEntity entity = player.getData().getQueueEntity(queueId);
		if (assistTime <= 0 || entity.getReusage() == QueueReusage.FREE.intValue()) {
			return false;
		}
		entity.setEndTime(entity.getEndTime() - assistTime);
		entity.setTotalReduceTime(entity.getTotalReduceTime() + assistTime);
		// 队列未结束推送刷新
		if (entity.getEndTime() > HawkTime.getMillisecond()) {
			QueuePB.Builder update = BuilderUtil.genQueueBuilder(entity);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_UPDATE_PUSH_VALUE, update));
			return true;
		}

		// 玩家不在线情况下被触发（calcElectricBeforeChange方法里面有判断），先算一遍战力，防止后面结算完再刷战力时没有基础对比造成数据错误
		player.getData().getPowerElectric().calcElectricBeforeChange();

		// 队列结束,直接走完成协议
		QueueService.getInstance().finishOneQueue(player, entity, false);
		return true;
	}

	
	
	

	
}