package com.hawk.game.module.plantsoldier.strengthen;

import java.util.*;

import com.google.common.collect.ImmutableMap;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.PlantCrystalAnalysis;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.cfg.PlantCrystalAnalysisCfg;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.PlantInstrument;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeCfg;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryCfg;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryCfgV3;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSodlierSync;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSodlierSync.Builder;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 集合强化的4个功能点.功能相关性强统一管理
 * @author lwt
 * @date 2022年2月10日
 */
public class PlantSoldierSchool {
	private boolean changed;
	private PlantSoldierSchoolEntity dbEntity;

	/** 破译仪器*/
	private PlantInstrument instrument;
	/**泰能战士破译阶段 */
	private ImmutableList<PlantSoldierCrack> cracks;
	/**晶体分析阶段*/
	private PlantCrystalAnalysis crystal;
	/** 展示强化阶段*/
	private ImmutableList<SoldierStrengthen> strengthens;
	/** 军衔阶段*/
	private ImmutableList<PlantSoldierMilitary> militaries;
	private ImmutableList<PlantSoldierMilitary> militaries3;
	private Map<Integer, Integer> switchMap;

	public static PlantSoldierSchool create(PlantSoldierSchoolEntity entity) {
		PlantSoldierSchool school = new PlantSoldierSchool(entity);
		school.loadInstrument();
		school.loadCracks();
		school.loadCrystal();
		school.loadStrengthen();
		school.loadMilitary();
		school.loadMilitary3();
		school.loadSwitch();
		return school;
	}

	private PlantSoldierSchool(PlantSoldierSchoolEntity entity) {
		this.dbEntity = entity;
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(dbEntity.getPlayerId());
	}

	public int getEffVal(EffType eff) {
		int result = 0;
		result += instrument.getEffVal(eff);
		for (PlantSoldierCrack cr : cracks) {
			result += cr.getEffVal(eff);
		}
		result += crystal.getEffVal(eff);
		for (SoldierStrengthen ps : strengthens) {
			result += ps.getEffVal(eff);
		}
		for (PlantSoldierMilitary military : militaries) {
			result += military.getEffVal(eff);
		}
		for (PlantSoldierMilitary military : militaries3) {
			result += military.getEffVal(eff);
		}
		return result;
	}

	public int getPower() {
		int result = 0;
		result += instrument.getPower();
		for (PlantSoldierCrack cr : cracks) {
			result += cr.getPower();
		}
		result += crystal.getPower();
		for (SoldierStrengthen ps : strengthens) {
			result += ps.getPower();
		}
		for (PlantSoldierMilitary military : militaries) {
			result += military.getPower();
		}
		for (PlantSoldierMilitary military : militaries3) {
			result += military.getPower();
		}
		return result;
	}

	public void notifyChange(SoldierType updateType) {
		if (!changed) {
			return;
		}
		changed = false;
		dbEntity.notifyUpdate();

		Player player = getParent();
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.PLANTTECH_CHANGE);

