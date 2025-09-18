package com.hawk.game.service;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.db.HawkDBManager;
import org.hawk.db.entifytype.EntityType;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.msg.BuildingQueueCancelMsg;
import com.hawk.game.msg.BuildingQueueFinishMsg;
import com.hawk.game.msg.BuildingRecoverFinishMsg;
import com.hawk.game.msg.CancelTechQueueMsg;
import com.hawk.game.msg.CrossTechQueueFinishMsg;
import com.hawk.game.msg.CurePlantQueueFinishMsg;
import com.hawk.game.msg.CureQueueCancelMsg;
import com.hawk.game.msg.CureQueueFinishMsg;
import com.hawk.game.msg.EquipQueueCancelMsg;
import com.hawk.game.msg.EquipQueueFinishMsg;
import com.hawk.game.msg.EquipResearchQueueFinishMsg;
import com.hawk.game.msg.HospiceQueueFinishMsg;
import com.hawk.game.msg.PlantScienceQueueFinishMsg;
import com.hawk.game.msg.TechQueueFinishMsg;
import com.hawk.game.msg.TrainQueueCancelMsg;
import com.hawk.game.msg.TrainQueueFinishMsg;
import com.hawk.game.msg.YuriStrikeCleanFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Queue.QueuePBSimple;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 队列操作类
 * @author julia
 *
 */
public class QueueService {

	private static QueueService queueService = null;
	public static QueueService getInstance() {
		if (queueService == null) {
			queueService = new QueueService();
		}
		return queueService;
	}

