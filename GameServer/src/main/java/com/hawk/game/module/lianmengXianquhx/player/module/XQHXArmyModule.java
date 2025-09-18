package com.hawk.game.module.lianmengXianquhx.player.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.db.entifytype.EntityType;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.PlayerArmyModule;
import com.hawk.game.module.PlayerQueueModule;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Army.HPAddSoldierReq;
import com.hawk.game.protocol.Army.HPCureSoldierReq;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

public class XQHXArmyModule extends PlayerModule {

	private IXQHXPlayer player;

	public XQHXArmyModule(IXQHXPlayer player) {
		super(player);
		this.player = player;
	}

	/** 训练士兵 */
	@ProtocolHandler(code = HP.code.ADD_SOLDIER_C_VALUE)
	private boolean onCreateSoldier(HawkProtocol protocol) {
		if (player.getParent().maShangOver()) {
			player.responseSuccess(protocol.getType());
			return true;
		}
		HPAddSoldierReq req = protocol.parseProtocol(HPAddSoldierReq.getDefaultInstance());
		final boolean immediate = req.getIsImmediate();
		if (!immediate) {
			return false;
		}

		PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
		if (armyModule == null) {
			return false;
		}
		boolean success = armyModule.onCreateSoldier(protocol);
		if (success) {
			int soldierCount = req.getSoldierCount();
			int armyId = req.getArmyId();
			RedisProxy.getInstance().jbsIncreaseCreateSoldier(player.getId(), armyId, soldierCount);
		}

		return true;
	}

	/**
	 * 队列加速
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_SPEED_UP_C_VALUE)
	private boolean onQueueSpeedUp(HawkProtocol protocol) {
		QueueSpeedUpReq req = protocol.parseProtocol(QueueSpeedUpReq.getDefaultInstance());
		QueueEntity queueEntity = player.getData().getQueueEntity(req.getId());
		if (Objects.isNull(queueEntity)) {
			return false;
		}
		PlayerQueueModule module = player.getModule(GsConst.ModuleType.QUEUE_MODULE);
		module.onQueueSpeedUp(protocol);
		return true;
	}

	/**
	 * 治疗伤兵
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CURE_SOLDIER_C_VALUE)
	private boolean onCureSoldier(HawkProtocol protocol) {
		if (player.getParent().maShangOver()) {
			player.responseSuccess(protocol.getType());
			return true;
		}

		if (player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE).size() > 0) {
			player.responseSuccess(protocol.getType());
			player.getPush().syncQueueEntityInfo();
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			return true;
		}

		HPCureSoldierReq req = protocol.parseProtocol(HPCureSoldierReq.getDefaultInstance());
		final List<ArmySoldierPB> cureList = req.getSoldiersList();
		final boolean immediate = req.getIsImmediate();
		// if (!immediate) { // 必须秒的
		// return false;
		// }
		for (ArmySoldierPB army : cureList) {
			int armyId = army.getArmyId();
			ArmyEntity armyEntity = player.getPlayerData().getArmyEntity(armyId);
			// 太能兵不在这里收治
			if (armyEntity == null || armyEntity.isPlantSoldier()) {
				return false;
			}

		}
		// 治疗伤兵需要的时间
		double recoverTime = GameUtil.recoverTime(player, cureList) / player.getParent().getCfg().getCureSpeedUp();
		// 治疗伤兵消耗资源
		List<ItemInfo> resCost = cureConsume(player, cureList, (int) Math.ceil(recoverTime), immediate, req.hasUseGold() ? req.getUseGold() : false, protocol.getType());
		if (resCost == null) {
			player.getPush().syncQueueEntityInfo();
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			return false;
		}

		// 治疗伤兵
		Map<Integer, Integer> armyIds = cureSoldier(player, cureList, immediate);
		if (!immediate) {
			addQueue(player, QueueType.CURE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					req.getBuildingUUID(), Const.BuildingType.HOSPITAL_STATION_VALUE, HawkTime.getMillisecond(), recoverTime * 1000, resCost, GsConst.QueueReusage.SOLDIER_CURE);
		}
		player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));

		if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
			GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
		}

		player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
		player.responseSuccess(protocol.getType());
		return true;
	}

	private QueueEntity addQueue(IXQHXPlayer player, int queueType, int queueStatus, String itemId,
			int buildingType, long startTime, double costTime, List<ItemInfo> cancelBackRes, GsConst.QueueReusage reusage) {
		QueueEntity queueEntity = new QueueEntity();
		///////////////////
		queueEntity.setPersistable(false);
		queueEntity.setEntityType(EntityType.TEMPORARY);
		/////////////////////
		queueEntity.setId(HawkOSOperator.randomUUID());
		queueEntity.setEndTime(startTime + (long) Math.ceil(costTime / 1000) * 1000);
		queueEntity.setItemId(itemId);
		queueEntity.setPlayerId(player.getId());
		queueEntity.setQueueType(queueType);
		queueEntity.setStartTime(startTime);
		queueEntity.setTotalQueueTime(queueEntity.getEndTime() - startTime);
		queueEntity.setBuildingType(buildingType);
		queueEntity.setStatus(queueStatus);
		queueEntity.setReusage(reusage.intValue()); // 可重用队列
		if (cancelBackRes != null && cancelBackRes.size() > 0) {
			AwardItems items = AwardItems.valueOf();
			items.addItemInfos(cancelBackRes);
			queueEntity.setCancelBackRes(items.toDbString());
		}
		// 推送队列
		player.getData().addQueueEntity(queueEntity);
		QueuePB.Builder pushQueue = BuilderUtil.genQueueBuilder(queueEntity);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_ADD_PUSH_VALUE, pushQueue));

		return queueEntity;
	}

	private List<ItemInfo> cureConsume(IXQHXPlayer player, List<ArmySoldierPB> cureList, int recoverTime, boolean immediate, boolean useGold, int hpCode) {
		// List<ItemInfo> itemInfos = GameUtil.cureItems(player, cureList);
		ConsumeItems consume = ConsumeItems.valueOf();
		// consume.addConsumeInfo(itemInfos, immediate || useGold);
		// 立即治疗
		if (immediate) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(recoverTime, SpeedUpTimeWeightType.TIME_WEIGHT_CURESOLDIER));
		}

		if (!consume.checkConsume(player, hpCode)) {
			return null;
		}

		AwardItems realCostItems = consume.consumeAndPush(player, Action.SOLDIER_TREATMENT);
		return realCostItems.getAwardItems();
	}

	/**
	 * 开始治疗伤兵
	 * 
	 * @param cureList
	 * @param immediate
	 * @return
	 */
	private Map<Integer, Integer> cureSoldier(IXQHXPlayer player, List<ArmySoldierPB> cureList, boolean immediate) {
		Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
		// 立即治疗士兵数量
		for (ArmySoldierPB army : cureList) {
			int armyId = army.getArmyId();
			ArmyEntity armyEntity = player.getPlayerData().getArmyEntity(armyId);
			int count = Math.min(army.getCount(), armyEntity.getWoundedCount());
			armyIds.put(armyId, count);
			ArmySection section = ArmySection.CURE;
			if (immediate) {
				armyEntity.addWoundedCount(-count);
				// 立即治疗或道具加速完成不用等待领取，直接回到兵营
				armyEntity.addFree(count);
				section = ArmySection.FREE;
			} else {
				armyEntity.addWoundedCount(-count);
				armyEntity.addCureCount(count);
			}

			LogUtil.logArmyChange(player, armyEntity, count, section, ArmyChangeReason.CURE);
		}

		return armyIds;
	}

