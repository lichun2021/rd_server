package com.hawk.game.module.plantsoldier.strengthen;

import java.util.List;
import java.util.Objects;

import com.hawk.game.protocol.PlantSoldierSchool;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.CrystalAnalysisChip;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.PlantCrystalAnalysis;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.cfg.PlantCrystalAnalysisChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.InstrumentChip;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.PlantInstrument;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitaryChip;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.CrackChip;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthenTech;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenTechCfg;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.PlantCrystalAnalysisChipMsg;
import com.hawk.game.msg.PlantInstrumentUpChipMsg;
import com.hawk.game.msg.PlantSoldierCrackChipMsg;
import com.hawk.game.msg.SoldierStrengthenTechLevelUpMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantCrystalAnalysisChipUpgradeReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantInstrumentChipUpgradeReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSOLdierStrengthTechUpgradeReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierCrackChipUpgradeReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierCrackUpgradeReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.PlantCrystalMaxEvent;
import com.hawk.game.service.mssion.event.PlantInstrumentMaxEvent;
import com.hawk.game.service.mssion.event.PlantSoldierCrackMaxEvent;
import com.hawk.game.service.mssion.event.PlantSoldierSeeEvent;
import com.hawk.game.service.mssion.event.PlantSoldierStepMaxEvent;
import com.hawk.game.service.mssion.event.PlantSoldierStepOneEvent;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import org.hawk.os.HawkException;

public class PlayerPlantSoldierSchoolModule extends PlayerModule {
	private static final int MAX_MILITARY_LEVEL = 5;

	public PlayerPlantSoldierSchoolModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		player.getPlantSoldierSchool().checkUnlock();
		try {
			WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
			if (worldPoint != null) {
				worldPoint.setPlantMilitaryLevel(player.getMaxSoldierPlantMilitaryLevel());
				worldPoint.setPlantMilitaryShow(player.getPlantSoldierSchool().getOutShowSwitchState());
				worldPoint.notifyUpdate();
			} else {
				HawkLog.logPrintln("PlayerPlantSoldierSchoolModule player login, fetch worldPoint is null, playerId: {}", player.getId());
			}
		} catch (Exception e){
			HawkException.catchException(e);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PLANT_SOLDIER_SYNC, player.getPlantSoldierSchool().toPBobj().toBuilder()));
		return true;
	}

	@Override
	public boolean onTick() {
		return true;
	}

	@MessageHandler
	private void onBuildingLevelUpMsg(BuildingLevelUpMsg msg) {
		player.getPlantSoldierSchool().checkUnlock();

		player.getPlantSoldierSchool().notifyChange(null);
	}

	/***
	 * 破译仪器升级阶段
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_INSTRUMENT_CHIP_UPGRADE_C_VALUE)
	private void onUpgradeInstrumentChip(HawkProtocol protocol) {
		
		//建筑 是否解锁
		List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.PLANT_POYISUO);
		if(buildingList == null || buildingList.size() == 0) {
			player.sendError(HP.code2.PLANT_INSTRUMENT_CHIP_UPGRADE_C_VALUE, Status.Error.BUILDING_FRONT_NOT_EXISIT, 0);
			return;
		}
		
		StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, 14);
		if(Objects.isNull(nextCfg) || !nextCfg.isOpen()){
			return;
		}
		
		PBPlantInstrumentChipUpgradeReq req = protocol.parseProtocol(PBPlantInstrumentChipUpgradeReq.getDefaultInstance());
		final int chipId = req.getChipCfgId();

		final PlantInstrument instrument = player.getPlantSoldierSchool().getInstrument();
		if (!instrument.isUnlock()) {
			return;
		}
		InstrumentChip upchip = instrument.getChipById(chipId);
		if (upchip == null || !upchip.isUnlock() || upchip.getCfg().getLevel() >= instrument.getCfg().getMaxChipLevel()) {
			return;
		}

		PlantInstrumentUpgradeChipCfg chipCfg = upchip.getCfg();
		PlantInstrumentUpgradeChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantInstrumentUpgradeChipCfg.class, chipCfg.getPostStage());
		// 消耗
		List<ItemInfo> buildCost = ItemInfo.valueListOf(upcfg.getBuildCost());
		reduceByEffect367819(buildCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(buildCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_INSTRUMENT_CHIP_UPGRADE);

		upchip.setCfgId(upcfg.getId());

		instrument.notifyChange();
		player.getPlantSoldierSchool().checkUnlock();
		player.getPlantSoldierSchool().notifyChange(null);
		player.responseSuccess(protocol.getType());

		if (instrument.isMax()) {
			MissionManager.getInstance().postMsg(player, new PlantInstrumentMaxEvent());
		}

		HawkApp.getInstance().postMsg(player, PlantInstrumentUpChipMsg.valueOf(upcfg.getLevel()));
		
		LogUtil.logPlantSchoolChange(player, Action.PLANT_INSTRUMENT_CHIP_UPGRADE, chipCfg.getId(), upcfg.getId(), player.getPlantSoldierSchool());
	}

	/**
	 * 战士主体破译
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_CRACK_UPGRADE_C_VALUE)
	private void onSoldierCrackUpgrade(HawkProtocol protocol) {
		PBPlantSoldierCrackUpgradeReq req = protocol.parseProtocol(PBPlantSoldierCrackUpgradeReq.getDefaultInstance());
		SoldierType type = req.getSoldierType();
		PlantSoldierCrack upfactory = player.getPlantSoldierSchool().getSoldierCrackByType(type);
		if (Objects.isNull(upfactory) || !upfactory.isUnlock()) {
			return;
		}

		PlantSoldierCrackCfg cfg = upfactory.getCfg();
		PlantSoldierCrackCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackCfg.class, cfg.getPostStage());

		if(upcfg == null){
			return;
		}
		
		// 技能达到最高等级
		for (CrackChip chip : upfactory.getChips()) {
			if (chip.getCfg().getLevel() < cfg.getMaxChipLevel()) {
				return;
			}
		}

		// 消耗
		List<ItemInfo> techCost = ItemInfo.valueListOf(upcfg.getBuildCost());
		reduceByEffect367819(techCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(techCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_SOLDIER_CRACK_UPGRADE);

		upfactory.setCfgId(upcfg.getId());

		upfactory.notifyChange();
		player.getPlantSoldierSchool().checkUnlock();
		player.getPlantSoldierSchool().notifyChange(type);
		player.responseSuccess(protocol.getType());

		if (upfactory.isMax()) {
			MissionManager.getInstance().postMsg(player, new PlantSoldierCrackMaxEvent());
		}

		LogUtil.logPlantSchoolChange(player, Action.PLANT_SOLDIER_CRACK_UPGRADE, cfg.getId(), upcfg.getId(), player.getPlantSoldierSchool());

	}

	/**
	 * 战士组件破译
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_CRACK_UPGRADE_CHIP_C_VALUE)
	private void onSoldierCrackChipUpgrade(HawkProtocol protocol) {
		PBPlantSoldierCrackChipUpgradeReq req = protocol.parseProtocol(PBPlantSoldierCrackChipUpgradeReq.getDefaultInstance());
		final SoldierType type = req.getSoldierType();
		final int chipId = req.getChipCfgId();
		PlantSoldierCrack upfactory = player.getPlantSoldierSchool().getSoldierCrackByType(type);

		CrackChip upchip = upfactory.getChipById(chipId);
		if (upchip == null || upchip.getCfg().getLevel() >= upfactory.getCfg().getMaxChipLevel()) {
			return;
		}

		PlantSoldierCrackChipCfg chipCfg = upchip.getCfg();
		PlantSoldierCrackChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackChipCfg.class, chipCfg.getPostStage());
		// 消耗
		List<ItemInfo> builcCost = ItemInfo.valueListOf(upcfg.getBuildCost());
		reduceByEffect367819(builcCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(builcCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_SOLDIER_CRACK_UPGRADE_CHIP);

		upchip.setCfgId(upcfg.getId());

		upfactory.notifyChange();
		player.getPlantSoldierSchool().notifyChange(type);
		player.responseSuccess(protocol.getType());

		LogUtil.logPlantSchoolChange(player, Action.PLANT_SOLDIER_CRACK_UPGRADE_CHIP, chipCfg.getId(), upcfg.getId(), player.getPlantSoldierSchool());
		
		// 推送礼包
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		pushGiftEntity.addPlantSoldierCrackTimes();
		HawkApp.getInstance().postMsg(player, PlantSoldierCrackChipMsg.valueOf(pushGiftEntity.getPlantSoldierCrackTimes(), upcfg.getLevel()));
		
	}

	/***
	 * 晶体分析阶段
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_CRYSTALANALYSIS_CHIP_UPGRADE_C_VALUE)
	private void onUpgradeCrystalAnalysisChip(HawkProtocol protocol) {
		PBPlantCrystalAnalysisChipUpgradeReq req = protocol.parseProtocol(PBPlantCrystalAnalysisChipUpgradeReq.getDefaultInstance());
		final int chipId = req.getChipCfgId();

		final PlantCrystalAnalysis crystal = player.getPlantSoldierSchool().getCrystal();
		if (!crystal.isUnlock()) {
			return;
		}
		CrystalAnalysisChip upchip = crystal.getChipById(chipId);
		if (upchip == null || upchip.getCfg().getLevel() >= crystal.getCfg().getMaxChipLevel()) {
			return;
		}

		PlantCrystalAnalysisChipCfg chipCfg = upchip.getCfg();
		PlantCrystalAnalysisChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantCrystalAnalysisChipCfg.class, chipCfg.getPostStage());
		// 消耗
		List<ItemInfo> buildCost = ItemInfo.valueListOf(upcfg.getBuildCost());
		reduceByEffect367819(buildCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(buildCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_CRYSTALANALYSIS_CHIP_UPGRADE);

		upchip.setCfgId(upcfg.getId());

		crystal.notifyChange();
		player.getPlantSoldierSchool().checkUnlock();
		player.getPlantSoldierSchool().notifyChange(null);
		player.responseSuccess(protocol.getType());

		if (crystal.isMax()) {
			MissionManager.getInstance().postMsg(player, new PlantCrystalMaxEvent());
		}
		LogUtil.logPlantSchoolChange(player, Action.PLANT_CRYSTALANALYSIS_CHIP_UPGRADE, chipCfg.getId(), upcfg.getId(), player.getPlantSoldierSchool());
		
		HawkApp.getInstance().postMsg(player, PlantCrystalAnalysisChipMsg.valueOf(upcfg.getLevel()));
	}

	// 强化阶段
	/**
	 * 升级科技
	 * @param id 科技Id
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIERSTRENGTHEN_TECH_UPGRADE_C_VALUE)
	private void onSoldierStrengthenTechLevelUp(HawkProtocol protocol) {
		PBPlantSOLdierStrengthTechUpgradeReq req = protocol.parseProtocol(PBPlantSOLdierStrengthTechUpgradeReq.getDefaultInstance());
		int cfgId = req.getTechCfgId();

		PlantSoldierStrengthenTechCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenTechCfg.class, cfgId);
		// 科技配置数据错误
		if (upcfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			return;
		}

		final SoldierStrengthen sthen = player.getPlantSoldierSchool().getSoldierStrengthenByType(upcfg.getType());

		if (!sthen.checkCondition(upcfg)) {
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return;
		}

		// 消耗
		List<ItemInfo> techCost = ItemInfo.valueListOf(upcfg.getTechCost());
		reduceByEffect367819(techCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(techCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_SOLDIERSTRENGTHEN_TECH_UPGRADE);

		SoldierStrengthenTech tech = sthen.getTechByTechId(upcfg.getTechId());
		final int beforeId = Objects.isNull(tech) ? 0 : tech.getCfg().getId();
		final int beforeLevel = sthen.getPlantStrengthLevel();
		sthen.techUpGrade(upcfg);

		sthen.notifyChange();
		player.getPlantSoldierSchool().notifyChange(upcfg.getType());
		player.responseSuccess(protocol.getType());

		LogUtil.logPlantSchoolChange(player, Action.PLANT_SOLDIERSTRENGTHEN_TECH_UPGRADE, beforeId, upcfg.getId(), player.getPlantSoldierSchool());
		if (beforeLevel != sthen.getPlantStrengthLevel()) {
			if (sthen.getPlantStrengthLevel() == 1) {
				MissionManager.getInstance().postMsg(player, new PlantSoldierStepOneEvent());
			} else if (sthen.getPlantStrengthLevel() == 5) {
				MissionManager.getInstance().postMsg(player, new PlantSoldierStepMaxEvent());
			}
		}	
		
		HawkApp.getInstance().postMsg(player, SoldierStrengthenTechLevelUpMsg.valueOf(upcfg.getSoldierType(), upcfg.getLevel(), upcfg.getGroup()));
	}

	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_SEE_FINISH_C_VALUE)
	private void onPlantSoldierSeeFinsh(HawkProtocol protocol) {
		MissionManager.getInstance().postMsg(player, new PlantSoldierSeeEvent());
	}

	/**
	 * 解锁军衔
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_MILITARY_UNLOCK_VALUE)
	private void onSoldierMilitaryUnlock(HawkProtocol protocol) {
		PBPlantMilitaryReq req = protocol.parseProtocol(PBPlantMilitaryReq.getDefaultInstance());
		SoldierType soldierType = req.getSoldierType();
		if (soldierType == null) {
			return;
		}
		// 已解锁
		PlantSoldierMilitary military;
		if(req.getType() == PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3){
			military = player.getPlantSoldierSchool().getSoldierMilitary3ByType(soldierType);
		}else {
			military = player.getPlantSoldierSchool().getSoldierMilitaryByType(soldierType);
		}
		if (military.isUnlock()) {
			return;
		}
		// 前置条件不满足
		if (!military.canUnlock()) {
			return;
		}
		military.setUnlock(true);
		military.notifyChange();
		player.getPlantSoldierSchool().notifyChange(soldierType);
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 升级军衔
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_MILITARY_UPGRADE_VALUE)
	private void onSoldierMilitaryUpgreade(HawkProtocol protocol) {
		PBPlantMilitaryReq req = protocol.parseProtocol(PBPlantMilitaryReq.getDefaultInstance());
		SoldierType soldierType = req.getSoldierType();
		if (soldierType == null) {
			return;
		}
		// 未解锁
		PlantSoldierMilitary military;
		if(req.getType() == PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3){
			military = player.getPlantSoldierSchool().getSoldierMilitary3ByType(soldierType);
		}else {
			military = player.getPlantSoldierSchool().getSoldierMilitaryByType(soldierType);
		}
		if (!military.isUnlock()) {
			return;
		}
		// 拿取部件
		PlantSoldierMilitaryChip chip = military.getChip(req.getChipCfgId());
		if (chip == null) {
			return;
		}
		// 没有下一等级了
		PlantSoldierMilitaryChipCfg nextChipCfg = chip.getNextCfg();
		if (nextChipCfg == null) {
			return;
		}
		// 消耗
		List<ItemInfo> buildCost = ItemInfo.valueListOf(nextChipCfg.getBuildCost());
		reduceByEffect367819(buildCost);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(buildCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANTSOLDIER_MILITARY_UPGRADE);
		int beforeLevel = military.getMilitaryLevel();
		chip.setCfgId(nextChipCfg.getId());
		military.notifyChange();
		int nextLevel = military.getMilitaryLevel();
		if (nextLevel > 0 && nextLevel > beforeLevel){
			if(military.getType() == PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3 && nextLevel >= MAX_MILITARY_LEVEL){
				if(!player.getPlantSoldierSchool().getSwitchMap().containsKey(PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_OUT_SHOW_VALUE)){
					player.getPlantSoldierSchool().getSwitchMap().put(PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_OUT_SHOW_VALUE, 1);
				}
				if(!player.getPlantSoldierSchool().getSwitchMap().containsKey(PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE)){
					player.getPlantSoldierSchool().getSwitchMap().put(PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE, soldierType.getNumber());
				}
			}
			WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
			int maxSoldierPlantMilitaryLevel = player.getMaxSoldierPlantMilitaryLevel();
			if (point.getPlantMilitaryLevel() < maxSoldierPlantMilitaryLevel){
				point.setPlantMilitaryLevel(maxSoldierPlantMilitaryLevel);
				point.setPlantMilitaryShow(player.getPlantSoldierSchool().getOutShowSwitchState());
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
				point.notifyUpdate();
			}
		}
		player.getPlantSoldierSchool().notifyChange(soldierType);
		player.responseSuccess(protocol.getType());
		LogUtil.logPlantSoldierMilitaryUpgrade(player, nextChipCfg.getId(), nextChipCfg.getLevel());
	}

	@ProtocolHandler(code = HP.code2.PLANT_SOLDIER_SWITCH_REQ_VALUE)
	private void onSwitch(HawkProtocol protocol) {
		PlantSoldierSchool.PBPlantSwitchReq req = protocol.parseProtocol(PlantSoldierSchool.PBPlantSwitchReq.getDefaultInstance());
		player.getPlantSoldierSchool().getSwitchMap().put(req.getType().getNumber(), req.getState());
		player.getPlantSoldierSchool().notifyChange(null);
		PlantSoldierSchool.PBPlantSwitchResp.Builder resp = PlantSoldierSchool.PBPlantSwitchResp.newBuilder();
		resp.setType(req.getType());
		resp.setState(req.getState());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PLANT_SOLDIER_SWITCH_RESP_VALUE, resp));
		switch (req.getType()){
			case PLANT_SOLDIER_MILITARY_OUT_SHOW:{
				WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
				if(point!=null){
					point.setPlantMilitaryShow(req.getState());
					WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
					point.notifyUpdate();
				}

			}
		}
	}

	public  void reduceByEffect367819(List<ItemInfo> itemInfos) {
//		泰矿母体id 21063005、聚能核心id 21063002、复制晶体id 21063003
		GameUtil.reduceByEffect(itemInfos, 21063005, player.getEffect().getEffValArr(EffType.EFF_367819));
		GameUtil.reduceByEffect(itemInfos, 21063002, player.getEffect().getEffValArr(EffType.EFF_367819));
		GameUtil.reduceByEffect(itemInfos, 21063003, player.getEffect().getEffValArr(EffType.EFF_367819));
	}

}
