package com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/plant_soldier_crack_chip.xml")
public class PlantSoldierCrackChipCfg extends HawkConfigBase {
	// id="10010105" level="4" frontStage="10010104" postStage="0" buildCost="10000_1008_100" effect="212_10000,221_100000"
	@Id
	protected final int id;
	// 类型
	protected final int level;
	protected final int power;

	/** 上一等级(阶段)建筑*/
	private final int frontStage;

	/** 下一等级(阶段)建筑*/
	private final int postStage;

	// 资源消耗 type_id_count,type_id_count
	protected final String buildCost;

	protected final String effect;

	protected final String atkAttr;
	protected final String hpAttr;
	
	private Map<EffType, Integer> effectList;

	public PlantSoldierCrackChipCfg() {
		id = 0;
		level = 0;
		effect = "";
		frontStage = 0;
		postStage = 0;
		buildCost = "";
		power = 0;
		atkAttr = "";
		hpAttr = "";
	}

	@Override
	protected boolean assemble() {
		Map<EffType, Integer> map = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			List<String> array = Splitter.on("|").omitEmptyStrings().splitToList(effect);
			for (String val : array) {
				String[] info = val.split("_");
				map.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(map);

		return true;
	}

	@Override
	protected boolean checkValid() {
		if (frontStage != 0) {
			PlantSoldierCrackChipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackChipCfg.class, frontStage);
			if (cfg == null) {
				HawkLog.errPrintln("plantFactoryCfg check valid failed, id: {}, frontStage: {}", id, frontStage);
				return false;
			}
		}
		if (postStage != 0) {
			PlantSoldierCrackChipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierCrackChipCfg.class, postStage);
			if (cfg == null) {
				HawkLog.errPrintln("plantFactoryCfg check valid failed, id: {}, postStage: {}", id, postStage);
				return false;
			}
		}

		return true;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getFrontStage() {
		return frontStage;
	}

	public int getPostStage() {
		return postStage;
	}

	public String getBuildCost() {
		return buildCost;
	}

	public String getEffect() {
		return effect;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public int getPower() {
		return power;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
