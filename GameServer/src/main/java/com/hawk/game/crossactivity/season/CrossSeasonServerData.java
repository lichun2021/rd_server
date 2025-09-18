package com.hawk.game.crossactivity.season;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;

public class CrossSeasonServerData {

	private int season;
	
	private String serverId;
	
	private int score;
	
	private int awardRank;
	
	private long initTime;
	
	
	private int inherited;
	private long mergeTime;
	private String inheritServer;
	private int inheritScore;
	
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public int getSeason() {
		return season;
	}
	
	
	public void setSeason(int season) {
		this.season = season;
	}
	
	
	public int getAwardRank() {
		return awardRank;
	}
	
	public void setAwardRank(int awardRank) {
		this.awardRank = awardRank;
	}
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	
	public int getInheritScore() {
		return inheritScore;
	}
	public String getInheritServer() {
		return inheritServer;
	}
	public long getMergeTime() {
		return mergeTime;
	}
	public void setMergeTime(long mergeTime) {
		this.mergeTime = mergeTime;
	}
	public void setInheritScore(int inheritScore) {
		this.inheritScore = inheritScore;
	}
	public void setInheritServer(String inheritServer) {
		this.inheritServer = inheritServer;
	}
	public int getInherited() {
		return inherited;
	}
	public void setInherited(int inherited) {
		this.inherited = inherited;
	}
	
	public String serialize(){
		JSONObject obj = new JSONObject();
        obj.put("season", this.season);
        obj.put("score", this.score);
        obj.put("serverId", this.serverId);
        obj.put("awardRank", this.awardRank);
        obj.put("initTime", this.initTime);
        obj.put("mergeTime", this.mergeTime);
        obj.put("inheritServer", this.inheritServer);
        obj.put("inheritScore", this.inheritScore);
        obj.put("inherited", this.inherited);
        return obj.toJSONString();
	}
	
	public void unSerialize(String data){
		if(HawkOSOperator.isEmptyString(data)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(data);
		this.season = obj.getIntValue("season");
		this.score = obj.getIntValue("score");
		this.serverId = obj.getString("serverId");
		this.awardRank = obj.getIntValue("awardRank");
		this.initTime = obj.getLongValue("initTime");
		this.mergeTime = obj.getLongValue("mergeTime");
		this.inheritServer = obj.getString("inheritServer");
		this.inheritScore = obj.getIntValue("inheritScore");
		this.inherited = obj.getIntValue("inherited");
	}

	
	
	
	public void saveData(){
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCORE , this.season);
        RedisProxy.getInstance().getRedisSession().hSet(key, this.serverId, this.serialize());
	}
	
	
	
	
	public static CrossSeasonServerData loadData(int season,String serverId){
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCORE , season);
		String value = RedisProxy.getInstance().getRedisSession().hGet(key, serverId);
        if(HawkOSOperator.isEmptyString(value)){
            return null;
        }
        CrossSeasonServerData data = new CrossSeasonServerData();
        data.unSerialize(value);
        return data;
	}
	
	
	public static Map<String,CrossSeasonServerData> loadAllData(int season){
		Map<String,CrossSeasonServerData> rlt = new HashMap<>();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCORE , season);
		Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
        if(Objects.isNull(map) || map.isEmpty()){
            return rlt;
        }
        for(String str : map.values()){
        	CrossSeasonServerData data = new CrossSeasonServerData();
        	data.unSerialize(str);
            rlt.put(data.serverId, data);
        }
        return rlt;
	}
	
	
	public static void saveAllData(List<CrossSeasonServerData> datas,int season){
		if(Objects.isNull(datas)){
			return;
		}
		if(datas.isEmpty()){
			return;
		}
		Map<String,String> map = new HashMap<>();
        for(CrossSeasonServerData data : datas){
        	map.put(data.getServerId(), data.serialize());
        }
        String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCORE , season);
		RedisProxy.getInstance().getRedisSession().hmSet(key, map, 0);
	}
	
}
