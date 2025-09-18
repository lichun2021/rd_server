package com.hawk.game.world.object;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.hawk.game.protocol.World.PBCakeShare;

public class CakeShareInfo {
	
	/**
	 * 蛋糕ID
	 */
	private int cakeId;
	
	private long startTime;
	
	private long endTime;
	
	
	/**
	 * 玩家领奖信息
	 */
	private Map<String,Long> awadMap = new ConcurrentHashMap<>();
	
	public void addAwardRecord(String playerId,long time){
		this.awadMap.put(playerId, time);
	}
	
	public boolean isAward(String playerId){
		return this.awadMap.containsKey(playerId);
	}
	
	
	public int getAwardRecordSize(){
		return this.awadMap.size();
	}
	
	public void addAwardRecord(Map<String,String> records){
		this.awadMap.clear();
		if(records == null){
			return;
		}
		if(records.size() <= 0){
			return;
		}
		for(Entry<String, String> entry : records.entrySet()){
			String playerId = entry.getKey();
			long time = Long.parseLong(entry.getValue());
			this.awadMap.put(playerId, time);
		}
	}
	/**
	 * 序列化
	 * @return
	 */
	public PBCakeShare.Builder toBuilder(){
		PBCakeShare.Builder builder = PBCakeShare.newBuilder();
		builder.setCakeId(this.cakeId);
		if(this.awadMap.size() > 0){
			builder.addAllAwardRecords(new ArrayList<String>(this.awadMap.keySet()));
		}
		return builder;
	}

	public int getCakeId() {
		return cakeId;
	}

	public void setCakeId(int cakeId) {
		this.cakeId = cakeId;
	}


	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public Map<String, Long> getAwadMap() {
		return awadMap;
	}

	public void setAwadMap(Map<String, Long> awadMap) {
		this.awadMap = awadMap;
	}
	
	
}
