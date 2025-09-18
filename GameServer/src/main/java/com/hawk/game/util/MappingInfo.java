package com.hawk.game.util;

import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class MappingInfo implements Comparable<MappingInfo> {
	private String mappingType;
	private String mappingValue;
	private long mappingTime;
	private String playerId;

	public String getMappingType() {
		return mappingType;
	}

	public void setMappingType(String mappingType) {
		this.mappingType = mappingType;
	}

	public String getMappingValue() {
		return mappingValue;
	}

	public void setMappingValue(String mappingValue) {
		this.mappingValue = mappingValue;
	}

	public long getMappingTime() {
		return mappingTime;
	}

	public void setMappingTime(long mappingTime) {
		this.mappingTime = mappingTime;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public boolean fromJson(String value) {
		try {
			JSONObject json = JSON.parseObject(value);
			mappingType = json.getString("mappingType");
			mappingValue = json.getString("mappingValue");
			mappingTime = json.getLongValue("mappingTime");
			playerId = json.getString("playerId");
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		json.put("mappingType", mappingType);
		json.put("mappingValue", mappingValue);
		json.put("mappingTime", mappingTime);
		json.put("playerId", playerId);
		return json.toString();
	}

	@Override
	public int compareTo(MappingInfo info) {
		if (mappingTime == info.getMappingTime()) {
			return 0;
		}
		return mappingTime > info.getMappingTime() ? -1 : 1;
	}
}
