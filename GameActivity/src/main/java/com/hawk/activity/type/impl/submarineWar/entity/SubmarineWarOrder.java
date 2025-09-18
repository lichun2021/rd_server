package com.hawk.activity.type.impl.submarineWar.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Transient;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity;
import com.hawk.serialize.string.SerializeHelper;

public class SubmarineWarOrder {
	
	/**
	 * 经验总值
	 */
	private int expTotal;
	
	/**
	 * 当前等级经验
	 */
	private int exp;
	
	/**
	 * 当前等级
	 */
	private int level;
	
	/**
	 * 进阶
	 */
	private int advance;
	
	/**
	 * 购买经验次数
	 */
	private int buyExpCount;
	
	/** 普通领奖等级信息 **/
	@Transient
	private Map<Integer, Long> rewardNormalLevelMap = new ConcurrentHashMap<Integer, Long>();
	
	/** 进阶等奖等级信息 **/
	@Transient
	private Map<Integer, Long> rewardAdvanceLevelMap = new ConcurrentHashMap<Integer, Long>();

	
	
	public void addAchieveRewardNormal(int level,long time){
		this.rewardNormalLevelMap.put(level, time);
	}
	
	
	public void addAchieveRewardAdvance(int level,long time){
		this.rewardAdvanceLevelMap.put(level, time);
	}
	
	public boolean canRewardNormal(int level){
		if(this.level < level){
			return false;
		}
		if(this.rewardNormalLevelMap.containsKey(level)){
			return false;
		}
		return true;
	}
	
	public boolean canRewardAdvance(int level){
		if(this.level < level){
			return false;
		}
		if(this.advance <= 0){
			return false;
		}
		if(this.rewardAdvanceLevelMap.containsKey(level)){
			return false;
		}
		return true;
	}
	
	
	public int getExpTotal() {
		return expTotal;
	}
	
	public void setExpTotal(int expTotal) {
		this.expTotal = expTotal;
	}
	
	
	public int getAdvance() {
		return advance;
	}
	public void setAdvance(int advance) {
		this.advance = advance;
	}
	
	
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public Map<Integer, Long> getRewardAdvanceLevelMap() {
		return rewardAdvanceLevelMap;
	}
	
	public Map<Integer, Long> getRewardNormalLevelMap() {
		return rewardNormalLevelMap;
	}
	
	public int getBuyExpCount() {
		return buyExpCount;
	}
	
	public void setBuyExpCount(int buyExpCount) {
		this.buyExpCount = buyExpCount;
	}
	
	
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("expTotal", this.expTotal);
		obj.put("exp", this.exp);
		obj.put("level", this.level);
		obj.put("advance", this.advance);
		obj.put("buyExpCount", this.buyExpCount);
		if(this.rewardNormalLevelMap.size() > 0){
			String rewardNormal = SerializeHelper.mapToString(rewardNormalLevelMap);
			obj.put("rewardNormalLevelMap", rewardNormal);
		}
		if(this.rewardAdvanceLevelMap.size() > 0){
			String rewardAdvance = SerializeHelper.mapToString(rewardAdvanceLevelMap);
			obj.put("rewardAdvanceLevelMap", rewardAdvance);
		}
		return obj.toJSONString();
	}
	

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.expTotal = obj.getIntValue("expTotal");
		this.exp = obj.getIntValue("exp");
		this.level = obj.getIntValue("level");
		this.advance = obj.getIntValue("advance");
		this.buyExpCount = obj.getIntValue("buyExpCount");
		if(obj.containsKey("rewardNormalLevelMap")){
			String str = obj.getString("rewardNormalLevelMap");
			this.rewardNormalLevelMap = SerializeHelper.stringToMap(str,Integer.class,Long.class);
		}
		if(obj.containsKey("rewardAdvanceLevelMap")){
			String str = obj.getString("rewardAdvanceLevelMap");
			this.rewardAdvanceLevelMap = SerializeHelper.stringToMap(str,Integer.class,Long.class);
		}
		
	}
	
	
	public Activity.SubmarineWarOrder.Builder genBuilder(){
		Activity.SubmarineWarOrder.Builder builder = Activity.SubmarineWarOrder.newBuilder();
		builder.setLevel(this.level);
		builder.setExp(this.exp);
		builder.setAuthorityId(this.advance);
		builder.setBuyLevelCount(this.buyExpCount);
		List<Integer> nlist = new ArrayList<>(this.rewardNormalLevelMap.keySet());
		List<Integer> alist = new ArrayList<>(this.rewardAdvanceLevelMap.keySet());
		builder.addAllRewardNormalLevel(nlist);
		builder.addAllRewardAdvancedLevel(alist);
		return builder;
	}
	
	
}
