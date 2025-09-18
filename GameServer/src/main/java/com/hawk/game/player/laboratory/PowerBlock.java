package com.hawk.game.player.laboratory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.LaboratoryBlockCfg;
import com.hawk.game.config.LaboratoryKVCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerBlockIndex;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Laboratory.PBLaboryBlock;

public class PowerBlock implements SerializJsonStrAble {
	private Map<PowerBlockIndex, Integer> talentMap;

	// 随机到没有确定的
	private Map<PowerBlockIndex, Integer> pretalentMap;
	private final Laboratory parent;

	private PowerBlock(Laboratory laboratory) {
		this.parent = laboratory;
	}

	public static PowerBlock create(Laboratory laboratory) {
		PowerBlock result = new PowerBlock(laboratory);
		result.talentMap = new HashMap<>();
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			result.talentMap.put(index, 0);
		}

		return result;
	}

	public void preRemake() {
		pretalentMap = new HashMap<>();
	}

	public void randomBlock(PowerBlockIndex index) {
		HawkAssert.notNull(pretalentMap, "preRemake() first!");
		if (!isIndexUnlock(index)) {
			return;
		}
		Set<Integer> otherTypes = new HashSet<>(6);
		for (PowerBlockIndex i : PowerBlockIndex.values()) {
			if (i == index) {
				continue;
			}
			LaboratoryBlockCfg preblockCfg = getPreBlockCfg(i);
			if (Objects.nonNull(preblockCfg)) {
				otherTypes.add(preblockCfg.getType());
			}
			LaboratoryBlockCfg blockCfg = getBlockCfg(i);
			if (Objects.nonNull(blockCfg)) {
				otherTypes.add(blockCfg.getType());
			}
		}
		List<LaboratoryBlockCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(LaboratoryBlockCfg.class).stream()
				.filter(cfg -> !otherTypes.contains(cfg.getType()))
				.collect(Collectors.toList());
		LaboratoryBlockCfg slectedCfg = HawkRand.randomWeightObject(cfgList);
		pretalentMap.put(index, slectedCfg.getId());
	}

	public void remakeBlock() {
		HawkAssert.notNull(pretalentMap, "preRemake() first!");
		this.talentMap.putAll(pretalentMap);
		pretalentMap = null;
	}

	/** 技能提供作用号值
	 * 
	 * @return */
	public Map<EffType, Integer> effectVal() {
		Map<EffType, Integer> result = new HashMap<>();
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			LaboratoryBlockCfg cfg = getBlockCfg(index);
			boolean indexUnlock = isIndexUnlock(index);
			if (indexUnlock && Objects.isNull(cfg)) { // 解锁 随机给属性
				preRemake();
				randomBlock(index);
				remakeBlock();
				cfg = getBlockCfg(index);
			}
			if (indexUnlock && Objects.nonNull(cfg)) {
				for (HawkTuple2<EffType, Integer> tup : cfg.getBuff()) {
					result.merge(tup.first, tup.second, (v1, v2) -> v1 + v2);
				}
			}
		}
		return result;
	}
	
	public LaboratoryBlockCfg getPreBlockCfg(PowerBlockIndex index) {
		LaboratoryBlockCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LaboratoryBlockCfg.class, pretalentMap.getOrDefault(index, 0));
		return cfg;
	}

	public LaboratoryBlockCfg getBlockCfg(PowerBlockIndex index) {
		LaboratoryBlockCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LaboratoryBlockCfg.class, talentMap.getOrDefault(index, 0));
		return cfg;
	}

	@Override
	public String serializ() {
		JSONObject tlent = new JSONObject();
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			tlent.put(index.INT_VAL + "", talentMap.getOrDefault(index, 0));
		}

		return tlent.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject tlent = JSONObject.parseObject(serialiedStr);
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			talentMap.put(index, tlent.getIntValue(index.INT_VAL + ""));
		}
	}

	public Laboratory getParent() {
		return parent;
	}

	public Map<PowerBlockIndex, Integer> getPretalentMap() {
		return pretalentMap;
	}

	public boolean isIndexUnlock(PowerBlockIndex index) {
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		if (!kvCfg.isBlockOpen()) {
			return false;
		}
		
		if (kvCfg.getUnlockLevel().first > getParent().getPowerCoreLevel()) {
			return false;
		}

		if (Objects.nonNull(getBlockCfg(index))) {
			return true;
		}

		switch (index) {
		case ONE:
			return kvCfg.getUnlockLevel().first <= getParent().getPowerCoreLevel();
		case TWO:
			return kvCfg.getUnlockLevel().second <= getParent().getPowerCoreLevel();
		case THREE:
			return kvCfg.getUnlockLevel().third <= getParent().getPowerCoreLevel();
		case FOUR:
			return kvCfg.getUnlockLevel().fourth <= getParent().getPowerCoreLevel();
		default:
			break;
		}
		return false;
	}

	public List<PBLaboryBlock> toPBObjs() {
		List<PBLaboryBlock> list = new ArrayList<>(3);
		for (Entry<PowerBlockIndex, Integer> bi : talentMap.entrySet()) {
			PBLaboryBlock builder = PBLaboryBlock.newBuilder()
					.setIndex(bi.getKey().INT_VAL)
					.setCfgId(bi.getValue())
					.setUnlock(isIndexUnlock(bi.getKey()))
					.build();
			list.add(builder);
		}
		return list;
	}
	
	public Map<PowerBlockIndex, Integer> getTalentMap() {
		return talentMap;
	}

}
