package com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantInstrument;
import com.hawk.game.service.BuildingService;

/**
 * 破译仪器
 * @author lwt
 * @date 2022年2月10日
 */
public class PlantInstrument {
	private int cfgId;
	private boolean unlock;
	private ImmutableList<InstrumentChip> chips;
	// ----------------------------------------------------------------------------------------//
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	private PlantSoldierSchool parent;

	private int techPower;
	private boolean efvalLoad;

	public PlantInstrument(PlantSoldierSchool school) {
		this.parent = school;
	}

	/** 已强化满级*/
	public boolean isMax() {
		if (getCfg().getPostStage() != 0) {
			return false;
		}

		for (InstrumentChip chip : chips) {
			if (chip.getCfg().getLevel() < getCfg().getMaxChipLevel()) {
				return false;
			}
		}
		return true;
	}

	/** 序列化 */
	public String serializ() {
		JSONObject result = new JSONObject();
		result.put("cfgId", cfgId);
		result.put("unlock", unlock);
		JSONArray arr = new JSONArray();
		chips.stream().map(InstrumentChip::serializ).forEach(arr::add);

		result.put("chips", arr);
		return result.toJSONString();
	}

	public void mergeFrom(String jsonstr) {
		JSONObject result = JSONObject.parseObject(jsonstr);
		this.cfgId = result.getIntValue("cfgId");
		List<InstrumentChip> list = new ArrayList<>();
		JSONArray arr = result.getJSONArray("chips");
		arr.forEach(str -> {
			InstrumentChip slot = new InstrumentChip(this);
			slot.mergeFrom(str.toString());
			list.add(slot);
		});

		this.chips = ImmutableList.copyOf(list);
		this.unlock = result.getBooleanValue("unlock");
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
		for (InstrumentChip chip : chips) {
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
		this.loadEffVal(); // 做号用变更,如删除技能

		parent.setChanged(true);
		if (!effValMap.isEmpty()) {
			Player player = getParent().getParent();
			player.getEffect().syncEffect(player, effValMap.keySet().toArray(new EffType[0]));
		}

	}

	private int power() {
		int result = 0;
		result += getCfg().getPower();
		for (InstrumentChip chip : chips) {
			result += chip.getCfg().getPower();
		}
		return result;
	}

	public PBPlantInstrument toPBobj() {
		PBPlantInstrument.Builder builder = PBPlantInstrument.newBuilder();
		builder.setUnlock(unlock);
		builder.setCfgId(cfgId);
		for (InstrumentChip chip : chips) {
			builder.addChips(chip.toPBObj());
		}
		return builder.build();
	}

	public void checkUnlock() {
		if (unlock) {
			return;
		}
		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!BuildingService.getInstance().checkFrontCondition(getParent().getParent(), getCfg().getFrontBuildIds(), null, 0)) {
			unlock = false;
			return;
		}
		unlock = true;
		this.notifyChange();
	}

	public void initChips() {
		List<InstrumentChip> list = new ArrayList<>();
		PlantInstrumentUpgradeCfg heroCfg = getCfg();
		for (int chipId : heroCfg.getChipIds()) {
			InstrumentChip slot = new InstrumentChip(this);
			slot.setCfgId(chipId);
			list.add(slot);
		}
		this.chips = ImmutableList.copyOf(list);
	}

	public PlantInstrumentUpgradeCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantInstrumentUpgradeCfg.class, cfgId);
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

	public ImmutableList<InstrumentChip> getChips() {
		return chips;
	}

	public InstrumentChip getChipById(int chipId) {
		for (InstrumentChip chip : chips) {
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

	public boolean isUnlock() {
		return unlock;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("cfgId", getCfgId())
				.add("chipSerialized", serializ())
				.toString();
	}

}
