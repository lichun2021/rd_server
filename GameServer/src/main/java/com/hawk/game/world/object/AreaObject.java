package com.hawk.game.world.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.service.WorldPointService;

/**
 * 区域
 * @author golden
 *
 */
public class AreaObject {
	
	protected int id;
	
	protected int beginX;

	protected int beginY;

	protected int endX;

	protected int endY;
	
	/**
	 * 所有点
	 */
	protected Map<Integer, Point> allPoints = new ConcurrentHashMap<Integer, Point>();
	
	/**
	 * 空闲点
	 */
	protected Map<Integer, Point> freePoints = new ConcurrentHashMap<Integer, Point>();
	
	/**
	 * 区域内每个迷雾ID对应的数量
	 */
	private Map<Integer, Integer> foggyNum = new HashMap<Integer, Integer>();
	
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
	 * 据点<坐标>
	 */
	private Set<Integer> strongpoints = new HashSet<>();
	
	/**
	 * 野怪boss
	 */
	private Set<Integer> monsterBoss = new HashSet<>();
	
	/**
	 * 记录区域宝箱数量
	 */
	protected int boxNum;
	
	/**
	 * 默认构造
	 */
	
	public AreaObject() {
		
	}

	/**
	 * 构造函数
	 * 
	 * @param id
	 * @param beginX
	 * @param beginY
	 * @param endX
	 * @param endY
	 */
	public AreaObject(int id, int beginX, int beginY, int endX, int endY) {
		this.id = id;
		this.beginX = beginX;
		this.beginY = beginY;
		this.endX = endX;
		this.endY = endY;
	}

	public int getId() {
		return id;
	}

	public int getBeginX() {
		return beginX;
	}

	public int getBeginY() {
		return beginY;
	}

	public int getEndX() {
		return endX;
	}

	public int getEndY() {
		return endY;
	}

	/**
	 * 初始化区域内的点
	 * @return
	 */
	public boolean initAreaPoints() {
		for (int i = beginX; i <= endX; i++) {
			for (int j = beginY; j <= endY; j++) {
				if (MapBlock.getInstance().isStopPoint(GameUtil.combineXAndY(i, j))) {
					continue;
				}
				Point point = new Point(i, j, this.id, WorldUtil.getPointResourceZone(i, j));
				allPoints.put(point.getId(), point);
				freePoints.put(point.getId(), point);
			}
		}
		return true;
	}

	/**
	 * 添加空闲点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean addFreePoint(int x, int y) {
		int id = GameUtil.combineXAndY(x, y);
		Point point = allPoints.get(id);
		if (point != null) {
			freePoints.put(id, point);
			return true;
		}
		return false;
	}

	/**
	 * 移除空闲点
	 * 
	 * @param x
	 * @param y
	 */
	public Point removeFreePoint(int x, int y) {
		int id = GameUtil.combineXAndY(x, y);
		return freePoints.remove(id);
	}

	/**
	 * 获取空闲点个数
	 * 
	 * @return
	 */
	public int getFreePointCount() {
		return freePoints.size();
	}

	/**
	 * 获取空闲点
	 * 
	 * @return
	 */
	public Point getFreePoint(int x, int y) {
		int id = GameUtil.combineXAndY(x, y);
		return freePoints.get(id);
	}
	
	/**
	 * 返回当前区块所有点列表
	 * 
	 * @return
	 */
	public List<Point> getAllPointList() {
		List<Point> pointList = new ArrayList<Point>(allPoints.size());
		pointList.addAll(allPoints.values());
		return pointList;
	}

	/**
	 * 获取所有点的数量
	 * 
	 * @return
	 */
	public int getTotalPointCount() {
		return allPoints.size();
	}

