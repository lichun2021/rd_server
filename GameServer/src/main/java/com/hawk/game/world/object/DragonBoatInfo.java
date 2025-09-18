package com.hawk.game.world.object;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.hawk.game.protocol.World.PBDragonBoat;

public class DragonBoatInfo {
	
	/**
	 * 龙船ID
	 */
	private long boatId;
	
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
	public PBDragonBoat.Builder toBuilder(){
		PBDragonBoat.Builder builder = PBDragonBoat.newBuilder();
		builder.setBoatId(this.boatId);
		if(this.awadMap.size() > 0){
			builder.addAllAwardRecords(new ArrayList<String>(this.awadMap.keySet()));
		}
		return builder;
	}

	public long getBoatId() {
		return boatId;
	}

	public void setBoatId(long boatId) {
		this.boatId = boatId;
	}

	public Map<String, Long> getAwadMap() {
		return awadMap;
	}

	public void setAwadMap(Map<String, Long> awadMap) {
		this.awadMap = awadMap;
	}

	
	
}
