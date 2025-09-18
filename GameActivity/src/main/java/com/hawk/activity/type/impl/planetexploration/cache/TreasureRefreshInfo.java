package com.hawk.activity.type.impl.planetexploration.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.hawk.os.HawkOSOperator;
import com.alibaba.fastjson.JSONObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 矿点刷新信息
 */
public class TreasureRefreshInfo {
	/**
	 * 已经刷出的矿点数
	 */
	private int refreshCount;
	/**
	 * 最近一次的刷新时间
	 */
	private long nearRefreshTime;
	/**
	 * 上期积分
	 */
	private long lastScore;
	/**
	 * 本期活动历史上刷出的所有点
	 */
	private Map<Long, List<Integer>> historyPointMap = new ConcurrentHashMap<>();
	/**
	 * 每个点刷出的时间
	 */
	private Map<Integer, Long> pointMap = new ConcurrentHashMap<>();
	
	public int getRefreshCount() {
		return refreshCount;
	}
	
	public void setRefreshCount(int refreshCount) {
		this.refreshCount = refreshCount;
	}
	
	public long getNearRefreshTime() {
		return nearRefreshTime;
	}
	
	public void setNearRefreshTime(long nearRefreshTime) {
		this.nearRefreshTime = nearRefreshTime;
	}

	public long getLastScore() {
		return lastScore;
	}

	public void setLastScore(long lastScore) {
		this.lastScore = lastScore;
	}

	public Map<Integer, Long> getPointMap() {
		return pointMap;
	}

	public void addPoint(long time, List<Integer> pointList) {
		pointList.forEach(e -> pointMap.put(e, time));
		this.historyPointMap.put(time, pointList);
	}
	
	public void removePoint(Integer point) {
		this.pointMap.remove(point);
	}

	public Map<Long, List<Integer>> getHistoryPointMap() {
		return historyPointMap;
	}
	
	public List<Long> getSortedHisTime() {
		if (historyPointMap.isEmpty()) {
			return Collections.emptyList();
		}
		
		return historyPointMap.keySet().stream().sorted().collect(Collectors.toList());
	}
	
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("refreshCount", refreshCount);
		json.put("nearRefreshTime", nearRefreshTime);
		json.put("lastScore", lastScore);
		if (!pointMap.isEmpty()) {
			json.put("pointInfo", SerializeHelper.mapToString(pointMap));
		}

		if (!historyPointMap.isEmpty()) {
			StringBuilder historyPoints = new StringBuilder();
			for(Entry<Long, List<Integer>> entry : historyPointMap.entrySet()) {
				String pointsStr = SerializeHelper.collectionToString(entry.getValue(), ",");
				historyPoints.append(entry.getKey()).append("_").append(pointsStr).append(";");
			}
			
			json.put("historyPoint", historyPoints.deleteCharAt(historyPoints.length() -1).toString());
		}
		return json.toJSONString();
	}
	
	public static TreasureRefreshInfo str2Object(String info) {
		TreasureRefreshInfo object = new TreasureRefreshInfo();
		JSONObject json = JSONObject.parseObject(info);
		object.refreshCount = json.getIntValue("refreshCount");
		object.nearRefreshTime = json.getLongValue("nearRefreshTime");
		object.lastScore = json.getLongValue("lastScore");
		String pointInfo = json.getString("pointInfo");
		if (!HawkOSOperator.isEmptyString(pointInfo)) {
			Map<Integer, Long> map = new HashMap<>();
			SerializeHelper.stringToMap(pointInfo, Integer.class, Long.class, map);
			object.pointMap.putAll(map);
		}
		String historyPoint = json.getString("historyPoint");
		if (!HawkOSOperator.isEmptyString(historyPoint)) {
			String[] timePointsArr = historyPoint.split(";");
			for (String timePoints : timePointsArr) {
				String[] arr = timePoints.split("_");
				object.historyPointMap.put(Long.valueOf(arr[0]), SerializeHelper.stringToList(Integer.class, arr[1], ","));
			}
		}
		return object;
	}
	
}
