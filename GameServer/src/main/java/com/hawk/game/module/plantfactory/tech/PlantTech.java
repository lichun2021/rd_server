package com.hawk.game.module.plantfactory.tech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.PlantTechnologyCfg;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantFactory.PBPlantTech;
import com.hawk.game.protocol.PlantFactory.PBPlantTechChip;
import com.hawk.log.LogConst.PowerChangeReason;

public class PlantTech {
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	private PlantTechEntity techEntity;

	private ImmutableList<TechChip> chips;
	private int techPower;
	private boolean efvalLoad;

	private PlantTech(PlantTechEntity entity) {
		this.techEntity = entity;
	}

	public static PlantTech create(PlantTechEntity entity) {
		PlantTech tech = new PlantTech(entity);
		tech.init();
		entity.recordTechObj(tech);
		return tech;
	}

	private void init() {
		chips = ImmutableList.copyOf(loadChips());

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
		effmap.putAll(getCfg().getEffectList());
		for (TechChip chip : chips) {
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
		techEntity.notifyUpdate();

		efvalLoad = false;
		this.loadEffVal(); // 做号用变更,如删除技能
		if (!effValMap.isEmpty()) {
			getParent().getEffect().syncEffect(getParent(), effValMap.keySet().toArray(new EffType[0]));
		}
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.PLANT_TECH_UPDATE_S, this.toPBobj().toBuilder()));
		if (techPower > 0) {
			getParent().refreshPowerElectric(PowerChangeReason.PLANTTECH_CHANGE);
		}
	}

	private int power() {
		int result = 0;
		result += getCfg().getPower();
		for (TechChip chip : chips) {
			result += chip.getCfg().getPower();
		}
		return result;
	}

	public PBPlantTech toPBobj() {
		PBPlantTech.Builder builder = PBPlantTech.newBuilder();
		builder.setBuildType(BuildingType.valueOf(techEntity.getBuildType()));
		builder.setCfgId(techEntity.getCfgId());
		for (TechChip chip : chips) {
			builder.addChips(PBPlantTechChip.newBuilder().setCfgId(chip.getCfgId()));
		}
		return builder.build();
	}

	private List<TechChip> loadChips() {
		if (StringUtils.isEmpty(techEntity.getChipSerialized())) {// 新英雄
			return initSkill();
		}

		List<TechChip> list = new ArrayList<>();
		JSONArray arr = JSONArray.parseArray(techEntity.getChipSerialized());
		arr.forEach(str -> {
			TechChip slot = new TechChip(this);
			slot.mergeFrom(str.toString());
			list.add(slot);
		});
		return list;
	}

	private List<TechChip> initSkill() {
		List<TechChip> list = new ArrayList<>();
		PlantTechnologyCfg heroCfg = getCfg();
		for (int chipId : heroCfg.getChipIds()) {
			TechChip slot = new TechChip(this);
			slot.setCfgId(chipId);
			list.add(slot);
		}
		return list;
	}

	/** 序列化skill */
	public String serializChips() {
		JSONArray arr = new JSONArray();
		chips.stream().map(TechChip::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	public PlantTechnologyCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyCfg.class, techEntity.getCfgId());
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(techEntity.getPlayerId());
	}

	public ImmutableMap<EffType, Integer> getEffValMap() {
		return effValMap;
	}

	public PlantTechEntity getTechEntity() {
		return techEntity;
	}

	public ImmutableList<TechChip> getChips() {
		return chips;
	}

	public TechChip getChipById(int chipId) {
		for (TechChip chip : chips) {
			if (chip.getCfgId() == chipId) {
				return chip;
			}
		}
		return null;
	}

	public Object getCfgId() {
		return techEntity.getCfgId();
	}

	public void setCfgId(int cfgId) {
		techEntity.setCfgId(cfgId);

	}

	public int getTechPower() {
		return techPower;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("cfgId", getCfgId())
				.add("chipSerialized", serializChips())
				.toString();
	}

}
