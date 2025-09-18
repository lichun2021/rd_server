package com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg.PlantSoldierStrengthenTechCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSOLdierStrength;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSOLdierStrengthTech;
import com.hawk.game.service.BuildingService;

/**
 * 泰能战士强化
 * @author lwt
 * @date 2022年2月10日
 */
public class SoldierStrengthen implements SerializJsonStrAble {
	private int cfgId;
	private int level;
	private boolean unlock;
	private List<SoldierStrengthenTech> chips = new CopyOnWriteArrayList<>();
	// ----------------------------------------------------------------------------------------//
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	private PlantSoldierSchool parent;

	private int techPower;
	private boolean efvalLoad;

	public SoldierStrengthen(PlantSoldierSchool school) {
		this.parent = school;
	}

	public SoldierType getSoldierType() {
		return getCfg().getType();
	}

	/** 序列化 */
	@Override
	public String serializ() {
		JSONObject result = new JSONObject();
		result.put("cfgId", cfgId);
		result.put("level", level);
		result.put("unlock", unlock);
		JSONArray arr = new JSONArray();
		chips.stream().map(SoldierStrengthenTech::serializ).forEach(arr::add);

		result.put("chips", arr);
		return result.toJSONString();
	}

	@Override
	public void mergeFrom(String jsonstr) {

		JSONObject result = JSONObject.parseObject(jsonstr);
		this.cfgId = result.getIntValue("cfgId");
		this.level = result.getIntValue("level");
		this.unlock = result.getBooleanValue("unlock");
		List<SoldierStrengthenTech> list = new ArrayList<>();
		JSONArray arr = result.getJSONArray("chips");
		arr.forEach(str -> {
			SoldierStrengthenTech slot = new SoldierStrengthenTech(this);
			slot.mergeFrom(str.toString());
			list.add(slot);
		});

		this.chips = list;
	}

	public boolean isEfvalLoad() {
		return efvalLoad;
	}

	public void loadEffVal() {
		if (efvalLoad) {
			return;
		}

		// 重新推送所有做用号
		Map<EffType, Integer> effmap = new HashMap<>();
		for (SoldierStrengthenTech chip : chips) {
			for (Entry<EffType, Integer> ent : chip.getCfg().getEffectList().entrySet()) {
				effmap.merge(ent.getKey(), ent.getValue(), (v1, v2) -> v1 + v2);
			}
		}
		effValMap = ImmutableMap.copyOf(effmap);
		efvalLoad = true;
		this.techPower = power();
	}

	/**
	 * 通知英雄数据有变化
	 */
	public void notifyChange() {
		efvalLoad = false;
		level = getPlantStrengthLevel();
		this.loadEffVal(); // 做号用变更,如删除技能

		parent.setChanged(true);
		if (!effValMap.isEmpty()) {
			Player player = getParent().getParent();
			player.getEffect().syncEffect(player, effValMap.keySet().toArray(new EffType[0]));
		}

	}

	private int power() {
		int result = 0;
		for (SoldierStrengthenTech chip : chips) {
			result += chip.getCfg().getBattlePoint();
		}
		return result;
	}

	public PBPlantSOLdierStrength toPBobj() {
		PBPlantSOLdierStrength.Builder builder = PBPlantSOLdierStrength.newBuilder();
		builder.setUnlock(unlock);
		builder.setCfgId(cfgId);
		builder.setSoldierType(getCfg().getType());
		builder.setLevel(level);
		for (SoldierStrengthenTech chip : chips) {
			builder.addTechs(PBPlantSOLdierStrengthTech.newBuilder().setCfgId(chip.getCfgId()));
		}
		return builder.build();
	}

	public void checkUnlock() {
		if (unlock) {
			return;
		}

		if (!parent.getCrystal().isMax()) {
			unlock = false;
			return;
		}
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(getParent().getParent(), getCfg().getFrontBuildIds(), null, 0)) {
			unlock = false;
			return;
		}

		if (!parent.getSoldierCrackByType(getSoldierType()).isMax()) {
			unlock = false;
			return;
		}

		unlock = true;
		this.notifyChange();
	}

	public PlantSoldierStrengthenCfg getCfg() {
		PlantSoldierStrengthenCfg config = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenCfg.class, cfgId);
		HawkAssert.notNull(config, "PlantSoldierStrengthenCfg null id:" + cfgId);
		return config;
	}

	public PlantSoldierSchool getParent() {
		return parent;
	}

	public int getEffVal(EffType eff) {
		if (!efvalLoad) {
			loadEffVal();
		}
		return effValMap.getOrDefault(eff, 0);
	}

	public List<SoldierStrengthenTech> getChips() {
		return chips;
	}

	public SoldierStrengthenTech getChipById(int chipId) {
		for (SoldierStrengthenTech chip : chips) {
			if (chip.getCfgId() == chipId) {
				return chip;
			}
		}
		return null;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getPower() {
		return techPower;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("cfgId", getCfgId())
				.add("chipSerialized", serializ())
				.toString();
	}

	public boolean isUnlock() {
		return unlock;
	}

	public SoldierStrengthenTech getTechByTechId(int techId) {
		return chips.stream().filter(tech -> tech.getTechId() == techId).findAny().orElse(null);
	}

	/**
	 * 检查科技研究条件
	 * @param techCfg
	 * @return
	 */
	public boolean checkCondition(PlantSoldierStrengthenTechCfg techCfg) {

		SoldierStrengthenTech tech = getTechByTechId(techCfg.getTechId());
		// 科技已达到对应的等级
		if (Objects.nonNull(tech) && tech.getLevel() >= techCfg.getLevel()) {
			return false;
		}

		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(getParent().getParent(), techCfg.getFrontBuildIds(), null, 0)) {
			return false;
		}

		for (int condition : techCfg.getConditionTechList()) {
			PlantSoldierStrengthenTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenTechCfg.class, condition);
			if (cfg == null) {
				return false;
			}
			SoldierStrengthenTech entity = getTechByTechId(cfg.getTechId());
			boolean match = false;
			if (entity != null && entity.getLevel() >= cfg.getLevel()) {
				match = true;
			}
			if (!match) {
				return false;
			}
		}
		return true;
	}

	public void techUpGrade(PlantSoldierStrengthenTechCfg techCfg) {
		SoldierStrengthenTech tech = getTechByTechId(techCfg.getTechId());
		if (Objects.isNull(tech)) {
			SoldierStrengthenTech slot = new SoldierStrengthenTech(this);
			slot.setCfgId(techCfg.getId());
			chips.add(slot);
		} else {
			tech.setCfgId(techCfg.getId());
		}

	}

	/**
	 * 技能强化等级
	 * @return
	 */
	public int getPlantStrengthLevel() {
		int level = 0;
		for (int cfgId : getCfg().getStepTechIds()) {
			PlantSoldierStrengthenTechCfg techcfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenTechCfg.class, cfgId);
			SoldierStrengthenTech tech = getTechByTechId(techcfg.getTechId());
			if (Objects.nonNull(tech) && tech.getLevel() >= techcfg.getLevel()) {
				level += 1;
			}
		}
		return level;
	}

}
