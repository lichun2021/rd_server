package com.hawk.game.profiler;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 客户端性能分析数据
 * 
 * @author lating
 *
 */
public class ClientDataInfo {
	/**
	 * 设备型号
	 */
	private String deviceModel = "";
	/**
	 * 客户端版本号
	 */
	private String clientVersion = "";
	/**
	 * 客户端操作
	 */
	private String action = "";
	/**
	 * 设备等级
	 */
	private int deviceLevel;
	/**
	 * 平均值
	 */
	private double avgVal;
	/**
	 * 最大值
	 */
	private double maxVal;
	/**
	 * 最小值
	 */
	private double minVal;
	/**
	 * 统计次数
	 */
	private int count;
	
	@JSONField(serialize = false)
	private boolean isNew;
	
	@JSONField(serialize = false)
	private boolean isCreate;
	
	@JSONField(serialize = false)
	private long index;
	
	public ClientDataInfo() {
	}
	
	public ClientDataInfo(String deviceModel, int deviceLevel, String clientVersion, String action) {
		this.deviceModel = deviceModel;
		this.deviceLevel = deviceLevel;
		this.clientVersion = clientVersion;
		this.action = action;
		this.isNew = true;
	}
	
	public long getIndex() {
		return index;
	}
	
	public void setIndex(long index) {
		this.index = index;
	}
	
	public String getDeviceModel() {
		return deviceModel;
	}
	
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	
	public int getDeviceLevel() {
		return deviceLevel;
	}
	
	public void setDeviceLevel(int deviceLevel) {
		this.deviceLevel = deviceLevel;
	}
	
	public String getClientVersion() {
		return clientVersion;
	}
	
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public double getAvgVal() {
		return avgVal;
	}

	public void setAvgVal(double avgVal) {
		this.avgVal = avgVal;
	}

	public double getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}

	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public void setCreate(boolean isCreate) {
		this.isCreate = isCreate;
	}
	
	public boolean isCreate() {
		return isCreate;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public String getRedisKey() {
		return deviceModel + ":" + clientVersion + ":" + action;
	}
}
