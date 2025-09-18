package com.hawk.game.module.lianmengfgyl.march.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;

public class FGYLGuildJoinData {
	
	private int termId;
	
	private String serverId;
	
	private String guildId;
		
	private int signCount; //报名次数
	
	private long signTime;  //报名的时间
	 
	private int signBattleIndex; //报名的时间点
	
	private int fightLevel; //战斗难度

	private long fightCreateTime; //副本开整时间
	
	private int createFightRlt; //创建副本结果
	
	private FGYLRoomData gameRoom; //当前正在进行的副本
	
	private List<FGYLRoomData> records = new ArrayList<>();  //历史记录
	
	private int winPassLevel;
	private int winPassTime;
	
	
	public boolean calAndSetFightCreateTime(){
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		HawkTuple2<Integer, Integer> tuple = constCfg.getWarTimePoint(this.signBattleIndex-1);
		if(Objects.isNull(tuple)){
			return false;
		}
		long zero = HawkTime.getAM0Date().getTime();
		long time = zero + tuple.first * HawkTime.HOUR_MILLI_SECONDS + tuple.second * HawkTime.MINUTE_MILLI_SECONDS;
		this.fightCreateTime = time;
		return true;
	}
		
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getGuildId() {
		return guildId;
	}
	
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	
	public int getSignCount() {
		return signCount;
	}
	
	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}
	
	
	public long getSignTime() {
		return signTime;
	}
	
	public void setSignTime(long signTime) {
		this.signTime = signTime;
	}
	
	public int getSignBattleIndex() {
		return signBattleIndex;
	}
	
	public void setSignBattleIndex(int signBattleIndex) {
		this.signBattleIndex = signBattleIndex;
	}
	
	
	public void setFightCreateTime(long fightCreateTime) {
		this.fightCreateTime = fightCreateTime;
	}
	

	public long getFightCreateTime() {
		return fightCreateTime;
	}
	
	public int getCreateFightRlt() {
		return createFightRlt;
	}
	
	public void setCreateFightRlt(int createFightRlt) {
		this.createFightRlt = createFightRlt;
	}
	
	public FGYLRoomData getGameRoom() {
		return gameRoom;
	}
	
	
	public void setGameRoom(FGYLRoomData gameRoom) {
		this.gameRoom = gameRoom;
	}
	
	public void clearGameRoom(){
		this.gameRoom = null;
	}
	
	
	public List<FGYLRoomData> getRecords() {
		return records;
	}

	public int getWinPassLevel() {
		return winPassLevel;
	}

	public void setWinPassLevel(int winPassLevel) {
		this.winPassLevel = winPassLevel;
	}
	
	public int getWinPassTime() {
		return winPassTime;
	}
	
	public void setWinPassTime(int winPassTime) {
		this.winPassTime = winPassTime;
	}
	
	public void setFightLevel(int fightLevel) {
		this.fightLevel = fightLevel;
	}
	
	public int getFightLevel() {
		return fightLevel;
	}
	
	
	public void recordFightRoom(){
		if(Objects.nonNull(this.gameRoom)){
			this.records.add(this.gameRoom);
		}
	}
	
	public void fightOver(boolean win,int level,int timeUse){
		if(!win){
			return;
		}
		this.winPassLevel = level;
		this.winPassTime = timeUse;
	}
	
	
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("serverId", this.serverId);
		obj.put("guildId", this.guildId);
		
		obj.put("signCount", this.signCount);
		obj.put("signTime", this.signTime);
		obj.put("signBattleIndex", this.signBattleIndex);
		
		
		obj.put("fightLevel", this.fightLevel);
		obj.put("fightCreateTime", this.fightCreateTime);
		obj.put("createFightRlt", this.createFightRlt);
		
		if(Objects.nonNull(this.gameRoom)){
			obj.put("gameRoom", this.gameRoom.serializ());
		}
		if(records.size() > 0){
			JSONArray arr = new JSONArray();
			for(FGYLRoomData data : this.records){
				arr.add(data.serializ());
			}
			obj.put("records", arr.toJSONString());
			
		}
		obj.put("winPassLevel", this.winPassLevel);
		obj.put("winPassTime", this.winPassTime);
		return obj.toJSONString();
	}
	
	
	public void mergeFrom(String serialiedStr){
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.serverId = obj.getString("serverId");
		this.guildId = obj.getString("guildId");
		
		this.signCount = obj.getIntValue("signCount");
		this.signTime = obj.getLongValue("signTime");
		this.signBattleIndex = obj.getIntValue("signBattleIndex");
		
		this.fightLevel = obj.getIntValue("fightLevel");
		this.fightCreateTime = obj.getLongValue("fightCreateTime");
		this.createFightRlt = obj.getIntValue("createFightRlt");
		
		if(obj.containsKey("gameRoom")){
			String str = obj.getString("gameRoom");
			FGYLRoomData data = new FGYLRoomData();
			data.mergeFrom(str);
			this.gameRoom = data;
		}
		
		List<FGYLRoomData> recordsTemp = new ArrayList<>();
		if(obj.containsKey("records")){
			String str = obj.getString("records");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				String param = jarr.getString(i);
				FGYLRoomData data = new FGYLRoomData();
				data.mergeFrom(param);
				recordsTemp.add(data);
			}
		}
		this.records = recordsTemp;
		this.winPassLevel = obj.getIntValue("winPassLevel");
		this.winPassTime = obj.getIntValue("winPassTime");
	}
	
	
	
	public void saveRedis(){
		String key = RedisProxy.FGYL_ACTIVITY_SIGN  + ":" + this.serverId+":"+ this.termId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().hSet(key, this.guildId, this.serializ());
	}

	
	
	public static Map<String,FGYLGuildJoinData> loadAll(String serverId,int termId){
		String key = RedisProxy.FGYL_ACTIVITY_SIGN  + ":" + serverId+":"+ termId;
		StatisManager.getInstance().incRedisKey(key);
		Map<String,String> rlt = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		Map<String,FGYLGuildJoinData> map = new ConcurrentHashMap<String, FGYLGuildJoinData>();
		for(Map.Entry<String, String> entry : rlt.entrySet()){
			String value = entry.getValue();
			FGYLGuildJoinData data = new FGYLGuildJoinData();
			data.mergeFrom(value);
			map.put(data.getGuildId(),data);
		}
		return map;
	}
}
