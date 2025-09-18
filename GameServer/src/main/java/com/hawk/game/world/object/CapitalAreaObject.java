package com.hawk.game.world.object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 黑土地(王城)区域
 * @author golden
 *
 */
public class CapitalAreaObject {
	
	/**
	 * 普通野怪<id,坐标>
	 */
	private Map<Integer, Set<Integer>> commonMonster = new HashMap<Integer, Set<Integer>>();
	
	/**
	 * 活动野怪<id,坐标>
	 */
	private Map<Integer, Set<Integer>> activityMonster = new HashMap<Integer, Set<Integer>>();
	
	/**
	 * 资源<type,坐标>
	 */
	private Map<Integer, Set<Integer>> resource = new HashMap<Integer, Set<Integer>>();
	
	/**
	 * 据点<id,坐标>
	 */
	private Set<Integer> strongpoints = new HashSet<>();
	
	/**
	 * 获取普通野怪坐标点
	 * @param monsterId
	 * @return
	 */
	public Set<Integer>  getConmmonMonsterPoints(int monsterId) {
		Set<Integer> monsters = commonMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			monsters = new HashSet<>();
		}
		return monsters;
	}
	
	/**
	 * 区域添加普通野怪
	 * @param monsterId
	 * @param pointId
	 */
	public void addCommonMonster(int monsterId, int pointId) {
		Set<Integer> monsters = commonMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			monsters = new HashSet<>();
		}
		monsters.add(pointId);
		commonMonster.put(monsterId, monsters);
	}
	
	/**
	 * 区域删除普通野怪
	 * @param monsterId
	 * @param pointId
	 */
	public void deleteCommonMonster(int monsterId, int pointId) {
		Set<Integer> monsters = commonMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			return;
		}
		monsters.remove(Integer.valueOf(pointId));
		commonMonster.put(monsterId, monsters);
	}
	
	/**
	 * 获取普通野怪数量
	 * @param monsterId
	 * @return
	 */
	public int getCommonMonsterNum(int monsterId) {
		Set<Integer> monsters = commonMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			return 0;
		}
		return monsters.size();
	}
	
	/**
	 * 获取普通野怪数量
	 * @param monsterId
	 * @return
	 */
	public int getCommonMonsterNum() {
		int count = 0;
		if (commonMonster == null || commonMonster.isEmpty()) {
			return count;
		}
		for (Set<Integer> monsters : commonMonster.values()) {
			count += monsters.size();
		}
		return count;
	}
	
	/**
	 * 获取普通野怪坐标点
	 * @param monsterId
	 * @return
	 */
	public Set<Integer>  getActivityMonsterPoints(int monsterId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			monsters = new HashSet<>();
		}
		return monsters;
	}
	
	/**
	 * 区域添加普通野怪
	 * @param monsterId
	 * @param pointId
	 */
	public void addActivityMonster(int monsterId, int pointId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			monsters = new HashSet<>();
		}
		monsters.add(pointId);
		activityMonster.put(monsterId, monsters);
	}
	
	/**
	 * 区域删除普通野怪
	 * @param monsterId
	 * @param pointId
	 */
	public void deleteActivityMonster(int monsterId, int pointId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			return;
		}
		monsters.remove(Integer.valueOf(pointId));
		activityMonster.put(monsterId, monsters);
	}
	
	/**
	 * 获取普通野怪数量
	 * @param monsterId
	 * @return
	 */
	public int getActivityMonsterNum(int monsterId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			return 0;
		}
		return monsters.size();
	}
	
	/**
	 * 获取普通野怪数量
	 * @param monsterId
	 * @return
	 */
	public int getActivityMonsterNum() {
		int count = 0;
		if (activityMonster == null || activityMonster.isEmpty()) {
			return count;
		}
		for (Set<Integer> monsters : activityMonster.values()) {
			count += monsters.size();
		}
		return count;
	}
	
	/**
	 * 获取活动怪坐标点
	 * @param monsterId
	 * @return
	 */
	public Map<Integer, Set<Integer>>  getActivityMonsterPoints() {
		return activityMonster;
	}
	
	/**
	 * 区域删除活动怪
	 * @param monsterId
	 * @param pointId
	 */
	public void clearActivityMonster() {
		activityMonster.clear();
	}
	

	/**
	 * 获取资源坐标点
	 * @param resourceType
	 * @return
	 */
	public Set<Integer>  getResourcePoints(int resourceType) {
		Set<Integer> points = resource.get(resourceType);
		if (points == null || points.isEmpty()) {
			points = new HashSet<>();
			resource.put(resourceType, points);
		}
		return points;
	}
	
	/**
	 * 获取资源坐标点
	 * @param resourceType
	 * @return
	 */
	public Map<Integer, Set<Integer>>  getResourcePoints() {
		return resource;
	}
	
	/**
	 * 区域添加资源点
	 * @param resourceType
	 * @param pointId
	 */
	public void addResourcePoint(int resourceType, int pointId) {
		Set<Integer> points = resource.get(resourceType);
		if (points == null || points.isEmpty()) {
			points = new HashSet<>();
			resource.put(resourceType, points);
		}
		points.add(pointId);
	}
	
	/**
	 * 区域删除资源点
	 * @param resourceType
	 * @param pointId
	 */
	public void deleteResourcePoint(int resourceType, int pointId) {
		Set<Integer> points = resource.get(resourceType);
		if (points == null || points.isEmpty()) {
			return;
		}
		points.remove(pointId);
	}
	
	/**
	 * 获取资源点数量
	 * @param resourceType
	 * @return
	 */
	public int getResourceNum(int resourceType) {
		Set<Integer> points = resource.get(resourceType);
		if (points == null || points.isEmpty()) {
			return 0;
		}
		return points.size();
	}
	
	/**
	 * 获取资源点数量
	 * @return
	 */
	public int getResourceNum() {
		int count = 0;
		if (resource == null || resource.isEmpty()) {
			return count;
		}
		for (Set<Integer> points : resource.values()) {
			count += points.size();
		}
		return count;
	}
	

	/**
	 * 获取据点坐标点
	 * @param resourceType
	 * @return
	 */
	public Set<Integer>  getStrongpointPoints() {
		return strongpoints;
	}
	
	/**
	 * 区域添加据点
	 * @param resourceType
	 * @param pointId
	 */
	public void addStrongpointPoint(int pointId) {
		strongpoints.add(pointId);
	}
	
	/**
	 * 区域删除据点
	 * @param resourceType
	 * @param pointId
	 */
	public void deleteStrongpointPoint(int pointId) {
		strongpoints.remove(pointId);
	}
	
	/**
	 * 获取据点数量
	 * @param resourceType
	 * @return
	 */
	public int getStrongpointNum() {
		return strongpoints.size();
	}
}
