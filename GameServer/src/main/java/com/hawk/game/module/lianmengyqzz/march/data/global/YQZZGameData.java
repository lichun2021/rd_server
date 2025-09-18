package com.hawk.game.module.lianmengyqzz.march.data.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;

/**
 *  月球之战房间信息
 * @author che
 */
public class YQZZGameData implements IYQZZData{
	
	private static final String redisKey = "YQZZ_ACTIVITY_GAME_DATA";

	private int termId;
	
	private String roomId;
	
	private String roomServerId;
	
	private List<String> servers;

	private long lastActiveTime;
	
	private long finishTime;
	
	
	public YQZZGameData() {
		this.servers = new ArrayList<>();
	}
	
	
	public void addServer(String serverId){
		if(this.servers.contains(serverId)){
			return;
		}
		this.servers.add(serverId);
	}
	
	public void addServer(List<String> serverIds){
		for(String serverId: serverIds){
			this.addServer(serverId);
		}
	}
	
	public boolean isActive(long periodTime) {
		long curTime = HawkTime.getMillisecond();
		if (curTime - lastActiveTime < 10 * 1000 + periodTime) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	public int getTermId() {
		return termId;
	}
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public String getRoomServerId() {
		return roomServerId;
	}
	public void setRoomServerId(String roomServerId) {
		this.roomServerId = roomServerId;
	}
	
	public List<String> getServers() {
		return servers;
	}
	
	public int getServerCount(){
		return this.servers.size();
	}
	
	public long getLastActiveTime() {
		return lastActiveTime;
	}
	
	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}
	
	
	public long getFinishTime() {
		return finishTime;
	}
	
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("roomId", this.roomId);
		obj.put("roomServerId", this.roomServerId);
		JSONArray arr = new JSONArray();
		if(this.servers!= null && !this.servers.isEmpty()){
			for(String str : this.servers){
				arr.add(str);
			}
		}
		obj.put("servers", arr.toJSONString());
		obj.put("lastActiveTime", this.lastActiveTime);
		obj.put("finishTime", this.finishTime);
		return obj.toJSONString();
	}
	

	@Override
	public void mergeFrom(String serialiedStr) {
		this.termId = 0;
		this.roomId =null;
		this.roomServerId = null;
		this.servers = new ArrayList<>();
		this.lastActiveTime = 0;
		this.finishTime = 0;
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.roomId = obj.getString("roomId");
		this.roomServerId = obj.getString("roomServerId");
		String serverStr = obj.getString("servers");
		if(!HawkOSOperator.isEmptyString(serverStr)){
			JSONArray arr = JSONArray.parseArray(serverStr);
			for(int i=0;i< arr.size();i++){
				this.servers.add(arr.getString(i));
			}
		}
		this.lastActiveTime = obj.getLongValue("lastActiveTime");
		this.finishTime = obj.getLongValue("finishTime");
	}

	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + termId;
		RedisProxy.getInstance().getRedisSession()
			.hSet(key, this.roomId,this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);
		StatisManager.getInstance().incRedisKey(redisKey);
	}

	public static YQZZGameData  loadData(int termId,String roomId) {
		String key = redisKey  + ":" + termId;
		String dataStr = RedisProxy.getInstance().getRedisSession()
				.hGet(key, roomId,YQZZConst.REDIS_DATA_EXPIRE_TIME);
		StatisManager.getInstance().incRedisKey(redisKey);
		if(HawkOSOperator.isEmptyString(dataStr)){
			return null;
		}
		YQZZGameData data = new YQZZGameData();
		data.mergeFrom(dataStr);
		return data;
	}
	
	public static Map<String,YQZZGameData>  loadAllData(int termId) {
		String key = redisKey  + ":" + termId;
		Map<String,String> map = RedisProxy.getInstance().getRedisSession()
				.hGetAll(key,YQZZConst.REDIS_DATA_EXPIRE_TIME);
		StatisManager.getInstance().incRedisKey(redisKey);
		Map<String,YQZZGameData> rlt = new HashMap<>();
		if(map != null){
			for(Entry<String, String> entry : map.entrySet()){
				String valStr = entry.getValue();
				YQZZGameData data = new YQZZGameData();
				data.mergeFrom(valStr);
				rlt.put(data.getRoomId(), data);
			}
		}
		return rlt;
	}
	
	public static void saveAll(int termId, Map<String,YQZZGameData> map){
		String key = redisKey  + ":" + termId;
		Map<String,String> dataStrMap = new HashMap<>();
		for(YQZZGameData room :map.values()){
			dataStrMap.put(room.getRoomId(), room.serializ());
		}
		RedisProxy.getInstance().getRedisSession()
			.hmSet(key,dataStrMap,YQZZConst.REDIS_DATA_EXPIRE_TIME);
		
	}

}
