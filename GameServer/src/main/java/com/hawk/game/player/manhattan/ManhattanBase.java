package com.hawk.game.player.manhattan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.hawk.game.config.ManhattanBaseLevelCfg;
import com.hawk.game.config.ManhattanBaseStageCfg;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.protocol.Manhattan.ManhattanPosInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.protocol.World.PresetMarchManhattan;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 超武底座
 * @author lating
 */
public class ManhattanBase extends PlayerManhattan {
	
	public static ManhattanBase create(ManhattanEntity entity) {
		ManhattanBase manhattan = new ManhattanBase(); 
		manhattan.init(entity);
		entity.recordManhattanObj(manhattan);
		return manhattan;
	}
	
	/**
	 * 等级提升
	 * @param posId
	 * @return
	 */
	public void levelUpgrade(int posId) {
		int oldLevel = this.getPosLevel(posId);
		int newLevel = oldLevel + 1;
		ManhattanBaseLevelCfg config = this.getPosLevelCfg(posId, newLevel);
		if (config == null) {
			throw new RuntimeException("wrong invoke");
		}
		updatePosLevel(posId, newLevel);
	}
	
	/**
	 * 品阶提升
	 * @return
	 */
	public void stageUpgrade() {
		int newStage = this.getStage() + 1;
		ManhattanBaseStageCfg config = ManhattanBaseStageCfg.getConfigByStage(newStage);
		if (config == null) {
			throw new RuntimeException("wrong invoke");
		}
		updateStage(newStage);
	}
	
	public int getType() {
		return 0;
	}
	
	public int getPower() {
		int power = 0;
		ManhattanBaseStageCfg stageCfg = this.getStageCfg();
		if (stageCfg != null) {
			power += stageCfg.getPower();
		}
		for(Entry<Integer, Integer> entry : getPosLevelMap().entrySet()) {
			ManhattanBaseLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			if (cfg != null) {
				power += cfg.getPower();
			}
		}
		return power;
	}
	
	public ManhattanBaseStageCfg getStageCfg() {
		return ManhattanBaseStageCfg.getConfigByStage(getStage());
	}
	
	public ManhattanBaseLevelCfg getPosLevelCfg(int posId, int level) {
		return ManhattanBaseLevelCfg.getConfig(posId, level);
	}
	
	/**
	 * 通知变化
	 */
	public void refreshPower() {
		getParent().refreshPowerElectric(PowerChangeReason.MANHATTAN_BASE);
	}
	
	protected Map<Integer, Integer> loadEffValMap(boolean loadSkill){
		return loadEffValMap();
	}
	
	public Map<Integer, Integer> loadEffValMap() {
		Map<Integer, Integer> map = new HashMap<>();
		ManhattanBaseStageCfg stageCfg = this.getStageCfg();
		if (stageCfg != null) {
			map.putAll(stageCfg.getAttrMap());
		}
		for(Entry<Integer, Integer> entry : getPosLevelMap().entrySet()) {
			ManhattanBaseLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			if (cfg == null) {
				continue;
			}
			for(Entry<Integer, Integer> attrEntry : cfg.getAttrMap().entrySet()) {
				map.merge(attrEntry.getKey(), attrEntry.getValue(), (v1, v2) -> v1 + v2);
			}
		}
		return map;
	}

	@Override
	public boolean isBase() {
		return true;
	}
	
	/**
	 * 构造超武builder
	 * @param builder
	 */
	@Override
	public void buildManhattanInfo(PBManhattanInfo.Builder builder, PresetMarchManhattan presetMarchManhattan) {
		builder.setBaseStage(getStage());
		List<ManhattanPosInfo> list = new ArrayList<>();
		Set<Integer> posSet = new HashSet<>();
		for (Entry<Integer, Integer> entry : this.getPosLevelMap().entrySet()) {
			ManhattanPosInfo.Builder posInfo = ManhattanPosInfo.newBuilder();
			ManhattanBaseLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			posInfo.setCfgId(cfg == null ? 0 : cfg.getId());
			posInfo.setPosId(entry.getKey());
			posInfo.setLevel(entry.getValue());
			list.add(posInfo.build());
			posSet.add(entry.getKey());
		}

		for (int posId : ManhattanBaseLevelCfg.getPosSet()) {
			if (posSet.contains(posId)) {
				continue;
			}
			ManhattanPosInfo.Builder posInfo = ManhattanPosInfo.newBuilder();
			ManhattanBaseLevelCfg cfg = this.getPosLevelCfg(posId, 0);
			posInfo.setCfgId(cfg == null ? 0 : cfg.getId());
			posInfo.setPosId(posId);
			posInfo.setLevel(0);
			list.add(posInfo.build());
		}
		builder.addAllBasePosLevels(list);
	}
}