	/**
	 * 添加可重用队列
	 * @param player
	 * @param armyCfg
	 * @param trainTime
	 * @param consumeRes
	 */
	public QueueEntity addReusableQueue(Player player, int queueType, int queueStatus, String itemId, int buildType, 
			double costtime, List<ItemInfo> cancelBackRes, GsConst.QueueReusage reuseType) {
		if (GsConst.QueueReusage.FREE == reuseType) {
			return addQueue(player, queueType, queueStatus, itemId, buildType, HawkApp.getInstance().getCurrentTime(), costtime, cancelBackRes, reuseType,0);
		}

		QueueEntity freeQueue = player.getData().getFreeQueue(queueType);
		if (freeQueue == null) {
			freeQueue = addQueue(player, queueType, queueStatus, itemId,
					buildType, HawkTime.getMillisecond(), costtime, cancelBackRes, reuseType,0);
		} else {
			freeQueue.update(queueType, queueStatus, itemId, buildType, HawkTime.getMillisecond(), costtime, cancelBackRes, reuseType,0);
			QueuePB.Builder pushQueue = BuilderUtil.genQueueBuilder(freeQueue);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_ADD_PUSH_VALUE, pushQueue));
		}
		
		HawkLog.logPrintln("add queue, playerId: {}, queueType: {}, queueStatus: {}, itemId: {}, costtime: {}, cancelBackRes: {}", player.getId(), queueType, queueStatus, itemId, String.valueOf(costtime), cancelBackRes);

		return freeQueue;
	}
	
	public QueueEntity addReusableQueue(Player player, int queueType, int queueStatus, String itemId, int buildType, 
			double costtime, List<ItemInfo> cancelBackRes, GsConst.QueueReusage reuseType,int multiply) {
		//一键升X级
		if (GsConst.QueueReusage.FREE == reuseType) {
			return addQueue(player, queueType, queueStatus, itemId, buildType, HawkApp.getInstance().getCurrentTime(), costtime, cancelBackRes, reuseType,multiply);
		}

		QueueEntity freeQueue = player.getData().getFreeQueue(queueType);
		if (freeQueue == null) {
			freeQueue = addQueue(player, queueType, queueStatus, itemId,
					buildType, HawkTime.getMillisecond(), costtime, cancelBackRes, reuseType,multiply);
		} else {
			freeQueue.update(queueType, queueStatus, itemId, buildType, HawkTime.getMillisecond(), costtime, cancelBackRes, reuseType,multiply);
			QueuePB.Builder pushQueue = BuilderUtil.genQueueBuilder(freeQueue);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_ADD_PUSH_VALUE, pushQueue));
		}
		
		HawkLog.logPrintln("add queue, playerId: {}, queueType: {}, queueStatus: {}, itemId: {}, costtime: {}, cancelBackRes: {}", player.getId(), queueType, queueStatus, itemId, String.valueOf(costtime), cancelBackRes);

		return freeQueue;
	}
	
	/**
	 * 初始化第二建造队列
	 * @param player
	 */
	public QueueEntity initPaidQueue(Player player) {
		QueueEntity queue = player.getData().getPaidQueue();
		if(queue == null) {
			queue = addQueue(player, 0, 0, GsConst.NULL_STRING, 0, 0, 0d, null, GsConst.QueueReusage.FREE,0);
			if(queue != null) {
				queue.setEnableEndTime(HawkApp.getInstance().getCurrentTime());
			}
		}
		return queue;
	}
	
	/**
	 * 增加一个队列
	 * 
	 * @param player  
	 * @param queueType 队列类型
	 * @param itemId  附带存储值,建筑放建筑的唯一id
	 * @param buildType 建筑类型
	 * @param startTime 开始时间(时间点)	单位毫秒
	 * @param costTime  建筑建造需要的号描述(时间段) 单位毫秒
	 * @param cancelBackRes 
	 * @return 失败null，成功返回实体
	 */
	public QueueEntity addQueue(Player player, int queueType, int queueStatus, String itemId,
			int buildingType, long startTime, double costTime, List<ItemInfo> cancelBackRes, GsConst.QueueReusage reusage,int multiply) {
		QueueEntity queueEntity = new QueueEntity();
		queueEntity.setId(HawkOSOperator.randomUUID());
		queueEntity.setEndTime(startTime + (long) Math.ceil(costTime / 1000) * 1000);
		queueEntity.setItemId(itemId);
		queueEntity.setPlayerId(player.getId());
		queueEntity.setQueueType(queueType);
		queueEntity.setStartTime(startTime);
		queueEntity.setTotalQueueTime(queueEntity.getEndTime() - startTime);
		queueEntity.setBuildingType(buildingType);
		queueEntity.setStatus(queueStatus);
		queueEntity.setReusage(reusage.intValue());  // 可重用队列
		queueEntity.setMultiply(multiply);
		if (cancelBackRes != null && cancelBackRes.size() > 0) {
			AwardItems items = AwardItems.valueOf();
			items.addItemInfos(cancelBackRes);
			queueEntity.setCancelBackRes(items.toDbString());
		}
		if (player.getLmjyState() == PState.GAMEING || player.getTBLYState() == TBLYState.GAMEING) {// 这两个副本中, 没有可落地队列
			queueEntity.setPersistable(false);
			queueEntity.setEntityType(EntityType.TEMPORARY);
		}
		// 推送队列
		if (reusage != GsConst.QueueReusage.FREE) {
			if (!HawkDBManager.getInstance().create(queueEntity)) {
				return null;
			}
			
			player.getData().addQueueEntity(queueEntity);
			QueuePB.Builder pushQueue = BuilderUtil.genQueueBuilder(queueEntity);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_ADD_PUSH_VALUE, pushQueue));
		}

		return queueEntity;
	}
	
	/**
	 * 刷新玩家所有队列,完成的执行完成操作
	 * @param player
	 * @param needPush
	 * @param isLogin 是否为登陆时调用
	 */
	public void refreshPlayerAllQueue(Player player, boolean needPush, boolean isLogin) {
		long now = HawkTime.getMillisecond();
		List<QueueEntity> deleteEntities = null;
		for (QueueEntity queueEntity : player.getData().getQueueEntities()) {
			// 空闲队列直接略过
			if (queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
				continue;
			}
			
			if (queueEntity.getEndTime() <= now) {
				finishOneQueue(player, queueEntity, needPush, false);
				if (deleteEntities == null) {
					deleteEntities = new ArrayList<QueueEntity>();
				}
				deleteEntities.add(queueEntity);
			}
		}
		
		if (deleteEntities == null) {
			return;
		}

		// 删除已完成的
		for (QueueEntity queueEntity : deleteEntities) {
			player.getData().removeQueue(queueEntity);
		}
	}

	/** 
	 * 完成一个队列 
	 * 
	 * @param player
	 * @param queueEntity
	 */
	public void finishOneQueue(Player player, QueueEntity queueEntity, boolean isImmediate) {
		if (queueEntity.getReusage() == QueueReusage.FREE.intValue() || queueEntity.getEndTime() > HawkTime.getMillisecond()) {
			return;
		}
		
		finishOneQueue(player, queueEntity, true, isImmediate);
		player.getData().removeQueue(queueEntity);
	}
	
	/**
	 * 取消一个队列
	 * @param player
	 * @param queueEntity
	 * @param guildId 
	 * @return
	 */
	public boolean cancelOneQueue(Player player, QueueEntity queueEntity) {
		if (queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
			return false;
		}
		
		HawkMsg msg = createCancelMsg(queueEntity, 0);
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		// 记录取消队列action日志
		BehaviorLogger.log4Service(player, Source.QUEUE, Action.PLAYER_QUEUE_CANCEL,
				Params.valueOf("queueType", queueEntity.getQueueType()),
				Params.valueOf("queueStatus", queueEntity.getStatus()),
				Params.valueOf("itemId", queueEntity.getItemId()),
				Params.valueOf("cancelBackRes", queueEntity.getCancelBackRes()),
				Params.valueOf("remainTime", (queueEntity.getEndTime() - HawkTime.getMillisecond()) / 1000));
		// 删除队列
		QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
		delete.setId(queueEntity.getId());
		delete.setQueueType(QueueType.valueOf(queueEntity.getQueueType()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
		player.getData().removeQueue(queueEntity);

		return true;
	}
	
	/**
	 * 完成队列
	 * @param player
	 * @param queueEntity
	 * @param needPush
	 * @param isImmediate
	 */
	private void finishOneQueue(Player player, QueueEntity queueEntity, boolean needPush, boolean isImmediate) {
		HawkMsg msg = createFinishMsg(player,queueEntity, isImmediate);
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		
		if(needPush) {
			// 通知客户端
			deleteQueueNotice(player, queueEntity);
		}
		
		// 记录队列完成action日志
		BehaviorLogger.log4Service(player, Source.QUEUE, Action.PLAYER_QUEUE_FINISH,
				Params.valueOf("queueType", queueEntity.getQueueType()),
				Params.valueOf("queueStatus", queueEntity.getStatus()),
				Params.valueOf("itemId", queueEntity.getItemId()),
				Params.valueOf("isImmediate", isImmediate));
	} 
	
	/**
	 * 删除队列通知
	 * @param player
	 * @param queueEntity
	 */
	public void deleteQueueNotice(Player player, QueueEntity queueEntity) {
		QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
		delete.setId(queueEntity.getId());
		delete.setQueueType(QueueType.valueOf(queueEntity.getQueueType()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_DELETE_PUSH_VALUE, delete));
	}
	
	/**
	 * 创建队列完成内部消息
	 * @param queueEntity
	 * @param isImmediate
	 * @return
	 */
	private HawkMsg createFinishMsg(Player player, QueueEntity queueEntity, boolean isImmediate) {
		if (queueEntity == null || queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
			return null;
		}

		switch (queueEntity.getQueueType()) {
		case QueueType.BUILDING_QUEUE_VALUE:
			return BuildingQueueFinishMsg.valueOf(queueEntity.getItemId(), queueEntity.getQueueType(), queueEntity.getStatus(),queueEntity.getMultiply());

		case QueueType.SCIENCE_QUEUE_VALUE:
			return TechQueueFinishMsg.valueOf(Integer.parseInt(queueEntity.getItemId()));

		case QueueType.SOILDER_QUEUE_VALUE:
		case QueueType.TRAP_QUEUE_VALUE:
		case QueueType.SOLDIER_ADVANCE_QUEUE_VALUE:
			return TrainQueueFinishMsg.valueOf(queueEntity.getItemId(), isImmediate);
		case QueueType.CURE_QUEUE_VALUE:
			return CureQueueFinishMsg.valueOf(queueEntity.getItemId(), isImmediate ? 0 : -1);			
		case QueueType.BUILDING_RECOVER_QUEUE_VALUE:
			return BuildingRecoverFinishMsg.valueOf();
		case QueueType.EQUIP_QUEUE_VALUE:
			return EquipQueueFinishMsg.valueOf(queueEntity.getItemId(), isImmediate);
		case QueueType.GUILD_HOSPICE_QUEUE_VALUE:
			return HospiceQueueFinishMsg.valueOf(queueEntity,player.getGuildId());
		case QueueType.YURISTRIKE_CLEAN_VALUE:
			return YuriStrikeCleanFinishMsg.valueOf(queueEntity);
		case QueueType.CROSS_TECH_QUEUE_VALUE:
			return CrossTechQueueFinishMsg.valueOf(Integer.parseInt(queueEntity.getItemId()));
		case QueueType.EQUIP_RESEARCH_QUEUE_VALUE:
			return EquipResearchQueueFinishMsg.valueOf(queueEntity.getItemId());
		case QueueType.PLANT_SCIENCE_QUEUE_VALUE:
			return PlantScienceQueueFinishMsg.valueOf(Integer.parseInt(queueEntity.getItemId()));
		case QueueType.CURE_PLANT_QUEUE_VALUE:
			return CurePlantQueueFinishMsg.valueOf(queueEntity.getItemId(), isImmediate ? 0 : -1);
		default:
			return null;
		}
	}

	/**
	 * 创建取消队列内部消息
	 * @param queueEntity
	 * @param protoType
	 * @param backPercent
	 * @return
	 */
	private HawkMsg createCancelMsg(QueueEntity queueEntity, int protoType) {
		if (queueEntity == null || queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
			return null;
		}
		switch (queueEntity.getQueueType()) {
		case QueueType.BUILDING_QUEUE_VALUE:
			return BuildingQueueCancelMsg.valueOf(queueEntity.getItemId(), queueEntity.getStatus(), queueEntity.getCancelBackRes());
		case QueueType.SCIENCE_QUEUE_VALUE:
			return CancelTechQueueMsg.valueOf(Integer.parseInt(queueEntity.getItemId()), queueEntity.getCancelBackRes());	
		case QueueType.SOILDER_QUEUE_VALUE:
		case QueueType.TRAP_QUEUE_VALUE:
		case QueueType.SOLDIER_ADVANCE_QUEUE_VALUE:
			return TrainQueueCancelMsg.valueOf(queueEntity.getItemId(), protoType, queueEntity.getCancelBackRes());
		case QueueType.CURE_QUEUE_VALUE:
			return CureQueueCancelMsg.valueOf(queueEntity.getCancelBackRes());
		case QueueType.EQUIP_QUEUE_VALUE:
			return EquipQueueCancelMsg.valueOf(queueEntity.getItemId(), queueEntity.getCancelBackRes());
		default:
			return null;
		}
	}
}
