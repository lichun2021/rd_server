package com.hawk.game.module.soldierExchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.ArmourSuitCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierSkillLevelCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerArmourModule;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleAddtionalCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleSlotCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEffObject;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.module.plantfactory.tech.TechChip;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitaryChip;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.CrackChip;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthenTech;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.player.supersoldier.skill.ISuperSoldierSkill;
import com.hawk.game.protocol.Armour;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Mail.MailSoldierPB;
import com.hawk.game.protocol.MechaCore.MechaCoreInfoSync;
import com.hawk.game.protocol.SoldierExchange.NationHospitalArmy;
import com.hawk.game.protocol.SoldierExchange.PBSEInfo;
import com.hawk.game.protocol.SoldierExchange.PBSEResp;
import com.hawk.game.service.BuildingService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

public class SoldierExchangeUtil {
	private final Player player;
	private final SoldierType fromType;
	private final SoldierType toType;
	private final PBSEResp.Builder resp = PBSEResp.newBuilder();
	private final PBSEInfo.Builder fromInfo = PBSEInfo.newBuilder();
	private final PBSEInfo.Builder toInfo = PBSEInfo.newBuilder();

	private SoldierExchangeUtil(Player player, SoldierType fromType, SoldierType toType) {
		this.player = player;
		this.fromType = fromType;
		this.toType = toType;
	}

	public static SoldierExchangeUtil create(Player player, SoldierType fromType, SoldierType toType) {
		SoldierExchangeUtil result = new SoldierExchangeUtil(player, fromType, toType);
		return result;
	}

	public void zhuanPlantTech() {
		BuildingType fromBuildtype = getSoldierBuilding(fromType);
		BuildingType toBuildtype = getSoldierBuilding(toType);

		PlantTech fromfactory = getTechObjByType(fromBuildtype);
		PlantTech tofactory = getTechObjByType(toBuildtype);

		doZhuanPlantTech(fromfactory, toBuildtype);
		doZhuanPlantTech(tofactory, fromBuildtype);

	}

	private void doZhuanPlantTech(PlantTech fromfactory, BuildingType toBuildtype) {
		if (Objects.isNull(fromfactory)) {
			return;
		}
		fromInfo.addPlantTechs(fromfactory.toPBobj());
		DungeonRedisLog.log(player.getId(), "PT before to->{} {}", toBuildtype, fromfactory);
		final int cfgId = fromfactory.getTechEntity().getCfgId(); // 10201000
		final int tocfgId = cfgId - fromfactory.getTechEntity().getBuildType() * 100 + toBuildtype.getNumber() * 100;
		fromfactory.getTechEntity().setCfgId(tocfgId);
		fromfactory.getTechEntity().setBuildType(toBuildtype.getNumber());

		for (TechChip chip : fromfactory.getChips()) {
			final int chipId = chip.getCfgId();
			chip.setCfgId(chipId - cfgId * 10 + tocfgId * 10);
		}
		fromfactory.notifyChange();
		toInfo.addPlantTechs(fromfactory.toPBobj());
		LogUtil.logPlantTechChange(player, Action.BINZHONGZHUANHUAN, cfgId, tocfgId, fromfactory);
		DungeonRedisLog.log(player.getId(), "PT fater to->{} {}", toBuildtype, fromfactory);
	}

	private PlantTech getTechObjByType(BuildingType type) {
		for (PlantTechEntity factory : player.getData().getPlantTechEntities()) {
			if (type.getNumber() == factory.getBuildType()) {
				return factory.getTechObj();
			}
		}
		return null;
	}

	public void zhuanArmy(boolean pre) {
		List<ArmyEntity> fromArmys = new ArrayList<>();
		List<ArmyEntity> toArmys = new ArrayList<>();
		for (ArmyEntity army : player.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			SoldierType type = cfg.getSoldierType();
			if (type == fromType) {
				fromArmys.add(army);
			}
			if (type == toType) {
				toArmys.add(army);
			}
		}

		Collections.sort(fromArmys, Comparator.comparingInt(ArmyEntity::getArmyId).reversed());
		Collections.sort(toArmys, Comparator.comparingInt(ArmyEntity::getArmyId).reversed());

		for (ArmyEntity army : fromArmys) {
			if (pre) {
				fromInfo.addArmyInfos(buildMailSoldierPB(army));
				buildNationHospitalArmy(army, fromInfo);
			} else {
				doZhuanArmy(fromType, toType, army);
			}
		}
		for (ArmyEntity army : toArmys) {
			if (pre) {
				fromInfo.addArmyInfos(buildMailSoldierPB(army));
				buildNationHospitalArmy(army, fromInfo);
			} else {
				doZhuanArmy(toType, fromType, army);
			}
		}

		player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
	}
	
