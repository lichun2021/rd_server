package com.hawk.game.module.plantsoldier.strengthen.soldierCrack;

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
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierCrack;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantSoldierCrackChip;
import com.hawk.game.service.BuildingService;

/**
 * 泰能战士破译阶段破译
 * @author lwt
 * @date 2022年2月10日
 */
public class PlantSoldierCrack implements SerializJsonStrAble {
	private int cfgId;
	private boolean unlock;
	private ImmutableList<CrackChip> chips;
	// ----------------------------------------------------------------------------------------//
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	private PlantSoldierSchool parent;

	private int techPower;
	private boolean efvalLoad;

	public PlantSoldierCrack(PlantSoldierSchool school) {
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
		result.put("unlock", unlock);
		JSONArray arr = new JSONArray();
		chips.stream().map(CrackChip::serializ).forEach(arr::add);

		result.put("chips", arr);
		return result.toJSONString();
	}

	/** 已强化满级*/
	public boolean isMax() {
		if (getCfg().getPostStage() != 0) {
			return false;
		}

		for (CrackChip chip : chips) {
			if (chip.getCfg().getLevel() < getCfg().getMaxChipLevel()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void mergeFrom(String jsonstr) {

		JSONObject result = JSONObject.parseObject(jsonstr);
		this.cfgId = result.getIntValue("cfgId");
		this.unlock = result.getBooleanValue("unlock");
		List<CrackChip> list = new ArrayList<>();
		JSONArray arr = result.getJSONArray("chips");
		arr.forEach(str -> {
			CrackChip slot = new CrackChip(this);
			slot.mergeFrom(str.toString());
			list.add(slot);
		});

		this.chips = ImmutableList.copyOf(list);
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
		for (CrackChip chip : chips) {
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
		for (CrackChip chip : chips) {
			result += chip.getCfg().getPower();
		}
		return result;
	}

	public PBPlantSoldierCrack toPBobj() {
		PBPlantSoldierCrack.Builder builder = PBPlantSoldierCrack.newBuilder();
		builder.setUnlock(unlock);
		builder.setCfgId(cfgId);
		builder.setSoldierType(getCfg().getType());
		for (CrackChip chip : chips) {
			builder.addChips(PBPlantSoldierCrackChip.newBuilder().setCfgId(chip.getCfgId()));
		}
		return builder.build();
	}

	public void checkUnlock() {
		if (unlock) {
			return;
		}

		if (!parent.getInstrument().isMax()) {
			unlock = false;
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
		List<CrackChip> list = new ArrayList<>();
		PlantSoldierCrackCfg heroCfg = getCfg();
		for (int chipId : heroCfg.getChipIds()) {
			CrackChip slot = new CrackChip(this);
			slot.setCfgId(chipId);
			list.add(slot);
		}
		this.chips = ImmutableList.copyOf(list);
	}

	public PlantSoldierCrackCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackCfg.class, cfgId);
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

	public ImmutableList<CrackChip> getChips() {
		return chips;
	}

	public CrackChip getChipById(int chipId) {
		for (CrackChip chip : chips) {
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

}
