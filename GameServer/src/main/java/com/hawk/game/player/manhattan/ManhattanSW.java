package com.hawk.game.player.manhattan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hawk.game.protocol.World.PresetMarchManhattan;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import com.hawk.game.config.ManhattanSWCfg;
import com.hawk.game.config.ManhattanSWLevelCfg;
import com.hawk.game.config.ManhattanSWSkillCfg;
import com.hawk.game.config.ManhattanSWStageCfg;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.protocol.Manhattan.ManhattanPosInfo;
import com.hawk.game.protocol.Manhattan.ManhattanSWInfo;
import com.hawk.game.protocol.Manhattan.PBManhattanInfo;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 超级武器
 * @author lating
 */
public class ManhattanSW extends PlayerManhattan {
	
	public static ManhattanSW create(ManhattanEntity entity) {
		ManhattanSW manhattan = new ManhattanSW(); 
		manhattan.init(entity);
		//超武  - 初始未解锁状态为0阶0级； 解锁后为1阶0级
		if (entity.getStage() <= 0) {
			entity.setStage(1);
		}
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
		ManhattanSWLevelCfg config = this.getPosLevelCfg(posId, newLevel);
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
		ManhattanSWSkillCfg oldSkillCfg = getUnlockedSkillCfg();
		int newStage = this.getStage() + 1;
		ManhattanSWStageCfg config = ManhattanSWStageCfg.getConfig(getSWCfgId(), newStage);
		if (config == null) {
			throw new RuntimeException("wrong invoke");
		}
		updateStage(newStage);
		ManhattanSWSkillCfg skillCfg = getUnlockedSkillCfg();
		//判断是否要同步世界点信息
		if ((oldSkillCfg == null && skillCfg != null) || (oldSkillCfg != null && oldSkillCfg.getId() != skillCfg.getId())) {
			WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(getEntity().getPlayerId());
			if (point != null) {
				syncWorldPoint(point);
			}
		}
	}
	
