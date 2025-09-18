package com.hawk.game.module.autologic.data;

import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.hawk.game.entity.PlayerOtherEntity;
import com.hawk.game.module.autologic.cfg.AutoMassJoinCfg;
import com.hawk.serialize.string.SerializeHelper;

public class PlayerAutoMarchParam {
	
	//数据实体类
	private PlayerOtherEntity entity;
	
	//可使用士兵数量-比例
	private int marchSoldierPer;
	
	//可使用队列数量
	private int marchCount;
	
	//自动加入集结类型
	private Set<Integer> joinSet = new ConcurrentHashSet<Integer>();
	
	//开启时间
	private long openTime;
	
	
	public void notifyUpdate(){
		this.entity.notifyUpdate();
	}
	
	
	
	public int getMarchSoldierPer() {
		return marchSoldierPer;
	}
	
	public void setMarchSoldierPer(int marchSoldierPer) {
		this.marchSoldierPer = marchSoldierPer;
	}
	
	public void setMarchCount(int marchCount) {
		this.marchCount = marchCount;
	}
	
	public int getMarchCount() {
		return marchCount;
	}
	
	public Set<Integer> getJoinSet() {
		return joinSet;
	}
	
	
	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}
	
	
	public long getOpenTime() {
		return openTime;
	}
	
	public long getEndTime(){
		if(this.openTime == 0){
			return 0;
		}
		AutoMassJoinCfg cfg = HawkConfigManager.getInstance().getKVInstance(AutoMassJoinCfg.class);
		return this.openTime + cfg.getWorkTime() * 1000;
	}
	
	public void setEntity(PlayerOtherEntity entity) {
		this.entity = entity;
	}
	
	public boolean inWorking(){
		long curTime = HawkTime.getMillisecond();
		if(curTime > this.openTime + HawkTime.HOUR_MILLI_SECONDS * 12){
			return false;
		}
		return true;
	}
	
	
	
	
	
	
	public String serialize() {
		JSONObject obj = new JSONObject();
		obj.put("marchSoldierPer", this.marchSoldierPer);
		obj.put("marchCount", this.marchCount);
		obj.put("openTime", this.openTime);
		if(!this.joinSet.isEmpty()){
			String joinSetStr = Joiner.on(SerializeHelper.BETWEEN_ITEMS).join(this.joinSet);
			obj.put("joinSet", joinSetStr);
		}
		return obj.toJSONString();
	}
	
	
	public void unSerialize(String buildParam) {
		if(HawkOSOperator.isEmptyString(buildParam)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(buildParam);
		this.marchSoldierPer = obj.getIntValue("marchSoldierPer");
		this.marchCount = obj.getIntValue("marchCount");
		this.openTime = obj.getLongValue("openTime");
		
		Set<Integer> joinSetTemp = new ConcurrentHashSet<Integer>();
		if(obj.containsKey("joinSet")){
			String joinSetStr = obj.getString("joinSet");
			String[] darr = joinSetStr.split(SerializeHelper.BETWEEN_ITEMS);
			for(String dstr : darr){
				joinSetTemp.add(Integer.parseInt(dstr));
			}
			
		}
		this.joinSet = joinSetTemp;
	
	}

	
}
