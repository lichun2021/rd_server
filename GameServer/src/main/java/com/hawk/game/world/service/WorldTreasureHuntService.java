package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.TreasureHuntConstProperty;
import com.hawk.game.config.TreasureHuntResCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;

/**
 * 世界寻宝服务类
 * @author golden
 *
 */
public class WorldTreasureHuntService extends HawkAppObj {

	/**
	 * 日志对象
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static WorldTreasureHuntService instance = null;

	/**
	 * 获取单例对象
	 * 
	 * @return
	 */
	public static WorldTreasureHuntService getInstance() {
		return instance;
	}

	/**
	 * 构造
	 * @param xid
	 */
	public WorldTreasureHuntService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 上次tick时间
	 */
	public long lastTickTime;
	
	/**
	 * 世界上野怪列表
	 */
	public Map<Integer, WorldPoint> monsters;

	/**
	 * 世界上资源列表
	 */
	public Map<Integer, WorldPoint> resources;

	/**
	 * 初始化
	 */
	public boolean init() {
		monsters = new ConcurrentHashMap<Integer, WorldPoint>();
		resources = new ConcurrentHashMap<Integer, WorldPoint>();

		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.TH_MONSTER);
		for (WorldPoint point : points) {
			monsters.put(point.getId(), point);
		}

