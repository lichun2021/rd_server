package com.hawk.game.module.dayazhizhan.battleroom.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.db.entifytype.EntityType;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.PlayerQueueModule;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZProtocol;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBattleCfg;
import com.hawk.game.module.dayazhizhan.battleroom.order.DYZZOrder;
import com.hawk.game.module.dayazhizhan.battleroom.order.DYZZOrderCollection;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayerEffect;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZOutTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state.DYZZTowerStateCuiHuiZhong;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Army.HPCureSoldierReq;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.DYZZ.PBDYZZCuiHuiTowerReq;
import com.hawk.game.protocol.DYZZ.PBDYZZItemBuyReq;
import com.hawk.game.protocol.DYZZ.PBDYZZItemGetReq;
import com.hawk.game.protocol.DYZZ.PBDYZZOrderSyncResp;
import com.hawk.game.protocol.DYZZ.PBDYZZOrderUseReq;
import com.hawk.game.protocol.DYZZ.PBDYZZRogueSelectReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

public class DYZZArmyModule extends DYZZBattleRoomModule {

	public DYZZArmyModule(DYZZBattleRoom appObj) {
		super(appObj);
	}

	/** 领取免费加速 */
	@ProtocolHandler(code = HP.code2.DYZZ_ITEM_GET_REQ_VALUE)
	private void onDYZZ_ITEM_GET_REQ(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();

		PBDYZZItemGetReq req = protocol.parseProtocol(PBDYZZItemGetReq.getDefaultInstance());
		DYZZBattleCfg bacfg = getParent().getCfg();
		DYZZPlayerEffect effect = (DYZZPlayerEffect) player.getEffect();
		if (player.isSpeedItemFree() && req.getItemId() == bacfg.getSpeedupItem() && effect.getSource().getEffVal(EffType.LIFE_TIME_CARD_647) > 0) {
			player.setSpeedItemFree(false);
			AwardItems extraAward = AwardItems.valueOf();
			extraAward.addItem(new ItemInfo(ItemType.TOOL_VALUE, bacfg.getSpeedupItem(), bacfg.getSpeedupItemFree()));
			extraAward.rewardTakeAffectAndPush(player, Action.LIFETIME_SPEED_BUY, true);
		}
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
	}

	/** 购买加速 */
	@ProtocolHandler(code = HP.code2.DYZZ_ITEM_BUY_REQ_VALUE)
	private void onDYZZ_ITEM_BUY_REQ(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();

		PBDYZZItemBuyReq req = protocol.parseProtocol(PBDYZZItemBuyReq.getDefaultInstance());
		DYZZBattleCfg bacfg = getParent().getCfg();
		DYZZPlayerEffect effect = (DYZZPlayerEffect) player.getEffect();
		if (req.getItemId() == bacfg.getSpeedupItem() && player.getSpeedItemBuyCnt() + req.getItemCnt() <= bacfg.getSpeedupItemCnt()
				&& effect.getSource().getEffVal(EffType.LIFE_TIME_CARD_647) > 0) {
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			List<ItemInfo> cost = ItemInfo.valueListOf(bacfg.getSpeedupItemCost());
			cost.forEach(it -> it.setCount(it.getCount() * req.getItemCnt()));
			consumeItems.addConsumeInfo(cost);

			// 判断消耗是否满足
			if (!consumeItems.checkConsume(player, protocol.getType())) {
				return;
			}
			consumeItems.consumeAndPush(player, Action.LIFETIME_SPEED_BUY);
			
			player.setSpeedItemBuyCnt(player.getSpeedItemBuyCnt() + req.getItemCnt());
			AwardItems extraAward = AwardItems.valueOf();
			extraAward.addItem(new ItemInfo(ItemType.TOOL_VALUE, bacfg.getSpeedupItem(), req.getItemCnt()));
			extraAward.rewardTakeAffectAndPush(player, Action.LIFETIME_SPEED_BUY, true);
			player.setSpeedItemFree(false);
		}
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
	}

	/** 摧毁 */
	@ProtocolHandler(code = HP.code2.DYZZ_DESTROY_TOWER_C_VALUE)
	private void onCuiHui(DYZZProtocol protocol) {
		if (getParent().getCurTimeMil() < getParent().getBattleStartTime()) {
			return;
		}
		PBDYZZCuiHuiTowerReq req = protocol.parseProtocol(PBDYZZCuiHuiTowerReq.getDefaultInstance());
		IDYZZPlayer player = protocol.getPlayer();
		IDYZZWorldPoint point = getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (!(point instanceof DYZZOutTower)) {
			return;
		}
		DYZZOutTower tower = (DYZZOutTower) point;
		if (tower.getBornCamp() == tower.getOnwerCamp()) {
			return;
		}
		if (tower.getBornCamp() == player.getCamp()) {
			return;
		}
		if (tower.getLeaderMarch() == null) {
			return;
		}

		tower.setStateObj(new DYZZTowerStateCuiHuiZhong(tower));
		player.responseSuccess(protocol.getType());
	}

	/** 选rogue */
	@ProtocolHandler(code = HP.code2.DYZZ_ROGUE_SELECT_C_VALUE)
	private void onRogueSelect(DYZZProtocol protocol) {
		PBDYZZRogueSelectReq req = protocol.parseProtocol(PBDYZZRogueSelectReq.getDefaultInstance());
		IDYZZPlayer player = protocol.getPlayer();
		player.getRogueCollec().select(req.getIndex(), req.getRogueId());
		player.getRogueCollec().notifyChange();
		player.responseSuccess(protocol.getType());
	}