	private void buildNationHospitalArmy(ArmyEntity army, PBSEInfo.Builder builderInfo) {
		int nationDeadCount = army.getNationalHospitalDeadCount();
		int nationRecoveredCount = army.getNationalHospitalRecoveredCount();
		int tszzDeadCount = army.getTszzDeadCount();
		int tszzRecoveredCount = army.getTszzRecoveredCount();
		if (nationDeadCount <= 0 && nationRecoveredCount <= 0 && tszzDeadCount <= 0 && tszzRecoveredCount <= 0) {
			return;
		} 
		NationHospitalArmy.Builder nationArmyBuilder = NationHospitalArmy.newBuilder();
		nationArmyBuilder.setArmyInfos(buildMailSoldierPB(army));
		nationArmyBuilder.setNationHospitalComm1(nationDeadCount);
		nationArmyBuilder.setNationHospitalComm2(nationRecoveredCount);
		nationArmyBuilder.setNationHospitalTSZZ1(tszzDeadCount);
		nationArmyBuilder.setNationHospitalTSZZ2(tszzRecoveredCount);
		builderInfo.addNhArmyInfo(nationArmyBuilder);
	} 

	private void doZhuanArmy(SoldierType fromType, SoldierType toType, ArmyEntity army) {
		DungeonRedisLog.log(army.getPlayerId(), "SE before to->{} {}", toType, army);
		final int armyId = army.getArmyId();
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		SoldierType type = cfg.getSoldierType(); // SoldierType.valueOf((id / 100) % 10);
		if (type != fromType) {
			return;
		}

		final int toArmyId = armyId - (armyId / 100) % 10 * 100 + xmlArmyType(toType) * 100;
		army.setArmyId(toArmyId);

		toInfo.addArmyInfos(buildMailSoldierPB(army));
		buildNationHospitalArmy(army, toInfo);
		LogUtil.logArmyChange(player, army, 0, ArmySection.FREE, ArmyChangeReason.BING_ZHOPNG_ZH);
		DungeonRedisLog.log(army.getPlayerId(), "SE after to->{} {}", toType, army);
	}

	private MailSoldierPB buildMailSoldierPB(ArmyEntity army) {
		ArmyInfo armyInfo = new ArmyInfo();
		armyInfo.setPlayerId(army.getPlayerId());
		armyInfo.setArmyId(army.getArmyId());
		armyInfo.setTotalCount(army.getFree());
		armyInfo.setStar(player.getSoldierStar(army.getArmyId()));
		armyInfo.setPlantStep(player.getSoldierStep(army.getArmyId()));
		return MailBuilderUtil.buildMailSoldierPB(armyInfo).build();
	}

	private static int xmlArmyType(SoldierType fromType) {
		switch (fromType) {
		case TANK_SOLDIER_1:
			return 2;
		case TANK_SOLDIER_2:
			return 1;
		case PLANE_SOLDIER_3:
			return 4;
		case PLANE_SOLDIER_4:
			return 3;
		case FOOT_SOLDIER_5:
			return 6;
		case FOOT_SOLDIER_6:
			return 5;
		case CANNON_SOLDIER_7:
			return 8;
		case CANNON_SOLDIER_8:
			return 7;
		default:
			break;
		}
		return 0;
	}

	/**机建筑*/
	public void zhuanBuild(boolean pre) {
		BuildingType fromBuildtype = getSoldierBuilding(fromType);
		BuildingType toBuildtype = getSoldierBuilding(toType);
		BuildingBaseEntity fromBuild = player.getData().getBuildingEntityByType(fromBuildtype);
		BuildingBaseEntity toBuild = player.getData().getBuildingEntityByType(toBuildtype);
		if (fromBuildtype == toBuildtype && Objects.nonNull(fromBuild)) {
			if (pre) {
				fromInfo.addBuilding(BuilderUtil.genBuildingBuilder(player, fromBuild));
			} else {
				toInfo.addBuilding(BuilderUtil.genBuildingBuilder(player, fromBuild));
			}
			return;
		}

		if (Objects.nonNull(fromBuild)) {
			if (pre) {
				fromInfo.addBuilding(BuilderUtil.genBuildingBuilder(player, fromBuild));
			} else {
				dozhuanJianzhu(fromBuild, toBuildtype);
			}
		}

		if (Objects.nonNull(toBuild)) {
			if (pre) {
				fromInfo.addBuilding(BuilderUtil.genBuildingBuilder(player, toBuild));
			} else {
				dozhuanJianzhu(toBuild, fromBuildtype);
			}
		}
		player.getPush().syncBuildingEntityInfo();
	}

