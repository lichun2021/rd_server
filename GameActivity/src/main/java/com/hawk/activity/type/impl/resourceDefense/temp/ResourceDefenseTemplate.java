package com.hawk.activity.type.impl.resourceDefense.temp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.game.protocol.Activity.RDStationState;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 资源保卫战
 * @author golden
 *
 */
public class ResourceDefenseTemplate implements SplitEntity {

	/**
	 * 资源站类型
	 */
	private int stationType;
	
	/**
	 * 状态 1 废弃状态 2 开采
	 */
	private int state;
	
	/**
	 * 开始时间
	 */
	private long beginTime;
	
	/**
	 * 上次计算资源时间
	 */
	private long lastTickTime;
	
	/**
	 * 加速值
	 */
	private int speedValue;
	
	/**
	 * 偷取的玩家列表
	 */
	private String beSteal;
	
	/**
	 * 晶体信息
	 */
	private String resource;
	
	/**
	 * 产出资源次数
	 */
	private int tickTimes;
	
	/**
	 * 请求帮助
	 */
	private int requestHelp;
	
	/**
	 * 被帮助次数
	 */
	private String beHelpStr;
	
	/**
	 * 晶体信息
	 */
	private Map<Integer, Integer> resourceMap = new HashMap<>();
	
	/**
	 * 被偷取玩家列表
	 */
	private List<String> beStealList = new ArrayList<>();
	
	/**
	 * 被帮助玩家列表
	 */
	private List<String> beHelpList = new ArrayList<>();
	
	public ResourceDefenseTemplate() {
		
	}
	
	/**
	 * 构造
	 * @param stationType
	 */
	public ResourceDefenseTemplate(int stationType) {
		this.stationType = stationType;
		this.state = RDStationState.RDSTATION_NONE_VALUE;
		this.beginTime = 0L;
		this.lastTickTime = 0L;
		this.speedValue = 0;
		this.beSteal = "";
		this.resource = "";
		this.tickTimes = 0;
		this.requestHelp = 0;
		this.beHelpStr = "";
	}

	@Override
	public SplitEntity newInstance() {
		return new ResourceDefenseTemplate();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(stationType);
		dataList.add(state);
		dataList.add(beginTime);
		dataList.add(lastTickTime);
		dataList.add(speedValue);
		
		beSteal = genBeStealStr();
		dataList.add(beSteal);
		
		resource = genResourceStr();
		dataList.add(resource);
		
		dataList.add(tickTimes);
		dataList.add(requestHelp);
		
		beHelpStr = genBeHelpStr();
		dataList.add(beHelpStr);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(10);
		setStationType(dataArray.getInt());
		setState(dataArray.getInt());
		setBeginTime(dataArray.getLong());
		setLastTickTime(dataArray.getLong());
		setSpeedValue(dataArray.getInt());
		setBeSteal(dataArray.getString());
		setResource(dataArray.getString());
		setTickTimes(dataArray.getInt());
		setRequestHelp(dataArray.getInt());
		setBeHelpStr(dataArray.getString());
		
		beStealList = genStealList();
		resourceMap = genResourceMap();
		beHelpList = genBeHelpList();
	}
	
	public void reset() {
		state = RDStationState.RDSTATION_NONE_VALUE;
		beginTime = 0L;
		lastTickTime = 0L;
		speedValue = 0;
		beSteal = "";
		resource = "";
		tickTimes = 0;
		requestHelp = 0;
		beHelpStr = "";
		
		beStealList.clear();
		resourceMap.clear();
		beHelpList.clear();
	}
	
	public int getStationType() {
		return stationType;
	}

	public void setStationType(int stationType) {
		this.stationType = stationType;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getLastTickTime() {
		return lastTickTime;
	}

	public void setLastTickTime(long lastTickTime) {
		this.lastTickTime = lastTickTime;
	}

	public int getSpeedValue() {
		return speedValue;
	}

	public void setSpeedValue(int speedValue) {
		this.speedValue = speedValue;
	}

	public String getBeSteal() {
		return beSteal;
	}

	public void setBeSteal(String beSteal) {
		this.beSteal = beSteal;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public int getTickTimes() {
		return tickTimes;
	}

	public void setTickTimes(int tickTimes) {
		this.tickTimes = tickTimes;
	}

	public void addTickTimes(int addTickTimes) {
		this.tickTimes += addTickTimes;
	}
	
	public int getRequestHelp() {
		return requestHelp;
	}

	public void setRequestHelp(int requestHelp) {
		this.requestHelp = requestHelp;
	}

	public String getBeHelpStr() {
		return beHelpStr;
	}

	public void setBeHelpStr(String beHelpStr) {
		this.beHelpStr = beHelpStr;
	}

	public void setBeStealList(List<String> beStealList) {
		this.beStealList = beStealList;
	}

	public List<String> getBeStealList() {
		return beStealList;
	}

	public List<String> getBeHelpList() {
		return beHelpList;
	}

	public void addBeHelpPlayer(String playerId) {
		if (beHelpList.contains(playerId)) {
			return;
		}
		beHelpList.add(playerId);
	}

	/**
	 * 添加偷取的玩家
	 */
	public void addBeSteal(String playerId) {
		beStealList.add(playerId);
	}

	/**
	 * 获取资源
	 */
	public Map<Integer, Integer> getResourceMap() {
		return resourceMap;
	}

	/**
	 * 资源站内是否有资源
	 */
	public boolean hasResource() {
		int count = 0;
		for (Entry<Integer, Integer> res : resourceMap.entrySet()) {
			count += res.getValue();
		}
		return count > 0;
	}
	/**
	 * 清空资源
	 */
	public void clearResource() {
		resourceMap.clear();
	}
	
	/**
	 * 添加资源数量
	 */
	public void addResourceCount(int resourceType, int addCount) {
		Integer beforeCount = resourceMap.getOrDefault(resourceType, 0);
		int afterCount = Math.max(0, beforeCount + addCount);
		resourceMap.put(resourceType, afterCount);
	}
	
	/**
	 * 生成被帮助玩家字符串
	 */
	public String genBeHelpStr() {
		return SerializeHelper.collectionToString(beHelpList, SerializeHelper.BETWEEN_ITEMS);
	}

	/**
	 * 生成被帮助玩家列表
	 */
	public List<String> genBeHelpList() {
		return SerializeHelper.stringToList(String.class, beHelpStr, SerializeHelper.BETWEEN_ITEMS);
	}
	
	/**
	 * 生成偷取玩家字符串
	 */
	public String genBeStealStr() {
		return SerializeHelper.collectionToString(beStealList, SerializeHelper.BETWEEN_ITEMS);
	}
	
	/**
	 * 获取偷取玩家列表
	 */
	public List<String> genStealList() {
		return SerializeHelper.stringToList(String.class, beSteal, SerializeHelper.BETWEEN_ITEMS);
	}
	
	/**
	 * 生成资源字符串
	 */
	public String genResourceStr() {
		return SerializeHelper.mapToString(resourceMap, SerializeHelper.COLON_ITEMS, SerializeHelper.SEMICOLON_ITEMS);
	}
	
	/**
	 * 生成资源map
	 */
	public Map<Integer, Integer> genResourceMap() {
		return SerializeHelper.stringToMap(resource, Integer.class, Integer.class, SerializeHelper.COLON_ITEMS, SerializeHelper.SEMICOLON_ITEMS);
	}
	
}