		Builder builder = toPBobj().toBuilder();
		if(Objects.nonNull(updateType)){
			builder.setUpdateSoldierType(updateType);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.PLANT_SOLDIER_SYNC, builder));
	}

	public PBPlantSodlierSync toPBobj() {
		PBPlantSodlierSync.Builder builder = PBPlantSodlierSync.newBuilder();
		builder.setShowInstrument(instrument.isUnlock());
		builder.setInstrument(instrument.toPBobj());

		builder.setShowCracks(instrument.isMax());
		cracks.forEach(cr -> builder.addCracks(cr.toPBobj()));

		Optional<PlantSoldierCrack> maxC = cracks.stream().filter(PlantSoldierCrack::isMax).findAny();
		builder.setShowCrystal(maxC.isPresent());
		builder.setCrystal(crystal.toPBobj());

		builder.setShowStrengths(crystal.isMax());
		strengthens.forEach(st -> builder.addStrengths(st.toPBobj()));
		
		militaries.forEach(mi -> builder.addMilitarys(mi.toPBobj()));
		militaries3.forEach(mi -> builder.addMilitarysV3(mi.toPBobj()));
		for(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType type : com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.values()){
			builder.addSwitchItems(toSwitchPB(type));
		}
		return builder.build();
	}

	private void loadStrengthen() {
		List<SoldierStrengthen> list = new ArrayList<>(8);
		if (StringUtils.isEmpty(dbEntity.getStrengthenSerialized())) {
			for (PlantSoldierStrengthenCfg cfg : HawkConfigManager.getInstance().getConfigIterator(PlantSoldierStrengthenCfg.class)) {
				SoldierStrengthen crack = new SoldierStrengthen(this);
				crack.setCfgId(cfg.getId());
				list.add(crack);
			}
		} else {
			JSONArray arr = JSONArray.parseArray(dbEntity.getStrengthenSerialized());
			arr.forEach(str -> {
				SoldierStrengthen crack = new SoldierStrengthen(this);
				crack.mergeFrom(str.toString());
				list.add(crack);
			});
		}
		this.strengthens = ImmutableList.copyOf(list);
		this.strengthens.forEach(SoldierStrengthen::loadEffVal);
	}

	private void loadCrystal() {
		PlantCrystalAnalysis tech = new PlantCrystalAnalysis(this);
		if (StringUtils.isEmpty(dbEntity.getCrystalSerialized())) {// 新英雄
			tech.setCfgId(PlantCrystalAnalysisCfg.getZeroLevelCfg().getId());
			tech.initChips();
		} else {
			tech.mergeFrom(dbEntity.getCrystalSerialized());
		}
		tech.loadEffVal();
		this.crystal = tech;

	}

	private void loadInstrument() {
		PlantInstrument tech = new PlantInstrument(this);
		if (StringUtils.isEmpty(dbEntity.getInstrumentSerialized())) {// 新英雄
			tech.setCfgId(PlantInstrumentUpgradeCfg.getZeroLevelCfg().getId());
			tech.initChips();
		} else {
			tech.mergeFrom(dbEntity.getInstrumentSerialized());
		}
		tech.loadEffVal();
		this.instrument = tech;
	}

	private void loadCracks() {
		List<PlantSoldierCrack> list = new ArrayList<>(8);
		if (StringUtils.isEmpty(dbEntity.getCracksSerialized())) {
			for (PlantSoldierCrackCfg cfg : PlantSoldierCrackCfg.getZeroLevelCfgMap().values()) {
				PlantSoldierCrack crack = new PlantSoldierCrack(this);
				crack.setCfgId(cfg.getId());
				crack.initChips();
				list.add(crack);
			}
		} else {
			JSONArray arr = JSONArray.parseArray(dbEntity.getCracksSerialized());
			arr.forEach(str -> {
				PlantSoldierCrack crack = new PlantSoldierCrack(this);
				crack.mergeFrom(str.toString());
				list.add(crack);
			});
		}
		this.cracks = ImmutableList.copyOf(list);
		this.cracks.forEach(PlantSoldierCrack::loadEffVal);
	}

	/**
	 * 加载泰能军衔
	 */
	private void loadMilitary() {
		List<PlantSoldierMilitary> list = new ArrayList<>(8);
		if (StringUtils.isEmpty(dbEntity.getMilitarySerialized())) {
			ConfigIterator<PlantSoldierMilitaryCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierMilitaryCfg.class);
			while (cfgIter.hasNext()) {
				PlantSoldierMilitaryCfg cfg = cfgIter.next();
				PlantSoldierMilitary military = new PlantSoldierMilitary(this);
				military.setSoldierType(cfg.getSoldierType());
				military.initChips(cfg);
				list.add(military);
			}
		} else {
			JSONArray arr = JSONArray.parseArray(dbEntity.getMilitarySerialized());
			arr.forEach(str -> {
				PlantSoldierMilitary military = new PlantSoldierMilitary(this);
				military.mergeFrom(str.toString());
				list.add(military);
			});
		}
		this.militaries = ImmutableList.copyOf(list);
		this.militaries.forEach(PlantSoldierMilitary::loadEffVal);
	}

	/**
	 * 加载泰能军衔
	 */
	private void loadMilitary3() {
		List<PlantSoldierMilitary> list = new ArrayList<>(8);
		if (StringUtils.isEmpty(dbEntity.getMilitarySerialized3())) {
			ConfigIterator<PlantSoldierMilitaryCfgV3> cfgIter = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierMilitaryCfgV3.class);
			while (cfgIter.hasNext()) {
				PlantSoldierMilitaryCfg cfg = cfgIter.next();
				PlantSoldierMilitary military = new PlantSoldierMilitary(this);
				military.setType(com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3);
				military.setSoldierType(cfg.getSoldierType());
				military.initChips(cfg);
				list.add(military);
			}
		} else {
			JSONArray arr = JSONArray.parseArray(dbEntity.getMilitarySerialized3());
			arr.forEach(str -> {
				PlantSoldierMilitary military = new PlantSoldierMilitary(this);
				military.setType(com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3);
				military.mergeFrom(str.toString());
				list.add(military);
			});
		}
		this.militaries3 = ImmutableList.copyOf(list);
		this.militaries3.forEach(PlantSoldierMilitary::loadEffVal);
	}

	private void loadSwitch() {
		this.switchMap = SerializeHelper.stringToMap(dbEntity.getSwitchInfo(), Integer.class, Integer.class);
	}
	
	public String instrumentSerialize() {
		return instrument.serializ();
	}

	public String crystalSerialize() {
		return crystal.serializ();
	}

	public String cracksSerialize() {
		JSONArray arr = new JSONArray();
		cracks.stream().map(PlantSoldierCrack::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public String strengthenSerialize() {
		JSONArray arr = new JSONArray();
		strengthens.stream().map(SoldierStrengthen::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public String militarySerialize() {
		JSONArray arr = new JSONArray();
		militaries.stream().map(PlantSoldierMilitary::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public String militarySerialize3() {
		JSONArray arr = new JSONArray();
		militaries3.stream().map(PlantSoldierMilitary::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public String switchSerialize() {
		return SerializeHelper.mapToString(switchMap);
	}
	
	public PlantInstrument getInstrument() {
		return instrument;
	}

	public ImmutableList<PlantSoldierCrack> getCracks() {
		return cracks;
	}

	public void checkUnlock() {
		if(getParent().isInDungeonMap()){
			return;
		}
		instrument.checkUnlock();
		if (instrument.isMax()) {
			cracks.forEach(PlantSoldierCrack::checkUnlock);
		}
		crystal.checkUnlock();
		if (crystal.isMax()) {
			strengthens.forEach(SoldierStrengthen::checkUnlock);
		}

	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public PlantSoldierCrack getSoldierCrackByType(SoldierType type) {
		return cracks.stream().filter(cr -> cr.getSoldierType() == type).findAny().orElse(null);
	}

	public SoldierStrengthen getSoldierStrengthenByType(SoldierType type) {
		return strengthens.stream().filter(cr -> cr.getSoldierType() == type).findAny().orElse(null);
	}

	public PlantSoldierMilitary getSoldierMilitaryByType(SoldierType type) {
		return militaries.stream().filter(mi -> mi.getSoldierType() == type).findAny().orElse(null);
	}

	public PlantSoldierMilitary getSoldierMilitary3ByType(SoldierType type) {
		return militaries3.stream().filter(mi -> mi.getSoldierType() == type).findAny().orElse(null);
	}
	
	public PlantCrystalAnalysis getCrystal() {
		return crystal;
	}

	public ImmutableList<SoldierStrengthen> getStrengthens() {
		return strengthens;
	}

	public ImmutableList<PlantSoldierMilitary> getMilitary() {
		return militaries;
	}

	public ImmutableList<PlantSoldierMilitary> getMilitaries3() {
		return militaries3;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("power", getPower())
				.toString();
	}

	public PlantSoldierSchoolEntity getDbEntity() {
		return dbEntity;
	}

	/**任意泰能兵的最高等级*/
	public int getMaxSoldierPlantMilitaryLevel(){
		ConfigIterator<PlantSoldierMilitaryCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierMilitaryCfg.class);
		int maxLevel = 0;
		while (configIterator.hasNext()){
			PlantSoldierMilitaryCfg cfg = configIterator.next();
			int soldierType = cfg.getSoldierType();
			PlantSoldierMilitary military = getSoldierMilitaryByType(SoldierType.valueOf(soldierType));
			PlantSoldierMilitary military3 = getSoldierMilitary3ByType(SoldierType.valueOf(soldierType));
			maxLevel = Math.max(maxLevel, military.getMilitaryLevel() + military3.getMilitaryLevel());
		}
		return maxLevel;
	}

	public Map<Integer, Integer> getSwitchMap() {
		return switchMap;
	}

	public com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchItem.Builder toSwitchPB(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType type){
		com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchItem.Builder builder = com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchItem.newBuilder();
		int state = switchMap.getOrDefault(type.getNumber(), -1);
		builder.setType(type);
		builder.setState(state);
		return builder;
	}

	public int getOutShowSwitchState(){
		if(switchMap == null){
			return -1;
		}
		return switchMap.getOrDefault(com.hawk.game.protocol.PlantSoldierSchool.PBPlantSwitchType.PLANT_SOLDIER_MILITARY_OUT_SHOW_VALUE, -1);
	}
}
