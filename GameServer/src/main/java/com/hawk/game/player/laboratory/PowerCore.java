package com.hawk.game.player.laboratory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.LaboratoryCoreCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerCoreIndex;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Laboratory.PBLaboryCore;

public class PowerCore implements SerializJsonStrAble {
	private PowerCoreIndex index;
	private int coreCfgId;
	private final Laboratory parent;
	private int preSetCfgId; // 准备替换的id

	private PowerCore(Laboratory laboratory) {
		this.parent = laboratory;
	}

	/** 技能提供作用号值
	 * 
	 * @return */
	public Map<EffType, Integer> effectVal() {
		Map<EffType, Integer> result = new HashMap<>();
		LaboratoryCoreCfg cfg = getInsCfg(coreCfgId);
		if (Objects.nonNull(cfg)) {
			for (HawkTuple2<EffType, Integer> tup : cfg.getBuff()) {
				result.merge(tup.first, tup.second, (v1, v2) -> v1 + v2);
			}
		}
		return result;
	}

	private LaboratoryCoreCfg getInsCfg(int cfgid) {
		LaboratoryCoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LaboratoryCoreCfg.class, cfgid);
		return cfg;
	}

	/** 总等级*/
	public int getLevel() {
		int result = 0;
		LaboratoryCoreCfg cfg = getInsCfg(coreCfgId);
		if (Objects.nonNull(cfg)) {
			result = cfg.getLevel();
		}
		return result;
	}

	public int getLockEnergyNum() {
		int result = 0;
		LaboratoryCoreCfg cfg = getInsCfg(coreCfgId);
		if (Objects.nonNull(cfg)) {
			result = cfg.getLockNum();
		}
		return result;
	}

	public int getPreLockEnergyNum() {
		int result = 0;
		LaboratoryCoreCfg cfg = getInsCfg(preSetCfgId);
		if (Objects.nonNull(cfg)) {
			result = cfg.getLockNum();
		}
		return result;
	}

	public static PowerCore create(Laboratory laboratory, PowerCoreIndex type) {
		PowerCore result = new PowerCore(laboratory);
		result.index = type;
		result.coreCfgId = 0;
		return result;

	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("index", index.INT_VAL);
		obj.put("bid", coreCfgId);

		return obj.toJSONString();

	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.index = PowerCoreIndex.valueOf(obj.getIntValue("index"));
		this.coreCfgId = obj.getIntValue("bid");
		this.preSetCfgId = coreCfgId;
	}

	public PowerCoreIndex getType() {
		return index;
	}

	public void setType(PowerCoreIndex type) {
		throw new RuntimeException();
	}

	public Laboratory getParent() {
		return parent;
	}

	public int getCoreCfgId() {
		return coreCfgId;
	}

	public void setCoreCfgId(int coreCfgId) {
		this.coreCfgId = coreCfgId;
	}

	public int getPreSetCfgId() {
		return preSetCfgId;
	}

	public void setPreSetCfgId(int preSetCfgId) {
		this.preSetCfgId = preSetCfgId;
	}

	public PBLaboryCore toPBObj() {
		return PBLaboryCore.newBuilder().setIndex(getType().INT_VAL).setCoreCfgId(getCoreCfgId()).build();
	}

}
