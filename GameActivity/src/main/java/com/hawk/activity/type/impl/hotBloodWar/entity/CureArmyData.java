package com.hawk.activity.type.impl.hotBloodWar.entity;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarKVCfg;

/**
 * @author LENOVO
 *
 */
public class CureArmyData implements Comparable<CureArmyData>{

	//士兵ID
	private int armyId;
	//未完成数量
	private int cureCount;
	//已经完成的数量
	private int healthCount;
	
	
	
	public CureArmyData() {
	}
	
	public int getArmyId() {
		return armyId;
	}
	
	public void setArmyId(int armyId) {
		this.armyId = armyId;
	}
	
	public int getCureCount() {
		return cureCount;
	}
	
	public void setCureCount(int cureCount) {
		this.cureCount = cureCount;
	}
	
	
	public int getHealthCount() {
		return healthCount;
	}
	
	public void setHealthCount(int healthCount) {
		this.healthCount = healthCount;
	}


	public String serializeData() {
		JSONObject obj = new JSONObject();
		obj.put("armyId", this.armyId);
		obj.put("cureCount", this.cureCount);
		obj.put("healthCount", this.healthCount);
		return obj.toJSONString();
	}

	public void unSerializeData(String data) {
		if(HawkOSOperator.isEmptyString(data)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(data);
		this.armyId = obj.getIntValue("armyId");
		this.cureCount = obj.getIntValue("cureCount");
		this.healthCount = obj.getIntValue("healthCount");
	}
	
	
	
	public int getSigleArmyCureTime(){
		HotBloodWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HotBloodWarKVCfg.class);
		HawkTuple3<Integer, Integer, Integer> armyConfig = this.getConfigData();
		
		int basicTime = armyConfig.third;
		int specialTime = ActivityManager.getInstance().getDataGeter().getSpecialSoldierTime(armyConfig.second);
		if (specialTime > 0) {
			basicTime = specialTime;
		}
		int armyCureTime = (int) (basicTime * 1000l  * cfg.getRecover()/ 10000l);
		return armyCureTime;
	}
	
	
	
	public HawkTuple3<Integer, Integer, Integer> getConfigData(){
		return ActivityManager.getInstance().getDataGeter().getSoldierConfigData(this.armyId);
	}

	
	@Override
	public int compareTo(CureArmyData target) {
		HawkTuple3<Integer, Integer, Integer> armyCfg = ActivityManager.getInstance().getDataGeter().getSoldierConfigData(this.armyId);
		HawkTuple3<Integer, Integer, Integer> targetCfg = ActivityManager.getInstance().getDataGeter().getSoldierConfigData(target.armyId);
		if (armyCfg.second != targetCfg.second) {
			return targetCfg.second - armyCfg.second;
		} else {
			return target.armyId - this.armyId;
		}
	}
	
	
}