	public int getType() {
		ManhattanSWCfg swCfg = HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, getSWCfgId());
		return swCfg.getType();
	}
	
	public ManhattanSWStageCfg getStageCfg() {
		return ManhattanSWStageCfg.getConfig(getSWCfgId(), getStage());
	}
	
	public ManhattanSWLevelCfg getPosLevelCfg(int posId, int level) {
		return ManhattanSWLevelCfg.getConfig(getSWCfgId(), posId, level);
	}
	
	public ManhattanSWCfg getSwCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(ManhattanSWCfg.class, getSWCfgId());
	}
	
	public int getPower() {
		int power = 0;
		ManhattanSWStageCfg stageCfg = this.getStageCfg();
		if (stageCfg != null) {
			power += stageCfg.getPower();
		}
		for(Entry<Integer, Integer> entry : getPosLevelMap().entrySet()) {
			ManhattanSWLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			if (cfg != null) {
				power += cfg.getPower();
			}
		}
		
		return power;
	}
	
	/**
	 * 通知变化
	 */
	public void refreshPower() {
		getParent().refreshPowerElectric(PowerChangeReason.MANHATTAN_SW);
	}
	
	public Map<Integer, Integer> loadEffValMap() {
		//超武技能：要部署了才生效
		return loadEffValMap(this.isDeployed());
	}
	
	public Map<Integer, Integer> loadEffValMap(boolean loadSkill) {
		Map<Integer, Integer> map = new HashMap<>();
		//超武品阶
		ManhattanSWStageCfg stageCfg = this.getStageCfg();
		if (stageCfg != null) {
			map.putAll(stageCfg.getAttrMap());
		}
		//超武部件
		for(Entry<Integer, Integer> entry : getPosLevelMap().entrySet()) {
			ManhattanSWLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			if (cfg == null) {
				continue;
			}
			for(Entry<Integer, Integer> attrEntry : cfg.getAttrMap().entrySet()) {
				map.merge(attrEntry.getKey(), attrEntry.getValue(), (v1, v2) -> v1 + v2);
			}
		}
		
		if (loadSkill) {
			ManhattanSWSkillCfg skillCfg = getUnlockedSkillCfg();
			if (skillCfg != null) {
				for(Entry<Integer, Integer> attrEntry : skillCfg.getAttrMap().entrySet()) {
					map.merge(attrEntry.getKey(), attrEntry.getValue(), (v1, v2) -> v1 + v2);
				}
			}
		}
		
		return map;
	}
	
	@Override
	public boolean isBase() {
		return false;
	}
	
	/**
	 * 同步世界点信息
	 * @param point
	 */
	private void syncWorldPoint(WorldPoint point) {
		ManhattanSWCfg swCfg = getSwCfg();
		ManhattanSWSkillCfg skillCfg = getUnlockedSkillCfg();
		if (swCfg.getType() == 1) {
			point.setAtkManhattanSw(swCfg.getSwId());
			point.setAtkSwSkillId(skillCfg == null ? 0 : skillCfg.getId());
		} else {
			point.setDefManhattanSw(swCfg.getSwId());
			point.setDefSwSkillId(skillCfg == null ? 0 : skillCfg.getId());
		}
		WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
		point.notifyUpdate();
	}
	
	/**
	 * 超武部署
	 * @return
	 */
	public boolean deploy() {
		getEntity().setDeployed(1);
		//更新世界点信息
		WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(getEntity().getPlayerId());
		if (point != null) {
			syncWorldPoint(point);
		} else {
			HawkLog.errPrintln("manhattan deploy update worldPoint failed, playerId: {}, swId: {}, stage: {}", getEntity().getPlayerId(), getSWCfgId(), getStage());
		}
		return true;
	}
	
	public boolean cancelDeploy(boolean sync) {
		getEntity().setDeployed(0);
		this.cancelShow();
		WorldPoint point = WorldPlayerService.getInstance().getPlayerWorldPoint(getEntity().getPlayerId());
		if (point != null) {
			ManhattanSWCfg swCfg = getSwCfg();
			if (swCfg.getType() == 1) {
				point.setAtkManhattanSw(0);
				point.setAtkSwSkillId(0);
			} else {
				point.setDefManhattanSw(0);
				point.setDefSwSkillId(0);
			}
			if (sync) {
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
			}
			point.notifyUpdate();
		} else {
			HawkLog.errPrintln("manhattan cancel deploy update worldPoint failed, playerId: {}, swId: {}, stage: {}", getEntity().getPlayerId(), getSWCfgId(), getStage());
		}
		return true;
	}
	
	public boolean show() {
		getEntity().setCityShow(1);
		return true;
	}
	
	public boolean cancelShow() {
		getEntity().setCityShow(0);
		return true;
	}
	
	/**
	 * 构造超武builder
	 * @param builder
	 */
	public void buildManhattanInfo(PBManhattanInfo.Builder builder, PresetMarchManhattan presetMarchManhattan) {
		ManhattanSWInfo.Builder swBuilder = ManhattanSWInfo.newBuilder();
		swBuilder.setManhattanId(getEntity().getId());
		swBuilder.setCfgId(getSWCfgId());
		swBuilder.setStage(getStage());
		int depolyed = getEntity().getDeployed();
		if (presetMarchManhattan != null) {
			boolean hasPresetSW = presetMarchManhattan.getManhattanAtkSwId() > 0 || presetMarchManhattan.getManhattanDefSwId() > 0;
			boolean isCurrentSWPreset = presetMarchManhattan.getManhattanAtkSwId() == getSWCfgId()
					|| presetMarchManhattan.getManhattanDefSwId() == getSWCfgId();
			if (hasPresetSW) {
				depolyed = 0; // Reset to 0 if any preset SW exists
			}
			if (isCurrentSWPreset) {
				depolyed = 1; // Set to 1 if current SW matches preset
			}
		}
		swBuilder.setDeployed(depolyed);
		swBuilder.setShowTime(getSwCfg().getShowTimeValue());
		swBuilder.setCityShow(getEntity().getCityShow());
		
		List<ManhattanPosInfo> list = new ArrayList<>();
		Set<Integer> posSet = new HashSet<>();
		for (Entry<Integer, Integer> entry : getPosLevelMap().entrySet()) {
			ManhattanPosInfo.Builder posInfo = ManhattanPosInfo.newBuilder();
			ManhattanSWLevelCfg cfg = this.getPosLevelCfg(entry.getKey(), entry.getValue());
			posInfo.setCfgId(cfg == null ? 0 : cfg.getId());
			posInfo.setPosId(entry.getKey());
			posInfo.setLevel(entry.getValue());
			list.add(posInfo.build());
			posSet.add(entry.getKey());
		}
		
		for (int posId : ManhattanSWLevelCfg.getSwPos(getSWCfgId())) {
			if (posSet.contains(posId)) {
				continue;
			}
			ManhattanPosInfo.Builder posInfo = ManhattanPosInfo.newBuilder();
			ManhattanSWLevelCfg cfg = this.getPosLevelCfg(posId, 0);
			posInfo.setCfgId(cfg == null ? 0 : cfg.getId());
			posInfo.setPosId(posId);
			posInfo.setLevel(0);
			list.add(posInfo.build());
		}
		
		swBuilder.addAllPosLevels(list);
		builder.addSwInfos(swBuilder);
	}
	
	public ManhattanSWSkillCfg getUnlockedSkillCfg() {
		return ManhattanSWSkillCfg.getConfig(getSWCfgId(), getStage());
	}
	
}
