package com.hawk.activity.type.impl.backImmigration;

import com.alibaba.fastjson.JSONObject;

public class BackImmgrationServer {

	private String serverId;
	
	private long powerMin;
	
	private long powerMax;
	
	private long updateTime;
	
	private long openTime;
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("serverId", this.serverId);
		obj.put("powerMin", this.powerMin);
		obj.put("powerMax", this.powerMax);
		obj.put("updateTime", this.updateTime);
		obj.put("openTime", this.openTime);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.serverId = obj.getString("serverId");
		this.powerMin = obj.getIntValue("powerMin");
		this.powerMax = obj.getIntValue("powerMax");
		this.updateTime = obj.getLongValue("updateTime");
		this.openTime = obj.getLongValue("openTime");
	}
	
	

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getPowerMin() {
		return powerMin;
	}

	public void setPowerMin(long powerMin) {
		this.powerMin = powerMin;
	}

	public long getPowerMax() {
		return powerMax;
	}

	public void setPowerMax(long powerMax) {
		this.powerMax = powerMax;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public long getOpenTime() {
		return openTime;
	}
	
	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}
	
	
	
}
