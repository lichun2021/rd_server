package com.hawk.game.player.laboratory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.LaboratoryKVCfg;
import com.hawk.game.config.LaboratoryPageCfg;
import com.hawk.game.entity.LaboratoryEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerCoreIndex;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Laboratory.PBLaboryCore;
import com.hawk.game.protocol.Laboratory.PBLaboryPageInfo;

/**一页*/
public class Laboratory {
	private LaboratoryEntity dbEntity;
	private ImmutableMap<PowerCoreIndex, PowerCore> powerCores;
	private PowerBlock powerBlock;
	private boolean efvalLoad;

	private ImmutableMap<EffType, Integer> effVal;

	public Laboratory(LaboratoryEntity laboratoryEntity) {
		this.dbEntity = laboratoryEntity;
	}

	protected void init() {
		loadPowerCore();
		loadPowerBlock();
	}

	public static Laboratory create(LaboratoryEntity laboratoryEntity) {
		Laboratory labr = new Laboratory(laboratoryEntity);
		labr.init();
		laboratoryEntity.recordLabObj(labr);
		return labr;
	}
	
	public void pageUnlock(){
		dbEntity.setPageUnlock(1);
	}
	
	public boolean isPageUnlock() {
		return dbEntity.getPageUnlock() > 0 || StringUtils.isEmpty(getPageCfg().getUnlockCost());
	}
	
	public LaboratoryPageCfg getPageCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(LaboratoryPageCfg.class, dbEntity.getPageIndex());
	}

	public PBLaboryPageInfo toPbObj() {
		PBLaboryPageInfo.Builder builder = PBLaboryPageInfo.newBuilder();
		builder.setIndex(getIndex());
		builder.setCoreLevelSum(getPowerCoreLevel());
		builder.setLockNum(getPowerCoreLockEnergyNum());
		for (Entry<PowerCoreIndex, PowerCore> ent : powerCores.entrySet()) {
			PowerCore core = ent.getValue();
			PBLaboryCore pbcore = core.toPBObj();
			builder.addCore(pbcore);
		}
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		builder.setBlockUnlock(kvCfg.isBlockOpen());
		builder.addAllBlock(powerBlock.toPBObjs());
		builder.setPageUnlock(isPageUnlock());
		return builder.build();
	}

	public PowerCore getPowerCore(PowerCoreIndex type) {
		return powerCores.get(type);
	}
	
	public ImmutableMap<PowerCoreIndex, PowerCore> getPowerCores() {
		return powerCores;
	}

	public PowerBlock getPowerBlock() {
		return powerBlock;
	}

	@SuppressWarnings("unused")
	private void setPowerBlock(PowerBlock powerBlock) {
		throw new UnsupportedOperationException();
	}

	public int getEffVal(EffType eff) {
		return effVal.getOrDefault(eff, 0);
	}

	public int getIndex() {
		return dbEntity.getPageIndex();
	}

	public void loadEffVal() {
		if (efvalLoad) {
			return;
		}
		Map<EffType, Integer> __effValMap = new HashMap<>(10);
		for (PowerCore core : powerCores.values()) {
			mergeEffval(__effValMap, core.effectVal());
		}

		mergeEffval(__effValMap, powerBlock.effectVal());

		effVal = ImmutableMap.copyOf(__effValMap);
		efvalLoad = true;

	}

	private void mergeEffval(Map<EffType, Integer> __effVal, Map<EffType, Integer> map) {
		for (Entry<EffType, Integer> effVal : map.entrySet()) {
			EffType type = effVal.getKey();
			__effVal.merge(type, effVal.getValue(), (v1, v2) -> v1 + v2);
		}
	}

	public int getPowerCoreLevel() {
		return powerCores.values().stream().mapToInt(PowerCore::getLevel).sum();
	}

	public int getPowerCorePreLockEnergyNum() {
		return powerCores.values().stream().mapToInt(PowerCore::getPreLockEnergyNum).sum();
	}

	public int getPowerCoreLockEnergyNum() {
		return powerCores.values().stream().mapToInt(PowerCore::getLockEnergyNum).sum();
	}

	public boolean isEfvalLoad() {
		return efvalLoad;
	}

	public void notifyChanged() {
		efvalLoad = false;
		dbEntity.notifyUpdate();
		// 重新推送所有做用号
		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(effVal.keySet());
		this.loadEffVal(); // 做号用变更,如删除技能
		allEff.addAll(effVal.keySet());

		getParent().getEffect().syncEffect(getParent(), allEff.toArray(new EffType[allEff.size()]));
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code.LABORATORY_PAGE_UPDATE, this.toPbObj().toBuilder()));
		Player player = getParent();
		HawkLog.logPrintln("Laboratory changed pname:{} pid:{} core:{} block:{}", player.getName(), player.getId(), serializPowerCore(), serializPowerBlock());
	}

	private void loadPowerCore() {
		if (StringUtils.isEmpty(dbEntity.getPowerCoreStr())) {// 新英雄
			Map<PowerCoreIndex, PowerCore> map = new HashMap<>();
			for (PowerCoreIndex type : PowerCoreIndex.values()) {
				map.put(type, PowerCore.create(this, type));
			}
			this.powerCores = ImmutableMap.copyOf(map);
			return;
		}

		Map<PowerCoreIndex, PowerCore> map = new HashMap<>();
		JSONArray arr = JSONArray.parseArray(dbEntity.getPowerCoreStr());
		arr.forEach(str -> {
			PowerCore slot = PowerCore.create(this, null);
			slot.mergeFrom(str.toString());
			map.put(slot.getType(), slot);
		});
		this.powerCores = ImmutableMap.copyOf(map);
	}

	public String serializPowerCore() {
		JSONArray arr = new JSONArray();
		powerCores.values().stream().map(PowerCore::serializ).forEach(arr::add);
		return arr.toJSONString();
	}

	private void loadPowerBlock() {
		if (StringUtils.isEmpty(dbEntity.getPowerBlockStr())) {// 新英雄
			this.powerBlock = PowerBlock.create(this);
			return;
		}

		PowerBlock bolck =  PowerBlock.create(this);
		bolck.mergeFrom(dbEntity.getPowerBlockStr());
		this.powerBlock = bolck;
	}

	public String serializPowerBlock() {
		return powerBlock.serializ();
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(dbEntity.getPlayerId());
	}

	public ImmutableMap<EffType, Integer> getEffVal() {
		return effVal;
	}

	public LaboratoryEntity getDbEntity() {
		return dbEntity;
	}

}
