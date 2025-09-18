package com.hawk.game.module.plantsoldier.science;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TechnologyLevelUpEvent;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.plantsoldier.science.cfg.PlantScienceCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantScienceSync;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.PowerChangeReason;

public class PlantScience {
	/** 数据实体*/
	private PlantScienceEntity scienceEntity;
	/** 科技列表*/
	private Map<Integer,PlantScienceComponent> components;
	/** 做用号 */
	private ImmutableMap<EffType, Integer> effValMap;
	/** 战斗力*/
	private int techPower;
	private boolean efvalLoad;

	private PlantScience(PlantScienceEntity entity) {
		this.scienceEntity = entity;
	}

	public static PlantScience create(PlantScienceEntity entity) {
		PlantScience plantScience = new PlantScience(entity);
		plantScience.init();
		entity.recordScienceObj(plantScience);
		return plantScience;
	}

	private void init() {
		this.components = this.loadComponents();
	}
	
	
	private Map<Integer,PlantScienceComponent> loadComponents(){
		Map<Integer,PlantScienceComponent> map = new ConcurrentHashMap<Integer, PlantScienceComponent>();
		String componentSerialized = this.scienceEntity.getPlantScienceSerialized();
		if(HawkOSOperator.isEmptyString(componentSerialized)){
			return map;
		}
		JSONArray arr = JSONArray.parseArray(componentSerialized);
		arr.forEach(obj->{
			PlantScienceComponent component = new PlantScienceComponent();
			component.mergeFrom(obj.toString());
			map.put(component.getScienceId(), component);
		});
		return map;
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
		for (PlantScienceComponent component : this.components.values()) {
			if(component.getLevel() <= 0){
				continue;
			}
			for (EffectObject eobj : component.getPlantScienceCfg().getEffectList()) {
				effmap.merge(eobj.getType(), eobj.getEffectValue(), (v1, v2) -> v1 + v2);
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
		//更新实体数据
		scienceEntity.notifyUpdate();
		//重新计算作用号
		efvalLoad = false;
		this.loadEffVal(); 
		if (!effValMap.isEmpty()) {
			getParent().getEffect().syncEffect(getParent(), effValMap.keySet().toArray(new EffType[0]));
		}
		this.plantScienceSync();
		if (techPower > 0) {
			getParent().refreshPowerElectric(PowerChangeReason.PLANT_SCIENCE_LEVEL_UP);
		}
	}

	/**
	 * 科技升级
	 * @param techId
	 * @return
	 */
	public boolean techLevelUp(int techId) {
		PlantScienceComponent entity = this.getComponentScienceId(techId);
		if (entity == null || entity.getState()<= 0) {
			return false;
		}
		int beforeLvl = entity.getLevel();
		int afterLvl = beforeLvl + 1;
		entity.setLevel(afterLvl);
		entity.setState(PlantScienceState.FREE.getNum());
		this.notifyChange();
		//抛事件
		PlantScienceCfg befCfg = HawkConfigManager.getInstance().getCombineConfig(PlantScienceCfg.class, techId,beforeLvl);
		int befPower = befCfg == null ? 0 : befCfg.getBattlePoint();
		PlantScienceCfg afterCfg = HawkConfigManager.getInstance().getCombineConfig(PlantScienceCfg.class, techId,afterLvl);
		int aftPower = afterCfg == null ? 0 : afterCfg.getBattlePoint();
		int addPower = Math.max(0, aftPower - befPower);
		ActivityManager.getInstance().postEvent(new TechnologyLevelUpEvent(this.getPlayerId(), 2,techId, addPower));
		//Tlog
		LogUtil.logPlantScienceResearchOperation(this.getParent(),techId, afterLvl,2,getTechPower());
		return true;
	}
	
	private int power() {
		int result = 0;
		for (PlantScienceComponent component : this.components.values()) {
			if(component.getLevel() <= 0){
				continue;
			}
			result += component.getPlantScienceCfg().getBattlePoint();
		}
		return result;
	}
	
	
	public PlantScienceComponent getComponentScienceId(int id){
		return this.components.get(id);
	}
	
	public ImmutableMap<Integer,PlantScienceComponent> getComponents() {
		return ImmutableMap.copyOf(components);
	}
	
	public PlantScienceComponent createScienceComponent(int sid,int level){
		PlantScienceComponent component = new PlantScienceComponent();
		component.setScienceId(sid);
		component.setLevel(level);
		component.setState(0);
		this.components.put(component.getScienceId(), component);
		this.notifyChange();
		return component;
	}

	public void plantScienceSync() {
		PBPlantScienceSync.Builder builder = PBPlantScienceSync.newBuilder();
		for(PlantScienceComponent component : this.components.values()){
			if(component.getLevel() <= 0){
				continue;
			}
			builder.addTechId(component.getPlantScienceCfg().getId());
		}
		getParent().sendProtocol(HawkProtocol.valueOf(HP.code2.PLAYER_PLANT_SCIENCE_SYNC_VALUE, builder));
	}



	/** 序列化skill */
	public String serializScienceComponent() {
		JSONArray arr = new JSONArray();
		for(PlantScienceComponent component : this.components.values()){
			arr.add(component.serializ());
		}
		return arr.toJSONString();
	}

	public String getPlayerId(){
		return this.scienceEntity.getPlayerId();
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(getPlayerId());
	}

	public ImmutableMap<EffType, Integer> getEffValMap() {
		return effValMap;
	}

	public int getTechPower() {
		return techPower;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("playerId", getPlayerId())
				.add("chipSerialized", serializScienceComponent())
				.toString();
	}

}