	/**
	 * 获取区域内制定坐标的有效点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getAreaPoint(int x, int y) {
		Integer pointId = GameUtil.combineXAndYCacheIndex(x, y);
		return allPoints.get(pointId);
	}

	/**
	 * 是否为空闲点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isFreePoint(int x, int y) {
		return freePoints.containsKey(GameUtil.combineXAndY(x, y));
	}

	/**
	 * 获取已使用的点集合
	 * 
	 * @return
	 */
	public List<Point> getUsedPoints() {
		List<Point> pointList = new ArrayList<Point>(allPoints.size());
		for (Entry<Integer, Point> entry : allPoints.entrySet()) {
			if (!freePoints.containsKey(entry.getKey())) {
				pointList.add(entry.getValue());
			}
		}
		return pointList;
	}

	/**
	 * 获取制定类型的有效可用点列表
	 * 
	 * @param worldPointType
	 * @return
	 */
	public List<Point> getValidPoints(int worldPointType, MonsterType monsterType) {
		List<Point> pointList = new ArrayList<Point>(allPoints.size());
		// 去除所有本类型可使用的点
		for (Point point : freePoints.values()) {
			if (worldPointType == WorldPointType.PLAYER_VALUE && !point.canPlayerSeat()) {
				continue;
			} else if(worldPointType == WorldPointType.YURI_FACTORY_VALUE && !point.canYuriSeat()){
				continue;
			} else if (worldPointType == WorldPointType.MONSTER_VALUE && !point.canMonsterGen(monsterType)) {
				continue;
			} else if (worldPointType == WorldPointType.RESOURCE_VALUE && !point.canRMSeat()) {
				continue;
			} else if (worldPointType == WorldPointType.BOX_VALUE && !point.canRMSeat()) {
				continue;
			} else if (worldPointType == WorldPointType.STRONG_POINT_VALUE && !point.canRMSeat()){
				continue;
			} else if (worldPointType == WorldPointType.FOGGY_FORTRESS_VALUE && !point.canYuriSeat()){
				continue;
			}
			
			pointList.add(point);
		}
		return pointList;
	}
	
	/**
	 * 获取制定类型的有效可用点列表
	 * @param worldPointType
	 * @param monsterType
	 * @param captalAreaControl 是否黑土地控制
	 * @param needInCaptalArea 是否需要在黑土地内
	 * @return
	 */
	public List<Point> getValidPoints(WorldPointType worldPointType, MonsterType monsterType, boolean captalAreaControl, boolean needInCaptalArea) {
		List<Point> pointList = new ArrayList<Point>(allPoints.size());
		// 去除所有本类型可使用的点
		for (Point point : freePoints.values()) {
			if (captalAreaControl && (needInCaptalArea != WorldPointService.getInstance().isInCapitalArea(point.getId()))) {
				continue;
			}
			if (worldPointType.equals(WorldPointType.PLAYER) && !point.canPlayerSeat()) {
				continue;
			} else if(worldPointType.equals(WorldPointType.YURI_FACTORY) && !point.canYuriSeat()){
				continue;
			} else if (worldPointType.equals(WorldPointType.MONSTER) && !point.canMonsterGen(monsterType)) {
				continue;
			} else if (worldPointType.equals(WorldPointType.RESOURCE) && !point.canRMSeat()) {
				continue;
			} else if (worldPointType.equals(WorldPointType.BOX) && !point.canRMSeat()) {
				continue;
			} else if (worldPointType.equals(WorldPointType.STRONG_POINT) && !point.canRMSeat()){
				continue;
			} else if (worldPointType.equals(WorldPointType.FOGGY_FORTRESS) && !point.canYuriSeat()){
				continue;
			} else if (worldPointType.equals(WorldPointType.PYLON) && !point.canYuriSeat()){
				continue;
			}
			
			pointList.add(point);
		}
		return pointList;
	}
	
	/**
	 * 增加区域内宝箱数量
	 * @param count
	 */
	public void addBoxNum(int count) {
		boxNum += count;
	}
	
	/**
	 * 减少区域内宝箱数量
	 * @param count
	 */
	public void deleteBoxNum(int num) {
		boxNum = boxNum >= num ? (boxNum - num) : 0;
	}
	
