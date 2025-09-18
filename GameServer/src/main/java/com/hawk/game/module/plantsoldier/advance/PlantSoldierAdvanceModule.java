package com.hawk.game.module.plantsoldier.advance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.plantsoldier.advance.msg.PlantSoldierAdvanceQueueSpeedUpEvent;
import com.hawk.game.module.plantsoldier.cfg.PlantSoldierConstKVCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierAdvanceAddReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierAdvanceReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierAdvanceSync;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierCollectResp;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.PlantSoldierAdvanceEvent;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

public class PlantSoldierAdvanceModule extends PlayerModule {

	public PlantSoldierAdvanceModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {

		fixOldData();
		syncPlantFactoryInfo();
		return true;
	}

	/** 老数据需要兼容*/
	private void fixOldData() {
		try {
			PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
			if (factory.getCollectArmy() > 0 && factory.getAdvanceTotal() == 0) {
				ArmyEntity collectArmy = getPlayerData().getArmyEntity(factory.getCollectArmy()); // 收取
				BattleSoldierCfg toarmyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, factory.getCollectArmy());
				List<ItemInfo> cost = trainCost(player, toarmyCfg, collectArmy.getTrainCount());

				factory.setAdvanceTotal(collectArmy.getTrainCount());
				factory.setResTotal(ItemInfo.toString(cost));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public boolean onTick() {
		return true;
	}

	private void syncPlantFactoryInfo() {
		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		int advanceSpeed = (int) getAdvanceSpeed(factory);
		PBPlantSoldierAdvanceSync.Builder info = PBPlantSoldierAdvanceSync.newBuilder()
				.setAdvanceArmyId(factory.getAdvanceArmy())
				.setCollectArmyId(factory.getCollectArmy())
				.setMaxStroe(maxStroe(factory))
				.setMaxAdvance(maxAdvance(factory))
				.setCollectOneUseMil(advanceSpeed)
				.setZeroStoreTime((long) (factory.getLastResStoreTime() - factory.getResStore() * advanceSpeed))
				.setAdvanceTotal(factory.getAdvanceTotal())
				.setResTotal(factory.getResTotal());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PLANT_SOLDIER_ADVANCE_SYNC, info));

	}

	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_ADVANCE_CANCEL_C_VALUE)
	private void onCancelAdvance(HawkProtocol protocol) {
		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		if (factory.getCollectArmy() == 0) {
			return;
		}
		PlantSoldierConstKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierConstKVCfg.class);
		ArmyEntity advanceArmy = getPlayerData().getArmyEntity(factory.getAdvanceArmy()); // 消耗兵
		ArmyEntity collectArmy = getPlayerData().getArmyEntity(factory.getCollectArmy()); // 收取

		int rstor = (int) resTore(factory); // 训练完成的
		int canlNum = collectArmy.getTrainCount() - rstor; // 退回的
		if (canlNum <= 0) { // 无可取消
			return;
		}

		advanceArmy.addFree(canlNum);
		collectArmy.setTrainCount(collectArmy.getTrainCount() - canlNum);

		LogUtil.logArmyChange(player, advanceArmy, canlNum, ArmySection.FREE, ArmyChangeReason.PLANT_ADVANCE_CANCEL);
		LogUtil.logArmyChange(player, collectArmy, canlNum, ArmySection.ADVANCE, ArmyChangeReason.PLANT_ADVANCE_CANCEL);
		GameUtil.soldierAddRefresh(player, advanceArmy.getArmyId(), canlNum);
		player.getPush().syncArmyInfo(ArmyChangeCause.TRAIN_CANCEL, advanceArmy.getArmyId());
		List<ItemInfo> resTotal = ItemInfo.valueListOf(factory.getResTotal());
		double pct = canlNum * 1D / factory.getAdvanceTotal();
		for (ItemInfo item : resTotal) {
			if (item.getItemType() == ItemType.PLAYER_ATTR) { // 资源再打5折
				item.setCount((long) (item.getCount() * pct * kvcfg.getCancelEvolutionRes() * GsConst.EFF_PER));
			} else {
				item.setCount((long) (item.getCount() * pct * kvcfg.getCancelEvolutionItem() * GsConst.EFF_PER));
			}
		}

		if (collectArmy.getTrainCount() == 0) { // 训练完成
			factory.setCollectArmy(0);
			factory.setAdvanceArmy(0);
			factory.setAdvanceStart(0);
			factory.setAdvanceEnd(0);
		}
		// 当前版本取消为全部取消.
		factory.setAdvanceTotal(0);
		factory.setResTotal("");

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(resTotal);
		awardItem.rewardTakeAffectAndPush(player, Action.TRAIN_SOLDIER_CANCEL);

		resTore(factory);
		updateQueue(factory);
		syncPlantFactoryInfo();
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_ADVANCE_C_VALUE)
	private void onAdvance(HawkProtocol protocol) {
		PBPlantSoldierAdvanceReq req = protocol.parseProtocol(PBPlantSoldierAdvanceReq.getDefaultInstance());
		final int toArmyId = req.getCollectArmyId();// = 2; // 目标兵种
		final int soldierCount = req.getSoldierCount(); // = 3;
		final boolean isImmediate = req.getImmediate() > 0;
		HawkAssert.checkPositive(soldierCount);

		PlantSoldierConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierConstKVCfg.class);
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(player, cfg.getEvolutionFrontBuildIds(), null, protocol.getType())) {
			return;
		}

		BattleSoldierCfg toarmyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, toArmyId);
		if (Objects.isNull(toarmyCfg) || !toarmyCfg.isPlantSoldier()) {
			return;
		}

		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		if (soldierCount > maxAdvance(factory, toarmyCfg)) {
			return;
		}
		if (factory.getCollectArmy() > 0 && toArmyId != factory.getCollectArmy()) {
			return;
		}

		boolean poyiMax = player.getPlantSoldierSchool().getSoldierCrackByType(toarmyCfg.getSoldierType()).isMax();
		if (!poyiMax) {
			HawkLog.errPrintln("PBPlantSoldierAdvanceReq error SoldierCrack not unlock toArmyId:{}", toArmyId);
			return;
		}

		ArmyEntity armyEntity = getPlayerData().getArmyEntity(toarmyCfg.getAdvanceArmy()); // 消耗兵
		if (Objects.isNull(armyEntity) || armyEntity.getFree() < soldierCount) {
			return;
		}

		// 资源消耗作用加成
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(trainCost(player, toarmyCfg, soldierCount), req.getUseGold() > 0 || isImmediate);
		// 立即训练
		if (isImmediate) {
			long trainTime = (long) Math.ceil((soldierCount * getCollectOneuseMil(toArmyId)));
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(trainTime / 1000, SpeedUpTimeWeightType.TIME_WEIGHT_TRAINSOLDIER));
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}

		consume.consumeAndPush(player, Action.PLANT_SOLDIER_ADVANCE);

		// 扣兵
		armyEntity.addFree(-soldierCount);
		player.getPush().syncArmyInfo(ArmyChangeCause.FIRE, armyEntity.getArmyId());
		LogUtil.logArmyChange(player, armyEntity, soldierCount, ArmySection.FREE, ArmyChangeReason.PLANT_ADVANCE_COST);

		if (isImmediate) {
			advanceSoldier(factory, toarmyCfg, soldierCount);
			factory.setLastResStoreTime(0);
			onCollectFactory(null);

			// ArmyEntity toarmyEntity = getPlayerData().getArmyEntity(toArmyId);
			// toarmyEntity.addFree(soldierCount);
			// GameUtil.soldierAddRefresh(player, toArmyId, soldierCount);
			// player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
			// player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, toArmyId);
			// LogUtil.logArmyChange(player, toarmyEntity, soldierCount, ArmySection.FREE, ArmyChangeReason.PLANT_ADVANCE_COLLECT);
		} else {
			advanceSoldier(factory, toarmyCfg, soldierCount);
			syncPlantFactoryInfo();
			updateQueue(factory);
		}
		player.responseSuccess(protocol.getType());

		MissionManager.getInstance().postMsg(player, new PlantSoldierAdvanceEvent());
	}

	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_ADVANCE_ADD_C_VALUE)
	private void onAddAdvanceSolider(HawkProtocol protocol) {
		PBPlantSoldierAdvanceAddReq req = protocol.parseProtocol(PBPlantSoldierAdvanceAddReq.getDefaultInstance());
		final int toArmyId = req.getArmyId(); // 目标兵种
		int itemId = req.getItemId();
		int itemCount = req.getItemCount();
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (itemCfg.getItemType() != Const.ToolType.TRAIN_PLANT_ADD_ONCE_VALUE) {
			return;
		}

		PlantSoldierConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierConstKVCfg.class);
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(player, cfg.getEvolutionFrontBuildIds(), null, protocol.getType())) {
			return;
		}

		BattleSoldierCfg toarmyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, toArmyId);
		if (Objects.isNull(toarmyCfg) || !toarmyCfg.isPlantSoldier()) {
			return;
		}
		boolean poyiMax = player.getPlantSoldierSchool().getSoldierCrackByType(toarmyCfg.getSoldierType()).isMax();
		if (!poyiMax) {
			HawkLog.errPrintln("PBPlantSoldierAdvanceReq error SoldierCrack not unlock toArmyId:{}", toArmyId);
			return;
		}
		int soldierCount = itemCfg.getNum() * itemCount;
		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		if (factory.getAdvanceArmy() != 0 && factory.getAdvanceArmy() != toarmyCfg.getAdvanceArmy()) {
			return;
		}
		ArmyEntity armyEntity = getPlayerData().getArmyEntity(toarmyCfg.getAdvanceArmy()); // 消耗兵
		if (Objects.isNull(armyEntity) || armyEntity.getFree() < soldierCount) {
			player.sendError(protocol.getType(), Status.Error.PLANT_SOLDIER_ADVANCE_ARMY_LESS_VALUE, 0);
			return;
		}

		List<ItemInfo> costList = new ArrayList<>();
		// 征召令消耗
		ItemInfo trainItem = new ItemInfo(ItemType.TOOL_VALUE, itemId, itemCount);
		costList.add(trainItem);
		// 资源消耗
		List<ItemInfo> trainCostList = trainCost(player, toarmyCfg, soldierCount);
		if (trainCostList != null) {
			costList.addAll(trainCostList);
		}
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(costList, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.PLANT_SOLDIER_ADVANCE);
		// 扣兵
		armyEntity.addFree(-soldierCount);
		player.getPush().syncArmyInfo(ArmyChangeCause.FIRE, armyEntity.getArmyId());
		LogUtil.logArmyChange(player, armyEntity, soldierCount, ArmySection.FREE, ArmyChangeReason.PLANT_ADVANCE_COST);
		// 添加训练
		advanceSoldier(factory, toarmyCfg, soldierCount);
		syncPlantFactoryInfo();
		updateQueue(factory);
		player.responseSuccess(protocol.getType());
		MissionManager.getInstance().postMsg(player, new PlantSoldierAdvanceEvent());
	}

	/**
	 * 收资源
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_COLLECT_C_VALUE)
	private void onCollectFactory(HawkProtocol protocol) {

		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		double store = resTore(factory);
		if (store < 1) {
			return;
		}
		int count = (int) store; // 取走的量
		ArmyEntity armyEntity = getPlayerData().getArmyEntity(factory.getCollectArmy());

		armyEntity.setTrainCount(armyEntity.getTrainCount() - count);
		armyEntity.addFree(count);
		double power = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, factory.getAdvanceArmy()).getPower();
		armyEntity.setAdvancePower(power * armyEntity.getTrainCount());

		factory.setResStore(store - count);

		if (armyEntity.getTrainCount() == 0) { // 训练完成
			factory.setCollectArmy(0);
			factory.setAdvanceArmy(0);
			factory.setAdvanceTotal(0);
			factory.setResTotal("");
			factory.setAdvanceStart(0);
			factory.setAdvanceEnd(0);
		}

		GameUtil.soldierAddRefresh(player, armyEntity.getArmyId(), count);
		LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.PLANT_ADVANCE_COLLECT);

		player.refreshPowerElectric(PowerChangeReason.TRAIN_SOLDIER);
		syncPlantFactoryInfo();
		updateQueue(factory);

		player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, armyEntity.getArmyId());

		PBPlantSoldierCollectResp.Builder resp = PBPlantSoldierCollectResp.newBuilder();
		resp.setArmyId(armyEntity.getArmyId());
		resp.setCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PLANT_SOLDIER_COLLECT_S, resp));

	}

	private void updateQueue(PlantSoldierAdvanceEntity factory) {
		long startTime = factory.getAdvanceStart();
		long entTime = factory.getAdvanceEnd();
		// if (entTime <= factory.getLastResStoreTime()) {
		// return;
		// }

		QueueEntity queue = player.getData().getQueueByBuildingType(BuildingType.PLANT_ADVANCE_VALUE);

		if (Objects.isNull(queue)) {
			queue = QueueService.getInstance().addReusableQueue(player,
					QueueType.PLANT_ADVANCE_QUEUE_VALUE,
					QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					factory.getCollectArmy() + "",
					BuildingType.PLANT_ADVANCE_VALUE,
					entTime - startTime,
					null,
					GsConst.QueueReusage.PLANT_SOLDIER_ADVANCE);
		}

		queue.setStartTime(startTime);
		queue.setEndTime(entTime);
		queue.setTotalReduceTime(0);
		queue.setTotalQueueTime(queue.getEndTime() - startTime);

		if (entTime <= GsApp.getInstance().getCurrentTime()) {
			QueueService.getInstance().finishOneQueue(player, queue, true);
		} else {
			QueuePB.Builder update = BuilderUtil.genQueueBuilder(queue);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_UPDATE_PUSH_VALUE, update));
		}
	}

	/**
	* 开始晋升
	*/
	private ArmyEntity advanceSoldier(PlantSoldierAdvanceEntity factory, BattleSoldierCfg toarmyCfg, int count) {
		resTore(factory);
		int toarmyId = toarmyCfg.getId();
		ArmyEntity armyEntity = getPlayerData().getArmyEntity(toarmyId);
		if (armyEntity == null) {
			armyEntity = new ArmyEntity();
			armyEntity.setPlayerId(player.getId());
			armyEntity.setArmyId(toarmyId);
			if (!HawkDBManager.getInstance().create(armyEntity)) {
				return null;
			}
			getPlayerData().addArmyEntity(armyEntity);
		}
		armyEntity.setTrainCount(count + armyEntity.getTrainCount());

		double power = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, toarmyCfg.getAdvanceArmy()).getPower();
		armyEntity.setAdvancePower(power * armyEntity.getTrainCount());

		factory.setAdvanceArmy(toarmyCfg.getAdvanceArmy());
		factory.setCollectArmy(toarmyCfg.getId());

		if (factory.getAdvanceStart() == 0) {
			factory.setAdvanceStart(HawkTime.getMillisecond());
		}
		factory.setAdvanceEnd((long) (Math.max(factory.getAdvanceStart(), factory.getAdvanceEnd()) + count * getCollectOneuseMil(toarmyId)));
		factory.setAdvanceTotal(factory.getAdvanceTotal() + count);
		AwardItems res = AwardItems.valueOf(factory.getResTotal());
		res.addItemInfos(trainCost(player, toarmyCfg, count));
		factory.setResTotal(res.toString());

		LogUtil.logArmyChange(player, armyEntity, count, count, 0, ArmySection.ADVANCE, ArmyChangeReason.PLANT_ADVANCE);

		return armyEntity;
	}

	public static List<ItemInfo> trainCost(Player player, BattleSoldierCfg armyCfg, int count) {
		List<ItemInfo> resList = ItemInfo.valueListOf(armyCfg.getPlantSoldierRes());
		resList.forEach(item -> item.setCount((long) Math.ceil(item.getCount() * count / 1000d)));
		// 资源消耗作用加成
		int[] goldArr = player.getEffect().getEffValArr(EffType.EFF_1473, EffType.EFF_1474,EffType.PLANT_SOLDIER_4129);
		int[] oillArr = player.getEffect().getEffValArr(EffType.EFF_1473, EffType.EFF_1475,EffType.PLANT_SOLDIER_4129);
		int[] tombArr = player.getEffect().getEffValArr(EffType.EFF_1473, EffType.EFF_1476,EffType.PLANT_SOLDIER_4129);
		int[] stelArr = player.getEffect().getEffValArr(EffType.EFF_1473, EffType.EFF_1477,EffType.PLANT_SOLDIER_4129);
		int[] medal = player.getEffect().getEffValArr(EffType.BLACK_TECH_367813,EffType.PLANT_SOLDIER_4129);
		GameUtil.reduceByEffect(resList, goldArr, oillArr, tombArr, stelArr, medal);
		return resList;
	}

	/**计算并更新当前储量*/
	private double resTore(PlantSoldierAdvanceEntity factory) {
		long passTime = GsApp.getInstance().getCurrentTime() - factory.getLastResStoreTime();

		double collectOneUseMil = getAdvanceSpeed(factory);

		double store = passTime / collectOneUseMil + factory.getResStore();

		store = Math.min(store, maxStroe(factory));
		if (GsApp.getInstance().getCurrentTime() > factory.getAdvanceEnd()) {
			store = maxStroe(factory);
		}
		
		factory.setResStore(store);
		factory.setLastResStoreTime(GsApp.getInstance().getCurrentTime());
		return factory.getResStore();
	}

	private double getAdvanceSpeed(PlantSoldierAdvanceEntity factory) {
		if (factory.getCollectArmy() == 0) {
			return Integer.MAX_VALUE;
		}
		double training = maxStroe(factory) - factory.getResStore();
		if (factory.getAdvanceEnd() == 0 || factory.getAdvanceStart() == 0) {
			double speed = getCollectOneuseMil(factory.getCollectArmy());
			factory.setAdvanceEnd((long) (training * speed + factory.getLastResStoreTime()));
			factory.setAdvanceStart((long) (factory.getAdvanceEnd() - factory.getAdvanceTotal() * speed));
		}
		if (training == 0) {
			return Integer.MAX_VALUE;
		}

		double result = (factory.getAdvanceEnd() - factory.getLastResStoreTime()) / (maxStroe(factory) - factory.getResStore());
		return Math.max(1, result);
	}

	private double getCollectOneuseMil(int armyId) {

		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		double time = armyCfg.getTime() * 1000d;

		int black = player.getEffect().getEffVal(Const.EffType.EFF_1478) + player.getEffect().getEffVal(Const.EffType.EFF_522);
		int eff = player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4128);

		SoldierType type = SoldierType.XXXXXXXXXXXMAN;
		if (Objects.nonNull(armyCfg)) {
			type = armyCfg.getSoldierType();
		}

		switch (type) {
		case TANK_SOLDIER_2:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4118);
			break;
		case TANK_SOLDIER_1:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4119);
			break;
		case PLANE_SOLDIER_4:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4120);
			break;
		case PLANE_SOLDIER_3:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4121);
			break;
		case FOOT_SOLDIER_6:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4122);
			break;
		case FOOT_SOLDIER_5:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4123);
			break;
		case CANNON_SOLDIER_8:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4124);
			break;
		case CANNON_SOLDIER_7:
			eff += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4125);
			break;
		default:
			break;
		}

		time = time * (1 - black * GsConst.EFF_PER) / (1 + eff * GsConst.EFF_PER);
		return Math.ceil(time);
	}

	/**储量上限, 取决于army的traincount*/
	private int maxStroe(PlantSoldierAdvanceEntity factory) {
		if (factory.getCollectArmy() == 0) {
			return 0;
		}

		ArmyEntity armyEntity = getPlayerData().getArmyEntity(factory.getCollectArmy());
		return armyEntity.getTrainCount();
	}

	@MessageHandler
	private void onSpeedUpMsg(PlantSoldierAdvanceQueueSpeedUpEvent msg) {
		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
		long upTime = msg.getUpTime();
		if (upTime < 0) { // 金币立即完成
			upTime = Integer.MAX_VALUE;
		}
		upTime += 1000;
		double storedAdd = upTime / getAdvanceSpeed(factory);

		double store = Math.min(maxStroe(factory), factory.getResStore() + storedAdd);
		factory.setAdvanceEnd(factory.getAdvanceEnd() - upTime);
		factory.setResStore(store);
		syncPlantFactoryInfo();
		if (store == maxStroe(factory)) {
			onCollectFactory(null);
		} else {
			updateQueue(factory);
		}
	}

	/** 最多再进化多少*/
	private int maxAdvance(PlantSoldierAdvanceEntity factory) {
		PlantSoldierConstKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierConstKVCfg.class);
		int max = cfg.getEvolutionNumber(); // 如果有做用号加量, 加在这

		if (factory.getCollectArmy() == 0) {
			return max;
		}

		ArmyEntity armyEntity = getPlayerData().getArmyEntity(factory.getCollectArmy());
		if (Objects.isNull(armyEntity)) {
			return max;
		}

		return max - armyEntity.getTrainCount();
	}

	/** 最多再进化多少*/
	private int maxAdvance(PlantSoldierAdvanceEntity factory, BattleSoldierCfg toarmyCfg) {
		int max = maxAdvance(factory);

		SoldierType type = SoldierType.XXXXXXXXXXXMAN;
		if (Objects.nonNull(toarmyCfg)) {
			type = toarmyCfg.getSoldierType();
		}
		switch (type) {
		case TANK_SOLDIER_2:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4110);
			break;
		case TANK_SOLDIER_1:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4111);
			break;
		case PLANE_SOLDIER_4:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4112);
			break;
		case PLANE_SOLDIER_3:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4113);
			break;
		case FOOT_SOLDIER_6:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4114);
			break;
		case FOOT_SOLDIER_5:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4115);
			break;
		case CANNON_SOLDIER_8:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4116);
			break;
		case CANNON_SOLDIER_7:
			max += player.getEffect().getEffVal(EffType.PLANT_SOLDIER_4117);
			break;
		default:
			break;
		}

		return Math.max(0, max);
	}

	// /** 收满时间点*/
	// private long advanceEndTime(PlantSoldierAdvanceEntity factory) {
	// return factory.getLastResStoreTime() + (long) ((maxStroe(factory) - factory.getResStore()) * getAdvanceSpeed(factory));
	// }
	//
	// private long advanceStartTime(PlantSoldierAdvanceEntity factory) {
	// return (long) (factory.getLastResStoreTime() - factory.getResStore() * getAdvanceSpeed(factory));
	// }

//	@MessageHandler
//	private void onEffectChangeEvent(PlayerEffectChangeMsg event) {
//		boolean repush = event.hasEffectChange(EffType.PLANT_SOLDIER_4118)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4119)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4120)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4121)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4122)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4123)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4124)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4125)
//				|| event.hasEffectChange(EffType.PLANT_SOLDIER_4128);
//		if (!repush) {
//			return;
//		}
//		PlantSoldierAdvanceEntity factory = player.getData().getPlantSoldierAdvanceEntity();
//		resTore(factory);
//		syncPlantFactoryInfo();
//		updateQueue(factory);
//	}

}