	private void dozhuanJianzhu(BuildingBaseEntity fromBuild, BuildingType toBuildtype) {
		if (fromBuild == null) {
			return;
		}
		DungeonRedisLog.log(player.getId(), "SE before to->{} {}", toBuildtype, fromBuild);
		BuildingCfg bcfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, fromBuild.getBuildingCfgId());
		Optional<BuildingCfg> toCfgOp = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class).stream()
				.filter(cfg -> cfg.getBuildType() == toBuildtype.getNumber())
				.filter(cfg -> cfg.getLevel() == bcfg.getLevel())
				.filter(cfg -> cfg.getHonor() == bcfg.getHonor())
				.filter(cfg -> cfg.getProgress() == bcfg.getProgress())
				.findFirst();
		if (toCfgOp.isPresent()) {
			fromBuild.setBuildingCfgId(toCfgOp.get().getId());
			fromBuild.setType(toCfgOp.get().getBuildType());

			BuildingService.getInstance().refreshBuildEffect(player, toCfgOp.get(), false);
			BuildingService.getInstance().refreshBuildEffect(player, bcfg, false);

			toInfo.addBuilding(BuilderUtil.genBuildingBuilder(player, fromBuild));
			LogUtil.logBuildFlow(player, fromBuild, bcfg.getLevel(), bcfg.getLevel());
			DungeonRedisLog.log(player.getId(), "SE after to->{} {}", toBuildtype, fromBuild);
		}
	}

	public BuildingType getSoldierBuilding(SoldierType type) {
		switch (type) {
		case FOOT_SOLDIER_5:
		case FOOT_SOLDIER_6:
			return BuildingType.BARRACKS;

		case TANK_SOLDIER_1:
		case TANK_SOLDIER_2:
			return BuildingType.WAR_FACTORY;
		case PLANE_SOLDIER_3:
		case PLANE_SOLDIER_4:
			return BuildingType.AIR_FORCE_COMMAND;
		case CANNON_SOLDIER_7:
		case CANNON_SOLDIER_8:
			return BuildingType.REMOTE_FIRE_FACTORY;
		default:
			break;
		}
		return null;
	}

	/** 转泰能*/
	public void zhuanPlantSchool(boolean pre) {
		PlantSoldierSchool plantSoldierSchool = player.getPlantSoldierSchool();
		if (pre) {
			fromInfo.setPlantSoldier(plantSoldierSchool.toPBobj());
			return;
		}
		// System.out.println("#################");
		// System.out.println(plantSoldierSchool.toPBobj());
		DungeonRedisLog.log(player.getId(), "SE before {}->{} {}", fromType, toType, plantSoldierSchool.getDbEntity().toString());

		PlantSoldierCrack fromfactory = plantSoldierSchool.getSoldierCrackByType(fromType);
		SoldierStrengthen fromsthen = plantSoldierSchool.getSoldierStrengthenByType(fromType);
		PlantSoldierMilitary frommilitary = plantSoldierSchool.getSoldierMilitaryByType(fromType);
		PlantSoldierMilitary frommilitary3 = plantSoldierSchool.getSoldierMilitary3ByType(fromType);
		PlantSoldierCrack toctory = plantSoldierSchool.getSoldierCrackByType(toType);
		SoldierStrengthen tosthen = plantSoldierSchool.getSoldierStrengthenByType(toType);
		PlantSoldierMilitary tomilitary = plantSoldierSchool.getSoldierMilitaryByType(toType);
		PlantSoldierMilitary tomilitary3 = plantSoldierSchool.getSoldierMilitary3ByType(toType);
		
		doZhuanPlantca(fromfactory, fromType, toType);
		doZhuanPlantca(toctory, toType, fromType);

		doZhuanStrengh(fromsthen, fromType, toType);
		doZhuanStrengh(tosthen, toType, fromType);

		doZhuanMilitary(frommilitary, fromType, toType);
		doZhuanMilitary(tomilitary, toType, fromType);

		doZhuanMilitary3(frommilitary3, fromType, toType);
		doZhuanMilitary3(tomilitary3, toType, fromType);

		doZhuanSchoolSwitch(plantSoldierSchool, fromType, toType);

		plantSoldierSchool.checkUnlock();
		plantSoldierSchool.notifyChange(null);

		toInfo.setPlantSoldier(plantSoldierSchool.toPBobj());

		// System.out.println("----------------------");
		// System.out.println(plantSoldierSchool.toPBobj());
		LogUtil.logPlantSchoolChange(player, Action.BINZHONGZHUANHUAN, 0, 0, plantSoldierSchool);
		DungeonRedisLog.log(player.getId(), "SE after {}->{} {}", fromType, toType, plantSoldierSchool.getDbEntity().toString());
	}

	private void doZhuanMilitary(PlantSoldierMilitary frommilitary, SoldierType fromType, SoldierType toType) {
		frommilitary.setSoldierType(toType.getNumber());
		for(PlantSoldierMilitaryChip chip: frommilitary.getChips()){
			chip.setCfgId(500000 + toType.getNumber() * 10000 + chip.getCfgId() % 10000);
		}
		frommilitary.notifyChange();
	}

	private void doZhuanMilitary3(PlantSoldierMilitary frommilitary, SoldierType fromType, SoldierType toType) {
		frommilitary.setSoldierType(toType.getNumber());
		for(PlantSoldierMilitaryChip chip: frommilitary.getChips()){
			chip.setCfgId(600000 + toType.getNumber() * 10000 + chip.getCfgId() % 10000);
		}
		frommilitary.notifyChange();
	}

	public void doZhuanSchoolSwitch(PlantSoldierSchool plantSoldierSchool, SoldierType fromType, SoldierType toType){
		if(plantSoldierSchool.getSwitchMap().getOrDefault(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE, -1) == fromType.getNumber()){
			plantSoldierSchool.getSwitchMap().put(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE, toType.getNumber());
		}else if (plantSoldierSchool.getSwitchMap().getOrDefault(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE, -1) == toType.getNumber()){
			plantSoldierSchool.getSwitchMap().put(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_IN_SHOW_VALUE, fromType.getNumber());
		}
	}

	private void doZhuanStrengh(SoldierStrengthen fromSt, SoldierType fromType, SoldierType toType) {
		ConfigIterator<PlantSoldierStrengthenCfg> stIt = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierStrengthenCfg.class);
		PlantSoldierStrengthenCfg toStCfg = stIt.stream().filter(cfg -> cfg.getType() == toType).findFirst().get();
		fromSt.setCfgId(toStCfg.getId());

		for (SoldierStrengthenTech chip : fromSt.getChips()) {
			int chipId = chip.getCfgId();
			chip.setCfgId(chipId - (chipId / 10000) % 10 * 10000 + toType.getNumber() * 10000);
		}
		fromSt.notifyChange();
	}

	private void doZhuanPlantca(PlantSoldierCrack fromfactory, SoldierType fromType, SoldierType toType) {
		// PlantSoldierCrackCfg fcfg = fromfactory.getCfg();
		// ConfigIterator<PlantSoldierCrackCfg> stIt = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierCrackCfg.class);
		// PlantSoldierCrackCfg toStCfg = stIt.stream().filter(cfg -> cfg.getType() == toType && cfg.getLevel() == fcfg.getLevel()).findFirst().get();
		// fromfactory.setCfgId(toStCfg.getId());
		int crackId = fromfactory.getCfgId();
		fromfactory.setCfgId(crackId - (crackId / 10000) % 10 * 10000 + toType.getNumber() * 10000);

		for (CrackChip chip : fromfactory.getChips()) {
			int chipId = chip.getCfgId();
			chip.setCfgId(chipId - (chipId / 10000) % 10 * 10000 + toType.getNumber() * 10000);
		}
		fromfactory.notifyChange();
	}

	/**机甲转*/
	public void zhuanSuperSoldier() {
		ConfigIterator<SuperSoldierCfg> supsCfgIt = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		SuperSoldierCfg fromLevelOnecfg = null;
		SuperSoldier fromLevelOneSoldier = null;
		SuperSoldierCfg toLevelOnecfg = null;
		SuperSoldier toLevelOneSoldier = null;

		SuperSoldierCfg fromLevelTwocfg = null;
		SuperSoldier fromLevelTwoSoldier = null;
		SuperSoldierCfg toLevelTwocfg = null;
		SuperSoldier toLevelTwoSoldier = null;
		for (SuperSoldierCfg supcfg : supsCfgIt) {
			if (supcfg.getSoldierTypeSet().contains(fromType) && supcfg.getPreSupersoldierId() == 0) {
				fromLevelOnecfg = supcfg;
				fromLevelOneSoldier = player.getSuperSoldierByCfgId(supcfg.getSupersoldierId()).orElse(null);
			}
			if (supcfg.getSoldierTypeSet().contains(toType) && supcfg.getPreSupersoldierId() == 0) {
				toLevelOnecfg = supcfg;
				toLevelOneSoldier = player.getSuperSoldierByCfgId(supcfg.getSupersoldierId()).orElse(null);
			}

			if (supcfg.getSoldierTypeSet().contains(fromType) && supcfg.getPreSupersoldierId() != 0) {
				fromLevelTwocfg = supcfg;
				fromLevelTwoSoldier = player.getSuperSoldierByCfgId(supcfg.getSupersoldierId()).orElse(null);
			}
			if (supcfg.getSoldierTypeSet().contains(toType) && supcfg.getPreSupersoldierId() != 0) {
				toLevelTwocfg = supcfg;
				toLevelTwoSoldier = player.getSuperSoldierByCfgId(supcfg.getSupersoldierId()).orElse(null);
			}

		}

		doDiaoHuanSup(fromLevelOneSoldier, toLevelOnecfg);
		doDiaoHuanSup(toLevelOneSoldier, fromLevelOnecfg);
		doDiaoHuanSup(fromLevelTwoSoldier, toLevelTwocfg);
		doDiaoHuanSup(toLevelTwoSoldier, fromLevelTwocfg);
		player.getPush().pushSuperSoldier();
	}

	private void doDiaoHuanSup(SuperSoldier fromSupe, SuperSoldierCfg toType) {
		if (fromSupe == null || fromSupe.getConfig() == toType) {
			return;
		}
		SuperSoldierCfg fromType = fromSupe.getConfig();
		fromInfo.addSuperSoldiers(fromSupe.toPBobj());
		DungeonRedisLog.log(fromSupe.getParent().getId(), "SE before {} {}", toType, fromSupe.getDBEntity().toString());

		for (HawkTuple3<Integer, Integer, Integer> tup : toType.getSkillBlankList()) {
			Optional<SuperSoldierSkillSlot> skillSlot = fromSupe.getSkillSlots().stream().filter(slot -> slot.getIndex() == tup.first).findAny();
			ISuperSoldierSkill skill = skillSlot.get().getSkill();
			int level = skill.getLevel();
			skill.setSkillID(tup.third);

			if (level > 1) {
				ConfigIterator<SuperSoldierSkillLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierSkillLevelCfg.class);
				int exp = configIterator.stream()
						.filter(cfg -> cfg.getSkillQuality() == skill.getCfg().getSkillQuality())
						.filter(cfg -> cfg.getSkillLevel() == level - 1).findFirst().get().getSkillExp();
				skill.setExp(exp);
			}
		}

		for (ISuperSoldierEnergy eng : fromSupe.getSoldierEnergy().getEnergys()) {
			int cfgId = eng.getCfgId();
			eng.init(cfgId % 10000 + toType.getSupersoldierId() * 10000);
		}
		fromSupe.getDBEntity().setSoldierId(toType.getSupersoldierId());
		if (fromSupe.getSkin() == fromType.getUnlockAnyWhereGetSkin()) {
			fromSupe.changeSkin(toType.getUnlockAnyWhereGetSkin());
		}
		fromSupe.notifyChange();
		toInfo.addSuperSoldiers(fromSupe.toPBobj());
		LogUtil.logMechaAttrChange(fromSupe.getParent(), Action.BINZHONGZHUANHUAN, fromSupe);
		LogUtil.logSuperSoldierEnergy(player, fromSupe);
		DungeonRedisLog.log(fromSupe.getParent().getId(), "SE after {} {}", toType, fromSupe.getDBEntity().toString());
	}

	public void zhuanArmour() {
		List<ArmourEntity> armourEntityList = player.getData().getArmourEntityList();
		Map<Integer, Long> suitPowerMap = new HashMap<>();
		for (ArmourEntity armour : armourEntityList) {
			ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			if (armourCfg == null) {
				continue;
			}
			if (armourCfg.getArmourSuitId() == 0) {
				continue;
			}
			ArmourSuitCfg suitCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourSuitCfg.class, armourCfg.getArmourSuitId());
			if (suitCfg == null) {
				continue;
			}
			if (armourCfg.getArmourSuitId() != fromType.getNumber()) {
				continue;
			}
			for (int suitId : armour.getSuitSet()) {
				long before = suitPowerMap.getOrDefault(suitId, 0L);
				long after = before + 10000000L + GameUtil.getArmourPower(armour);
				suitPowerMap.put(suitId, after);
			}
		}
		int suitId = -1;
		long power = 0L;
		for (int tmpId : suitPowerMap.keySet()) {
			long tmpPower = suitPowerMap.getOrDefault(tmpId, 0L);
			if (tmpPower > power) {
				suitId = tmpId;
				power = tmpPower;
			}
		}
		Armour.ArmourBriefInfo.Builder fromArmour = Armour.ArmourBriefInfo.newBuilder();
		Armour.ArmourBriefInfo.Builder toArmour = Armour.ArmourBriefInfo.newBuilder();
		List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			Armour.ArmourTechInfo.Builder researchInfo = Armour.ArmourTechInfo.newBuilder();
			researchInfo.setArmourTechId(researchEntity.getResearchId());
			researchInfo.setArmourTechLevel(researchEntity.getResearchLevel());
			fromArmour.addArmourTechInfo(researchInfo);
			toArmour.addArmourTechInfo(researchInfo);
		}
		List<ArmourEntity> suitArmours = player.getSuitArmours(suitId);
		for (ArmourEntity armour : suitArmours) {
			fromArmour.addArmourInfo(BuilderUtil.genArmourInfoBuilder(armour));
			changeArmour(armour, fromType.getNumber(), toType.getNumber());
			armour.notifyUpdate();
			toArmour.addArmourInfo(BuilderUtil.genArmourInfoBuilder(armour));
			player.getPush().syncArmourInfo(armour);
			PlayerArmourModule module = player.getModule(GsConst.ModuleType.ARMOUR_MODULE);
			module.logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_16);
		}
		fromInfo.setArmours(fromArmour);
		toInfo.setArmours(toArmour);
		player.getEffect().resetEffectArmour(player);
		player.refreshPowerElectric(LogConst.PowerChangeReason.ARMOUR_CHANGE);
	}

	private void changeArmour(ArmourEntity armour, int from, int to) {
		ArmourCfg fromCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
		if (armour.isSuper()) {
			return;
		}
		if (fromCfg == null) {
			return;
		}
		if (fromCfg.getArmourSuitId() != from && fromCfg.getArmourSuitId() != to) {
			return;
		}
		if (fromCfg.getArmourSuitId() == to) {
			int tmp = to;
			to = from;
			from = tmp;
		}
		ConfigIterator<ArmourCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourCfg.class);
		for (ArmourCfg cfg : iterator) {
			if (cfg.isSuper()) {
				continue;
			}
			if (cfg.getPos() == fromCfg.getPos() && cfg.getArmourSuitId() == to) {
				armour.setArmourId(cfg.getArmourId());
			}
		}
		for (ArmourEffObject armourEffObject : armour.getExtraAttrEff()) {
			changeArmourExtraEff(armourEffObject, from, to);
		}
		for (ArmourEffObject armourEffObject : armour.getStarEff()) {
			// 将代替换的属性重置，防止转职后属性重复
		    armourEffObject.setReplaceAttrId(0);
			changeArmourStarEff(armourEffObject, from, to);
		}
	}

	private static void changeArmourStarEff(ArmourEffObject armourEffObject, int from, int to) {
		ArmourChargeLabCfg fromCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, armourEffObject.getAttrId());
		if (fromCfg == null) {
			return;
		}
		if (!fromCfg.getSoldierType().contains(from) && !fromCfg.getSoldierType().contains(to)) {
			return;
		}
		if (fromCfg.getSoldierType().contains(from) && fromCfg.getSoldierType().contains(to)) {
			return;
		}
		if (fromCfg.getSoldierType().contains(to)) {
			int tmp = to;
			to = from;
			from = tmp;
		}
		ConfigIterator<ArmourChargeLabCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourChargeLabCfg.class);
		for (ArmourChargeLabCfg cfg : iterator) {
			if (cfg.getEffectGroup() == fromCfg.getEffectGroup() && cfg.getSoldierType().contains(to)) {
				EffectObject attributeValue = cfg.getAttributeValue();
				armourEffObject.setAttrId(cfg.getId());
				armourEffObject.setEffectType(attributeValue.getEffectType());
				armourEffObject.setEffectValue(attributeValue.getEffectValue());
			}
		}
	}

	private static void changeArmourExtraEff(ArmourEffObject armourEffObject, int from, int to) {
		ArmourAdditionalCfg fromCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, armourEffObject.getAttrId());
		if (fromCfg == null) {
			return;
		}
		if (!fromCfg.getSoldierType().contains(from) && !fromCfg.getSoldierType().contains(to)) {
			return;
		}
		if (fromCfg.getSoldierType().contains(from) && fromCfg.getSoldierType().contains(to)) {
			return;
		}
		if (fromCfg.getSoldierType().contains(to)) {
			int tmp = to;
			to = from;
			from = tmp;
		}
		List<ArmourAdditionalCfg> attrCfgs = AssembleDataManager.getInstance().getArmourAdditionCfgs(1, fromCfg.getQuality());
		for (ArmourAdditionalCfg cfg : attrCfgs) {
			if (cfg.getEffectGroup() == fromCfg.getEffectGroup() && cfg.getSoldierType().contains(to)) {
				ArmourAttrTemplate toAttr = RandomUtil.random(cfg.getAttrList());
				if (armourEffObject.getEffectValue() >= toAttr.getRandMin() && armourEffObject.getEffectValue() <= toAttr.getRandMax()) {
					HawkLog.logPrintln("===whc=== changeArmourExtraEff before attrId:{},effectType:{},effectValue:{}",
							armourEffObject.getAttrId(), armourEffObject.getEffectType(), armourEffObject.getEffectValue());
					armourEffObject.setAttrId(cfg.getId());
					armourEffObject.setEffectType(toAttr.getEffect());
					HawkLog.logPrintln("===whc=== changeArmourExtraEff after attrId:{},effectType:{},effectValue:{}",
							armourEffObject.getAttrId(), armourEffObject.getEffectType(), armourEffObject.getEffectValue());
				}
			}
		}
	}
	
	/**
	 * 机甲核心
	 */
	public void zhuanMechaCore(Player sourcePlayer) {
		PlayerMechaCore mechaCore = sourcePlayer.getPlayerMechaCore();
		List<MechaCoreModuleEntity> moduleEntityList = sourcePlayer.getData().getMechaCoreModuleEntityList();
		Map<Integer, Long> suitPowerMap = new HashMap<>();
		for (MechaCoreModuleEntity module : moduleEntityList) {
			MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, module.getCfgId());
			if (moduleCfg == null || !moduleCfg.getTroopsTypeSet().contains(fromType.getNumber())) {
				continue;
			}
			for (int suitId : module.getSuitList()) {
				long before = suitPowerMap.getOrDefault(suitId, 0L);
				MechaCoreModuleSlotCfg slotCfg = mechaCore.getSlotCfg(moduleCfg.getModuleType());
				long after = before + 10000000L + (int)Math.floor(mechaCore.getModulePower(module, slotCfg));
				suitPowerMap.put(suitId, after);
			}
		}
		int suitId = -1;
		long power = 0L;
		for (int tmpId : suitPowerMap.keySet()) {
			long tmpPower = suitPowerMap.getOrDefault(tmpId, 0L);
			if (tmpPower > power) {
				suitId = tmpId;
				power = tmpPower;
			}
		}
		
		int oldSuit = mechaCore.getWorkSuit();
		if (suitId >= 0 && suitId != oldSuit) {
			mechaCore.suitSwitch(suitId);
		}
		boolean funcUnlocked = sourcePlayer.checkMechacoreFuncUnlock();
		funcUnlocked = funcUnlocked && MechaCoreConstCfg.getInstance().isMechaCoreOpen();
		MechaCoreInfoSync.Builder fromBuilder = MechaCoreInfoSync.newBuilder();
		fromBuilder.setFuncUnlocked(funcUnlocked ? 1 : 0);
		if (funcUnlocked) {
			mechaCore.genMechaCoreBuilder(fromBuilder, suitId);
		}

		List<MechaCoreModuleEntity> moduleList = new ArrayList<>();
		Map<String, MechaCoreModuleEntity> copyModuleMap = new HashMap<>();
		for(int slotType : MechaCoreModuleSlotCfg.getSlotTypes()) {
			MechaCoreModuleEntity module = mechaCore.getLoadedModule(suitId, slotType);
			if (module != null) {
				copyModuleMap.put(module.getId(), module.copy());
				changeModule(module, suitId, slotType, fromType.getNumber(), toType.getNumber());
				moduleList.add(module);
			}
		}
		
		MechaCoreInfoSync.Builder toBuilder = MechaCoreInfoSync.newBuilder();
		toBuilder.setFuncUnlocked(funcUnlocked ? 1 : 0);
		if (funcUnlocked) {
			mechaCore.genMechaCoreBuilder(toBuilder, suitId);
		}
		
		fromInfo.setMechaCore(fromBuilder);
		toInfo.setMechaCore(toBuilder);
		if (suitId >= 0 && suitId != oldSuit) {
			mechaCore.suitSwitch(oldSuit);
		}
		
		if(!player.getId().equals(sourcePlayer.getId())) {
			for (MechaCoreModuleEntity entity : moduleList) {
				MechaCoreModuleEntity copyModule = copyModuleMap.get(entity.getId());
				entity.refresh(copyModule);
			}
		} else {
			mechaCore.syncModuleInfo(moduleList);
			//mechaCore.notifyChange(PowerChangeReason.MECHA_CORE_MODULE);
		}
	}
	
	/**
	 * 机甲核心模块转换
	 * @param module
	 * @param from
	 * @param to
	 */
	private void changeModule(MechaCoreModuleEntity module, int suitId, int slotType, int from, int to) {
		MechaCoreModuleCfg fromCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, module.getCfgId());
		if (fromCfg == null) {
			return;
		}
		if (!fromCfg.getTroopsTypeSet().contains(from) && !fromCfg.getTroopsTypeSet().contains(to)) {
			return;
		}
		if (fromCfg.getTroopsTypeSet().contains(from) && fromCfg.getTroopsTypeSet().contains(to)) {
			for (MechaCoreModuleEffObject effObject : module.getRandomAttrEff()) {
				changeModuleRandomAttr(effObject, suitId, slotType, fromCfg.getId(), module.getCfgId(), from, to);
			}
			return;
		}
		int sourceFrom = from, sourceTo = to;
		if (fromCfg.getTroopsTypeSet().contains(to)) {
			int tmp = to;
			to = from;
			from = tmp;
		}
		ConfigIterator<MechaCoreModuleCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreModuleCfg.class);
		for (MechaCoreModuleCfg cfg : iterator) {
			if (cfg.getModuleType() == fromCfg.getModuleType() && cfg.getModuleQuality() == fromCfg.getModuleQuality() && cfg.getTroopsTypeSet().contains(to)) {
				HawkLog.logPrintln("SoldierExchangeUtil changeModule, openid: {}, playerId: {}, suitId: {}, slotType: {}, moduleId: {}, to moduleId: {}, from: {}, to: {}-{}", 
						player.getOpenId(), player.getId(), suitId, slotType, fromCfg.getId(), cfg.getId(), sourceFrom, sourceTo, cfg.getTroopsTypeSet());
				module.setCfgId(cfg.getId());
				break;
			}
		}
		for (MechaCoreModuleEffObject effObject : module.getRandomAttrEff()) {
			changeModuleRandomAttr(effObject, suitId, slotType, fromCfg.getId(), module.getCfgId(), sourceFrom, sourceTo);
		}
	}

	/**
	 * 机甲核心模块属性转换
	 * @param effObject
	 * @param from
	 * @param to
	 */
	private void changeModuleRandomAttr(MechaCoreModuleEffObject effObject, int suitId, int slotType, int moduleId, int toModuleId, int from, int to) {
		int fromAttrId = effObject.getAttrId();
		MechaCoreModuleAddtionalCfg fromCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, fromAttrId);
		if (fromCfg == null) {
			return;
		}
		if (!fromCfg.getTroopsTypeSet().contains(from) && !fromCfg.getTroopsTypeSet().contains(to)) {
			return;
		}
		if (fromCfg.getTroopsTypeSet().contains(from) && fromCfg.getTroopsTypeSet().contains(to)) {
			return;
		}
		if (fromCfg.getTroopsTypeSet().contains(to)) {
			int tmp = to;
			to = from;
			from = tmp;
		}
		
		for (int cfgId : MechaCoreModuleAddtionalCfg.getCfgIdByQuality(fromCfg.getQuality())) {
			MechaCoreModuleAddtionalCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, cfgId);
			if (cfg.getQualitySort() != fromCfg.getQualitySort() || cfg.getEffectGroup() != fromCfg.getEffectGroup() || !cfg.getTroopsTypeSet().contains(to)) {
				continue;
			}
			if (effObject.getEffectValue() < cfg.getRandMin() || effObject.getEffectValue() > cfg.getRandMax()) {
				continue;
			}
			HawkLog.logPrintln("SoldierExchangeUtil changeModuleRandomAttr openid: {}, playerId: {}, suitId: {}, slotType: {}, moduleId: {}, attrId: {}, effectType: {}, value: {}, to toModuleId: {}, attrId: {}, effectType: {}, from: {}, to: {}-{}", 
					player.getOpenId(), player.getId(), suitId, slotType, moduleId, fromAttrId, effObject.getEffectType(), effObject.getEffectValue(), 
					toModuleId, cfg.getId(), cfg.getEffect(), from, to, cfg.getTroopsTypeSet());
			effObject.setAttrId(cfg.getId());
			effObject.setEffectType(cfg.getEffect());
			break;
		}
	}

	public Player getPlayer() {
		return player;
	}

	public SoldierType getFromType() {
		return fromType;
	}

	public SoldierType getToType() {
		return toType;
	}

	public PBSEResp.Builder getResp() {
		resp.setCreateTime(HawkTime.getMillisecond());
		resp.setFromType(fromType);
		resp.setToType(toType);
		resp.setFromInfo(fromInfo);
		resp.setToInfo(toInfo);
		return resp;
	}

	public PBSEInfo.Builder getFromInfo() {
		return fromInfo;
	}

	public PBSEInfo.Builder getToInfo() {
		return toInfo;
	}

}
