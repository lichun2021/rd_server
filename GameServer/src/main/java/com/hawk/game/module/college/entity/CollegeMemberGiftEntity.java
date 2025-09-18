package com.hawk.game.module.college.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

/**
 * 直购商店礼包数据
 */
public class CollegeMemberGiftEntity{
	

	private long refreshTime;
	
	private Map<Integer,Integer> giftMap = new ConcurrentHashMap<>();
	
	
	
	public boolean insell(int gId){
		return this.giftMap.containsKey(gId);
	}
	
	
	public int getBuyCount(int gId){
		return this.giftMap.getOrDefault(gId, 0);
	}
	
	
	public void addGiftBuy(int gId,int count){
		int buyCount = this.giftMap.getOrDefault(gId, 0);
		buyCount += count;
		this.giftMap.put(gId, buyCount);
	}
	
	
	public Map<Integer, Integer> getGiftMap() {
		return giftMap;
	}
	
	
	public long getRefreshTime() {
		return refreshTime;
	}
	
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("refreshTime", this.refreshTime);
		if(this.giftMap.size() > 0){
			JSONObject giftJson = new JSONObject();
			for(Map.Entry<Integer, Integer> entry : this.giftMap.entrySet()){
				int key = entry.getKey();
				int value = entry.getValue();
				giftJson.put(String.valueOf(key), value);
			}
			obj.put("giftMap", giftJson.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.refreshTime = 0;
			this.giftMap =  new ConcurrentHashMap<>();
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.refreshTime = obj.getLongValue("refreshTime");
		
		Map<Integer,Integer> giftMapTemp =  new ConcurrentHashMap<>();
		if(obj.containsKey("giftMap")){
			String giftStr = obj.getString("giftMap");
			JSONObject gjson = JSONObject.parseObject(giftStr);
			for(String key : gjson.keySet()){
				int value = gjson.getIntValue(key);
				giftMapTemp.put(Integer.parseInt(key), value);
			}
		}
		this.giftMap = giftMapTemp;
		
	}
	
	
}
