package com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.cfg.PlantCrystalAnalysisCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantCrystalAnalysis;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantCrystalAnalysisChip;
import com.hawk.game.service.BuildingService;

/**
 * 晶体分析阶段 
 * @author lwt
 * @date 2022年2月10日
 */
public class PlantCrystalAnalysis {
	private int cfgId;
	private boolean unlock;
	private ImmutableList<CrystalAnalysisChip> chips;
	// ----------------------------------------------------------------------------------------//
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	private PlantSoldierSchool parent;

	private int techPower;
	private boolean efvalLoad;

	public PlantCrystalAnalysis(PlantSoldierSchool school) {
		this.parent = school;
	}

	/** 已强化满级*/
	public boolean isMax() {
		if (getCfg().getPostStage() != 0) {
			return false;
		}

		for (CrystalAnalysisChip chip : chips) {
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
		chips.stream().map(CrystalAnalysisChip::serializ).forEach(arr::add);

		result.put("chips", arr);
		return result.toJSONString();
	}

	public void mergeFrom(String jsonstr) {
		JSONObject result = JSONObject.parseObject(jsonstr);
		this.cfgId = result.getIntValue("cfgId");
		List<CrystalAnalysisChip> list = new ArrayList<>();
		JSONArray arr = result.getJSONArray("chips");
		arr.forEach(str -> {
			CrystalAnalysisChip slot = new CrystalAnalysisChip(this);
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
		for (CrystalAnalysisChip chip : chips) {
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
		for (CrystalAnalysisChip chip : chips) {
			result += chip.getCfg().getPower();
		}
		return result;
	}

	public PBPlantCrystalAnalysis toPBobj() {
		PBPlantCrystalAnalysis.Builder builder = PBPlantCrystalAnalysis.newBuilder();
		builder.setUnlock(unlock);
		builder.setCfgId(cfgId);
		for (CrystalAnalysisChip chip : chips) {
			builder.addChips(PBPlantCrystalAnalysisChip.newBuilder().setCfgId(chip.getCfgId()));
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

		Optional<PlantSoldierCrack> maxC = parent.getCracks().stream().filter(PlantSoldierCrack::isMax).findAny();
		if (!maxC.isPresent()) {
			unlock = false;
			return;
		}

		unlock = true;
		this.notifyChange();
	}

	public void initChips() {
		List<CrystalAnalysisChip> list = new ArrayList<>();
		PlantCrystalAnalysisCfg heroCfg = getCfg();
		for (int chipId : heroCfg.getChipIds()) {
			CrystalAnalysisChip slot = new CrystalAnalysisChip(this);
			slot.setCfgId(chipId);
			list.add(slot);
		}
		this.chips = ImmutableList.copyOf(list);
	}

	public PlantCrystalAnalysisCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(PlantCrystalAnalysisCfg.class, cfgId);
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

	public ImmutableList<CrystalAnalysisChip> getChips() {
		return chips;
	}

	public CrystalAnalysisChip getChipById(int chipId) {
		for (CrystalAnalysisChip chip : chips) {
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
