package com.hawk.game.module.plantfactory;

import java.util.Objects;
import java.util.Optional;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.common.AccountRoleInfo;
import com.hawk.game.config.PlantTechnologyCfg;
import com.hawk.game.config.PlantTechnologyChipCfg;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.module.plantfactory.tech.TechChip;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.PlantTechnologyChipMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantFactory.PBPlantTechChipUpgradeReq;
import com.hawk.game.protocol.PlantFactory.PBPlantTechSync;
import com.hawk.game.protocol.PlantFactory.PBPlantTechUpgradeReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.BuildingService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class PlayerPlantTechModule extends PlayerModule {
	private boolean checked;

	public PlayerPlantTechModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		PBPlantTechSync.Builder builder = PBPlantTechSync.newBuilder();
		for (PlantTechEntity factory : player.getData().getPlantTechEntities()) {
			builder.addTeches(factory.getTechObj().toPBobj());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLANT_TECH_SYNC_S, builder));

		try{
			for (PlantTechnologyCfg zeroLevelCfg : PlantTechnologyCfg.getZeroLevelCfgMap().values()) {
				checkUnlock(zeroLevelCfg);
			}
			// 世界点显示阶级
			AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
			if (Objects.nonNull(accountRoleInfo) && accountRoleInfo.getCityPlantLevel() != player.getCityPlantLv()) {
				accountRoleInfo.cityPlantLevel(player.getCityPlantLv());
				GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);
			}
			checked = true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return true;
	}

	@Override
	public boolean onTick() {
		return true;
	}

	/**
	 * 建筑升级触发城建礼包
	 * @param msg
	 */
	@MessageHandler
	private void onBuildingLevelUpMsg(BuildingLevelUpMsg msg) {
		PlantTechnologyCfg zeroLevelCfg = PlantTechnologyCfg.getZeroLevelCfgMap().get(BuildingType.valueOf(msg.getBuildingType()));
		if (Objects.isNull(zeroLevelCfg)) {
			return;
		}
		for(PlantTechnologyCfg cfg : PlantTechnologyCfg.getZeroLevelCfgMap().values()){
			checkUnlock(cfg);
		}

	}

	private void checkUnlock(PlantTechnologyCfg zeroLevelCfg) {
		if(player.isInDungeonMap()){
			return;
		}
		if (getTechObjByType(BuildingType.valueOf(zeroLevelCfg.getBuilding())) != null) { // 如果建筑上的解锁
			return;
		}
		if (!checkFront(zeroLevelCfg, 0)) {
			return;
		}

		// 创建
		PlantTechEntity entity = new PlantTechEntity();
		entity.setPlayerId(player.getId());
		entity.setCfgId(zeroLevelCfg.getId());
		entity.setBuildType(zeroLevelCfg.getBuilding());

		entity.afterRead();

		HawkDBManager.getInstance().create(entity);
		player.getData().getPlantTechEntities().add(entity);

		PlantTech techObj = entity.getTechObj();
		techObj.notifyChange();

		LogUtil.logPlantTechChange(player, Action.PLANT_TECH_UNKOCK, 0, zeroLevelCfg.getId(), techObj);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.PLANT_TECH_UNKOCK,
				Params.valueOf("toString", techObj.toString()));
	}

	@ProtocolHandler(code = HP.code.PLANT_TECH_UPGRADE_C_VALUE)
	private void onUpgradeTech(HawkProtocol protocol) {
		PBPlantTechUpgradeReq req = protocol.parseProtocol(PBPlantTechUpgradeReq.getDefaultInstance());
		BuildingType type = req.getBuildType();
		PlantTech upfactory = getTechObjByType(type);
		PlantTechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyCfg.class, upfactory.getCfgId());
		PlantTechnologyCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyCfg.class, cfg.getPostStage());

		if (!checkFront(upcfg, protocol.getType())) {
			return;
		}

		// 技能达到最高等级
		for (TechChip chip : upfactory.getChips()) {
			if (chip.getCfg().getLevel() < cfg.getMaxChipLevel()) {
				return;
			}
		}

		// 消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(upcfg.getBuildCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_TECH_UPGRADE);

		upfactory.setCfgId(upcfg.getId());

		upfactory.notifyChange();

		player.responseSuccess(protocol.getType());
		// 世界点显示阶级
		AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
		if(Objects.nonNull(accountRoleInfo)){
			accountRoleInfo.cityPlantLevel(player.getCityPlantLv());
			GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);
		}

		LogUtil.logPlantTechChange(player, Action.PLANT_TECH_UPGRADE, cfg.getId(), upcfg.getId(), upfactory);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLANT_TECH_UPGRADE,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("beforedCfgId", cfg.getId()),
				Params.valueOf("afterCfgId", upcfg.getId()),
				Params.valueOf("toString", upfactory.toString()));

	}

	@ProtocolHandler(code = HP.code.PLANT_TECH_CHIP_UPGRADE_C_VALUE)
	private void onUpgradeTechChip(HawkProtocol protocol) {
		PBPlantTechChipUpgradeReq req = protocol.parseProtocol(PBPlantTechChipUpgradeReq.getDefaultInstance());
		final BuildingType type = req.getBuildType();
		final int chipId = req.getChipCfgId();
		PlantTech upfactory = getTechObjByType(type);

		TechChip upchip = upfactory.getChipById(chipId);
		if (upchip == null || upchip.getCfg().getLevel() >= upfactory.getCfg().getMaxChipLevel()) {
			return;
		}

		PlantTechnologyChipCfg chipCfg = upchip.getCfg();
		PlantTechnologyChipCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyChipCfg.class, chipCfg.getPostStage());
		// 消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(upcfg.getBuildCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.PLANT_TECH_CHIP_UPGRADE);

		upchip.setCfgId(upcfg.getId());

		upfactory.notifyChange();

		player.responseSuccess(protocol.getType());

		LogUtil.logPlantTechChange(player, Action.PLANT_TECH_CHIP_UPGRADE, chipCfg.getId(), upcfg.getId(), upfactory);
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLANT_TECH_CHIP_UPGRADE,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("beforedCfgId", chipCfg.getId()),
				Params.valueOf("afterCfgId", upcfg.getId()),
				Params.valueOf("toString", upfactory.toString()));
		
		// 增加推送礼包泰能强化统计次数
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		pushGiftEntity.addPlantTechnologyTimes();
		HawkApp.getInstance().postMsg(player, PlantTechnologyChipMsg.valueOf(pushGiftEntity.getPlantTechnologyTimes(), type));
	}

	private PlantTech getTechObjByType(BuildingType type) {
		for (PlantTechEntity factory : player.getData().getPlantTechEntities()) {
			if (type.getNumber() == factory.getBuildType()) {
				return factory.getTechObj();
			}
		}
		return null;
	}

	private boolean checkFront(PlantTechnologyCfg cfg, int protoType) {
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(player, cfg.getFrontBuildIds(), null, protoType)) {
			return false;
		}

		// 检查前置工厂
		for (int cfgId : cfg.getFrontTechIds()) {
			PlantTechnologyCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyCfg.class, cfgId);
			if (buildingCfg == null) {
				continue;
			}

			Optional<PlantTechEntity> op = player.getData().getPlantTechEntities().stream()
					.filter(e -> e.getBuildType() == buildingCfg.getBuilding())
					.filter(e -> e.getCfgId() >= cfgId)
					.findAny();

			if (!op.isPresent()) {
				if (protoType > 0) {
					player.sendError(protoType, Status.PlantFactoryError.FRONT_PLANT_TECH_NOT_EXIT, 0);
				}
				return false;
			}
		}

		// 上一等级工厂是否解锁
		if (cfg.getFrontStage() != 0) {
			boolean frontStateUnlock = false;
			for (PlantTechEntity factory : player.getData().getPlantTechEntities()) {
				if (factory.getCfgId() == cfg.getFrontStage()) {
					frontStateUnlock = true; // 已解锁
					break;
				}
			}
			if (!frontStateUnlock) {
				if (protoType > 0) {
					player.sendError(protoType, Status.PlantFactoryError.FRONT_PLANT_TECH_NOT_EXIT, 0);
				}
				return false;
			}
		}
		return true;

	}
}