		points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.TH_RESOURCE);
		for (WorldPoint point : points) {
			resources.put(point.getId(), point);
		}

		lastTickTime = HawkTime.getMillisecond();
		
		logger.info("initTreasureHunt, monsterCount:{}, resureceCount:{}", monsters.size(), resources.size());

		return true;
	}

	@Override
	public boolean onTick() {
		if (HawkTime.getMillisecond() - lastTickTime < GsConst.HOUR_MILLI_SECONDS) {
			return true;
		}
		
		lastTickTime = HawkTime.getMillisecond();
		
		// 移除过期野怪
		List<WorldPoint> rmMonster = new ArrayList<>();
		for (WorldPoint monster : monsters.values()) {
			WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monster.getMonsterId());
			if (cfg == null) {
				continue;
			}
			if (HawkTime.getMillisecond() - monster.getLifeStartTime() < cfg.getLifeTime() * 1000L) {
				continue;
			}
			rmMonster.add(monster);
		}
		
		for (WorldPoint point : rmMonster) {
			notifyMonsterRemove(point.getId(), null, null);
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
		}
		WorldPointProxy.getInstance().batchDelete(rmMonster);
		
		// 移除过期资源
		List<WorldPoint> rmRes = new ArrayList<>();
		for (WorldPoint res : resources.values()) {
			TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, res.getResourceId());
			if (cfg == null) {
				continue;
			}
			if (HawkTime.getMillisecond() - res.getLifeStartTime() < cfg.getLifeTime() * 1000L) {
				continue;
			}
			if (!HawkOSOperator.isEmptyString(res.getPlayerId())) {
				continue;
			}
			rmRes.add(res);
		}
		
		for (WorldPoint point : rmRes) {
			notifyResRemove(point.getId(), null, null);
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
		}
		WorldPointProxy.getInstance().batchDelete(rmRes);
		
		return true;
	}

	/**
	 * 触发世界生成野怪(需要抛到世界线程处理)
	 */
	public void touchCreateMonster() {
		int currentCount = monsters.size();
		int maxCount = TreasureHuntConstProperty.getInstance().getMonsterMaxCount();
		if (currentCount >= maxCount) {
			logger.info("touchCreateMonster, has full, currentCount:{}, maxCount:{}", currentCount, maxCount);
			
			LogUtil.logTreasureHuntTouceMonster(0, currentCount);
			return;
		}
		
		int createCount = TreasureHuntConstProperty.getInstance().getMonsterOnceCount();
		createCount = Math.min(createCount, maxCount - currentCount);
		
		// 生成点列表
		List<WorldPoint> createPoints = new ArrayList<>();
		
		for (int i = 0; i < createCount; i++) {
			WorldPoint point = createMonster();
			if (point == null) {
				continue;
			}
			createPoints.add(point);
		}
		
		WorldPointProxy.getInstance().batchCreate(createPoints);
		
		LogUtil.logTreasureHuntTouceMonster(createCount, monsters.size());
		
		logger.info("touchCreateMonster, currentCount:{}, createCount:{}, maxCount:{}", currentCount, createCount, maxCount);
	}
	
	/**
	 * 生成野怪
	 */
	private WorldPoint createMonster() {
		
		int randomX = HawkRand.randInt(TreasureHuntConstProperty.getInstance().getBornMinX(), TreasureHuntConstProperty.getInstance().getBornMaxX());
		int randomY = HawkRand.randInt(TreasureHuntConstProperty.getInstance().getBornMinY(), TreasureHuntConstProperty.getInstance().getBornMaxY());
		int randomRadius = TreasureHuntConstProperty.getInstance().getRandomRadius();
		
		List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(randomX, randomY, randomRadius);
		for (Point point : points) {

			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			
			if (!point.canYuriSeat()) {
				continue;
			}
			
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				continue;
			}
			
			int monsterId = TreasureHuntConstProperty.getInstance().randomMonsterId();
			
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
			if (monsterCfg == null) {
				logger.error("createTreasureHuntMonster failed, cfg is null, monsterId:{}", monsterId);
				break;
			}
			
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.TH_MONSTER_VALUE);
			worldPoint.setMonsterId(monsterId);
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			int maxEnemyBlood = getMaxEnemyBlood(monsterId);
			worldPoint.setRemainBlood(maxEnemyBlood);
			WorldPointService.getInstance().addPoint(worldPoint);
			
			monsters.put(worldPoint.getId(), worldPoint);
			
			logger.info("createTreasureHuntMonster, x:{}, y:{}, areaId:{}, blood:{}, monsterId:{}", point.getX(), point.getY(), point.getAreaId(), worldPoint.getRemainBlood(), monsterId);
			
			return worldPoint;
		}
		
		return null;
	}
	
	/**
	 * 获取怪物最大血量
	 * @param monsterCfg
	 * @return
	 */
	public int getMaxEnemyBlood(int monsterId) {
		// 野怪配置
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if (cfg == null) {
			return 0;
		}
		
		int totalEnemyBlood = 0;
		List<ArmyInfo> armyList = cfg.getArmyList();
		for (ArmyInfo army : armyList) {
			totalEnemyBlood += army.getTotalCount();
		}
		return totalEnemyBlood;
	}
	
	/**
	 * 通知野怪移除
	 */
	public void notifyMonsterRemove(int pointId, String leaderId, String marchId) {
		WorldPointService.getInstance().removeWorldPoint(pointId);
		monsters.remove(pointId);
		
		int[] pos = GameUtil.splitXAndY(pointId);
		logger.info("notifyTHMonsterRemove, posX:{}, posY:{}, leaderId:{}, marchId:{}, currentCount:{}", pos[0], pos[1], leaderId, marchId, monsters.size());
	}
	
	/**
	 * 触发世界生成资源(需要抛到世界线程处理)
	 */
	public void touchCreateResource() {
		int currentCount = resources.size();
		int maxCount = TreasureHuntConstProperty.getInstance().getResMaxCount();
		if (currentCount >= maxCount) {
			logger.info("touchCreateResource, has full, currentCount:{}, maxCount:{}", currentCount, maxCount);
			
			LogUtil.logTreasureHuntTouceResource(0, currentCount);
			return;
		}
		
		int createCount = TreasureHuntConstProperty.getInstance().getResOnceCount();
		createCount = Math.min(createCount, maxCount - currentCount);
		
		// 生成点列表
		List<WorldPoint> createPoints = new ArrayList<>();
		
		for (int i = 0; i < createCount; i++) {
			WorldPoint point = createResource();
			if (point == null) {
				continue;
			}
			createPoints.add(point);
		}
		
		WorldPointProxy.getInstance().batchCreate(createPoints);
		
		LogUtil.logTreasureHuntTouceResource(createCount, resources.size());
		
		logger.info("touchCreateResource, currentCount:{}, createCount:{}, maxCount:{}", currentCount, createCount, maxCount);
	}
	
	/**
	 * 生成资源
	 */
	private WorldPoint createResource() {
		
		int randomX = HawkRand.randInt(TreasureHuntConstProperty.getInstance().getBornMinX(), TreasureHuntConstProperty.getInstance().getBornMaxX());
		int randomY = HawkRand.randInt(TreasureHuntConstProperty.getInstance().getBornMinY(), TreasureHuntConstProperty.getInstance().getBornMaxY());
		int randomRadius = TreasureHuntConstProperty.getInstance().getRandomRadius();
		
		List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(randomX, randomY, randomRadius);
		for (Point point : points) {
			
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());

			if (!point.canYuriSeat()) {
				continue;
			}

			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}

			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}

			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				continue;
			}
			
			int resId = TreasureHuntConstProperty.getInstance().randomResId();
			
			// 创建世界点对象
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.TH_RESOURCE_VALUE);
			worldPoint.setResourceId(resId);
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			WorldPointService.getInstance().addPoint(worldPoint);
			
			resources.put(worldPoint.getId(), worldPoint);
			
			logger.info("createTreasureHuntRes, x:{}, y:{}, areaId:{}, resId:{}", point.getX(), point.getY(), point.getAreaId(), resId);
			
			return worldPoint;
		}
		
		return null;
	}

	/**
	 * 通知资源移除
	 */
	public void notifyResRemove(int pointId, String playerId, String marchId) {
		WorldPointService.getInstance().removeWorldPoint(pointId);
		resources.remove(pointId);
		
		int[] pos = GameUtil.splitXAndY(pointId);
		logger.info("notifyTHResRemove, posX:{}, posY:{}, playerId:{}, marchId:{}, currentCount:{}", pos[0], pos[1], playerId, marchId, resources.size());
	}
}