	/** 使用号令 */
	@ProtocolHandler(code = HP.code2.DYZZ_ORDER_USE_C_VALUE)
	private boolean onUseOrder(DYZZProtocol protocol) {
		PBDYZZOrderUseReq req = protocol.parseProtocol(PBDYZZOrderUseReq.getDefaultInstance());
		IDYZZPlayer player = protocol.getPlayer();
		// // R4盟主队长可以
		// boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		// if (!guildAuthority) {
		// player.sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE ,0);
		// return false;
		// }
		// 消耗
		DYZZOrderCollection orderCollection = battleRoom.getDYZZOrderCollection(player.getCamp());
		if (orderCollection == null) {
			return true;
		}
		int orderId = req.getOrderId();
		int rlt = orderCollection.canStartOrder(orderId);
		if (rlt > 0) {
			player.sendError(protocol.getType(), rlt, 0);
			return true;
		}
		DYZZOrder order = orderCollection.getOrder(orderId);
		// 消耗
		int costPower = order.getPowerCost();
		if (player.getCamp() == DYZZCAMP.A) {
			battleRoom.campAOrder -= costPower;
		}
		if (player.getCamp() == DYZZCAMP.B) {
			battleRoom.campBOrder -= costPower;
		}
		order.startOrder();
		orderCollection.notifyChange();
		PBDYZZOrderSyncResp.Builder syncBuilder = orderCollection.genPBDYZZOrderSyncRespBuilder();
		List<IDYZZPlayer> plist = battleRoom.getCampPlayers(player.getCamp());
		for (IDYZZPlayer member : plist) {
			member.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ORDER_SYNC_S_VALUE, syncBuilder));
		}
		player.responseSuccess(protocol.getType());
		// //广播战队
		// Const.NoticeCfgId noticeId = Const.NoticeCfgId.DYZZ_MONSTER_ORDER;
		// ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(noticeId)
		// .addParms(player.getCamp().intValue()).addParms(player.getName(),req.getOrderId()).build();
		// getParent().addWorldBroadcastMsg(parames);
		// 广播战场
		ChatParames paramesBroad = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_MONSTER_ORDER)
				.addParms(player.getCamp().intValue()).addParms(player.getName(), req.getOrderId()).build();
		getParent().addWorldBroadcastMsg(paramesBroad);
		// Tlog
		LogUtil.logDYZZUseOrder(player, battleRoom.getId(), player.getDYZZGuildId(), player.getGuildName(), orderId, costPower);
		return true;
	}

	// /** 训练士兵 */
	// @ProtocolHandler(code = HP.code.ADD_SOLDIER_C_VALUE)
	// private boolean onCreateSoldier(DYZZProtocol protocol) {
	// IDYZZPlayer player = protocol.getPlayer();
	// if (battleRoom.maShangOver()) {
	// player.responseSuccess(protocol.getType());
	// return true;
	// }
	// HPAddSoldierReq req = protocol.parseProtocol(HPAddSoldierReq.getDefaultInstance());
	// final boolean immediate = req.getIsImmediate();
	// if (!immediate) {
	// return false;
	// }
	//
	// PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
	// if (armyModule == null) {
	// return false;
	// }
	// boolean success = armyModule.onCreateSoldier(protocol);
	// if (success) {
	// int soldierCount = req.getSoldierCount();
	// int armyId = req.getArmyId();
	// RedisProxy.getInstance().jbsIncreaseCreateSoldier(player.getId(), armyId, soldierCount);
	// }
	//
	// return true;
	// }

	/**
	 * 队列加速
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_SPEED_UP_C_VALUE)
	private boolean onQueueSpeedUp(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
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
	private boolean onCureSoldier(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		if (battleRoom.maShangOver()) {
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

		// 治疗伤兵需要的时间
		double recoverTime = GameUtil.recoverTime(player, cureList) / getParent().getCfg().getCureSpeedUp();
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
					req.getBuildingUUID(), Const.BuildingType.HOSPITAL_STATION_VALUE, getParent().getCurTimeMil(), recoverTime * 1000, resCost, GsConst.QueueReusage.SOLDIER_CURE);
		}
		player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));

		if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
			GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
		}

		player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
		player.responseSuccess(protocol.getType());
		return true;
	}

	private QueueEntity addQueue(IDYZZPlayer player, int queueType, int queueStatus, String itemId,
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

	private List<ItemInfo> cureConsume(IDYZZPlayer player, List<ArmySoldierPB> cureList, int recoverTime, boolean immediate, boolean useGold, int hpCode) {
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
	private Map<Integer, Integer> cureSoldier(IDYZZPlayer player, List<ArmySoldierPB> cureList, boolean immediate) {
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
	private boolean onCurePlantSoldier(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		if (battleRoom.maShangOver()) {
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
		double recoverTime = GameUtil.plantRecoverTime(player, cureList) / getParent().getCfg().getCureSpeedUp();
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
					req.getBuildingUUID(), Const.BuildingType.PLANT_HOSPITAL_VALUE, getParent().getCurTimeMil(), recoverTime * 1000, resCost,
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