	/**
	 * 获取区域内宝箱数量
	 * @param num
	 * @return
	 */
	public int getBoxNum() {
		return boxNum;
	}
	/**
	 * 获取区域内monsterId野怪的数量
	 * @param monsterId
	 * @return
	 */
	public int getFoggyIdNum(int foggyLevel) {
		if (!foggyNum.containsKey(foggyLevel)) {
			foggyNum.put(foggyLevel, 0);
		}
		return foggyNum.get(foggyLevel);
	}
	
	/**
	 * 增加某Id的怪物
	 * @param monsterId
	 */
	public void addFoggyIdNum(int foggyLevel){
		if(foggyNum.containsKey(foggyLevel)){
			foggyNum.put(foggyLevel, getFoggyIdNum(foggyLevel) + 1);
		} else {
			foggyNum.put(foggyLevel, 1);
		}
	}
	
	/**
	 * 删除某Id的怪物
	 * @param monsterId
	 */
	public void delFoggyIdNum(int foggyLevel){
		if(foggyNum.containsKey(foggyLevel)){
			int val = getFoggyIdNum(foggyLevel) - 1;
			if(val < 0){
				WorldPointService.logger.error("FoggyIdNum value error, val:{} ", val);
				val = 0;
			}
			foggyNum.put(foggyLevel, val);
		}
	}
	
	/**
	 * 获取当前区域内怪物小于这个值的迷雾id
	 * @return
	 */
	public int getNeedFoggyId(List<Integer> foggyIds){
		//区域内普通怪物总量
		int allNum = getAreaFoggyCount();
		Set<Integer> set = new HashSet<Integer>();
		//重新计算等级
		for (Integer id : foggyIds) {
			FoggyFortressCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, id);
			set.add(cfg.getLevel());
		}
		// 获取当前可刷新野怪种类数量
		int kindsCount = set.size();
		// 保底值
		double val = Math.ceil((allNum * (WorldMapConstProperty.getInstance().getWorldEnemyFoggyLevelMinLimitCof() / 1000.0)) / kindsCount);
		List<Integer> res = new ArrayList<Integer>();
		// 找出对应迷雾值
		for (int foggyId : foggyIds) {
			FoggyFortressCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if(getFoggyIdNum(cfg.getLevel()) < val){
				res.add(foggyId);
			}
		}
		if(res.isEmpty()){
			return -1;
		}
		return HawkRand.randomObject(res);
	}
	
	/**
	 * 区域普通野怪数量
	 * @return
	 */
	public int getAreaFoggyCount() {
		int allNum = 0;
		for (Integer num : foggyNum.values()) {
			allNum += num;
		}
		return allNum;
	}

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
			commonMonster.put(monsterId, monsters);
		}
		monsters.add(pointId);
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
		monsters.remove(pointId);
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
	 * 获取活动怪坐标点
	 * @param monsterId
	 * @return
	 */
	public Map<Integer, Set<Integer>>  getActivityMonsterPoints() {
		return activityMonster;
	}
	
	/**
	 * 获取活动怪坐标点
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
	 * 区域添加活动怪
	 * @param monsterId
	 * @param pointId
	 */
	public void addActivityMonster(int monsterId, int pointId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			monsters = new HashSet<>();
			activityMonster.put(monsterId, monsters);
		}
		monsters.add(pointId);
	}
	
	/**
	 * 区域删除活动怪
	 * @param monsterId
	 * @param pointId
	 */
	public void deleteActivityMonster(int monsterId, int pointId) {
		Set<Integer> monsters = activityMonster.get(monsterId);
		if (monsters == null || monsters.isEmpty()) {
			return;
		}
		monsters.remove(pointId);
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
	 * 获取活动怪数量
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
	 * 获取活动怪数量
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
	
	/**
	 * 添加野怪boss
	 * @param pointId
	 */
	public void addMonsterBoss(int pointId) {
		monsterBoss.add(pointId);
	}
	
	/**
	 * 获取野怪boss坐标
	 * @return
	 */
	public Set<Integer> getMonsterBoss() {
		return monsterBoss;
	}
	
	/**
	 * 删除野怪boss
	 * @param pointId
	 */
	public void removeMonsterBoss(int pointId) {
		monsterBoss.remove(pointId);
	}
}