	/**
	 * 治疗伤兵
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.CURE_PLANT_SOLDIER_C_VALUE)
	private boolean onCurePlantSoldier(HawkProtocol protocol) {
		if (player.getParent().maShangOver()) {
			player.responseSuccess(protocol.getType());
			return true;
		}

		if (player.getData().getQueueEntitiesByType(QueueType.CURE_PLANT_QUEUE_VALUE).size() > 0) {
			player.responseSuccess(protocol.getType());
			player.getPush().syncQueueEntityInfo();
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			return true;
		}

		HPCureSoldierReq req = protocol.parseProtocol(HPCureSoldierReq.getDefaultInstance());
		final List<ArmySoldierPB> cureList = req.getSoldiersList();
		final boolean immediate = req.getIsImmediate();
		// if (!immediate) { // 必须秒的
		// return false;
		// }
		for (ArmySoldierPB army : cureList) {
			int armyId = army.getArmyId();
			ArmyEntity armyEntity = player.getPlayerData().getArmyEntity(armyId);
			// 普通兵不在这里收治
			if (armyEntity == null || !armyEntity.isPlantSoldier()) {
				return false;
			}

		}
		// 治疗伤兵需要的时间
		double recoverTime = GameUtil.plantRecoverTime(player, cureList) / player.getParent().getCfg().getCureSpeedUp();
		// 治疗伤兵消耗资源
		List<ItemInfo> resCost = cureConsume(player, cureList, (int) Math.ceil(recoverTime), immediate, req.hasUseGold() ? req.getUseGold() : false, protocol.getType());
		if (resCost == null) {
			player.getPush().syncQueueEntityInfo();
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			return false;
		}

		// 治疗伤兵
		Map<Integer, Integer> armyIds = cureSoldier(player, cureList, immediate);
		if (!immediate) {
			addQueue(player, QueueType.CURE_PLANT_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					req.getBuildingUUID(), Const.BuildingType.PLANT_HOSPITAL_VALUE, HawkTime.getMillisecond(), recoverTime * 1000, resCost,
					GsConst.QueueReusage.PLANT_SOLDIER_CURE);
		}
		player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));

		if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
			GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.COMMON);
		}

		player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
		player.responseSuccess(protocol.getType());
		return true;
	}

}
