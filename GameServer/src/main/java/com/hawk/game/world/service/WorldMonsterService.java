package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.GsApp;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldEnemyRefreshCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMonsterRefreshCfg;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.MapUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 世界怪全局服务类
 * @author zhenyu.shang
 * @since 2017年8月15日
 */
public class WorldMonsterService extends HawkAppObj {

	private static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * tick周期
	 */
	private static final long TICKPERIOD = 10 * 1000L;
	
	/**
	 * 上次tick时间
	 */
	private long lastTickTime = 0L;
	
	/**
	 * 上次刷新时间
	 */
	private long lastRefreshTime = 0L;

	/**
	 * 
	 */
	private static WorldMonsterService instance = null;

	
	public static WorldMonsterService getInstance() {
		return instance;
	}
	
	public WorldMonsterService(HawkXID xid) {
		super(xid);
		instance = this;
		long currentTime = HawkTime.getMillisecond();
		lastTickTime = currentTime;
		lastRefreshTime = currentTime;
	}
	
	
	
	public boolean init() {
		
		// 初始化野怪boss
		List<WorldPoint> monsterPoints = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.MONSTER);
		for (WorldPoint point : monsterPoints) {
			WorldEnemyCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
			if (config.getType() != MonsterType.TYPE_3_VALUE) {
				continue;
			}
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			area.addMonsterBoss(point.getId());
		}
		
		Collection<AreaObject> areas = WorldPointService.getInstance().getAreaVales();
		
		// 活动怪是否开启
		boolean isActivityMonsterOpen = isActivityMonsterOpen();
		
		// 刷新普通怪
		refreshCapitalAreaCommonMonster(isActivityMonsterOpen);
		for (AreaObject area : areas) {
			refreshAreaCommonMonster(area, isActivityMonsterOpen);
		}
		
		// 刷新活动怪
		if (isActivityMonsterOpen) {
			refreshCapitalAreaActivityMonster();
			for (AreaObject area : areas) {
				refreshAreaActivityMonster(area);
			}
		} else {
			removeCapitalAreaActivityMonster();
			for (AreaObject area : areas) {
				removeAreaActivityMonster(area);
			}
		}
		
		// 刷新野怪boss
		for (AreaObject area : areas) {
			refreshAreaMonsterBoss(area);
		}
		
		return true;
	}

	
	@Override
	public boolean onTick() {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastTickTime < TICKPERIOD) {
			return true;
		}
		lastTickTime = currentTime;
		
		int monsterRefreshTime = WorldMapConstProperty.getInstance().getMonsterRefreshTime();
		if (currentTime - lastRefreshTime > monsterRefreshTime * 1000L) {
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REFRESH_MONSTER) {
				@Override
				public boolean onInvoke() {
					lastRefreshTime = currentTime;
					notifyRefreshCommonMonster();
					notifyRefreshActivityMonster();
					return true;
				}
			});
		}
		return true;
	}
	
	/**
	 * 通知怪物被杀, 扣血, 血量为0即删除重新刷
	 * 
	 * @param worldPoint
	 * @return
	 */
	public long notifyMonsterKilled(WorldPoint worldPoint) {
		try {
			// 点为null || (不是野怪 && 不是机器人)
			if (worldPoint == null || (worldPoint.getPointType() != WorldPointType.MONSTER_VALUE && worldPoint.getPointType() != WorldPointType.ROBOT_VALUE)) {
				return 0;
			}
			
			// 是否在黑土地区域
			boolean isInCaptalArea = WorldPointService.getInstance().isInCapitalArea(worldPoint.getId());
			
			// 怪物配置
			WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, worldPoint.getMonsterId());
			if (enemyCfg == null) {
				return 0;
			}
			
			// 删除原有的怪点数据
			WorldPointService.getInstance().removeWorldPoint(worldPoint.getX(), worldPoint.getY());
			logger.info("remove monster worldPoint, pos: ({}, {})", worldPoint.getX(), worldPoint.getY());
			AreaObject areaObj = WorldPointService.getInstance().getArea(worldPoint.getAreaId());
			if (areaObj == null) {
				return 0;
			}
			
			MonsterType monsterType = MonsterType.valueOf(enemyCfg.getType());
			if(monsterType == MonsterType.TYPE_1){
				if (isInCaptalArea) {
					CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
					captialArea.deleteCommonMonster(enemyCfg.getId(), worldPoint.getId());
				} else {
					areaObj.deleteCommonMonster(enemyCfg.getId(), worldPoint.getId());
				}
			}
			
			if (monsterType == MonsterType.TYPE_2) {
				if (isInCaptalArea) {
					CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
					captialArea.deleteActivityMonster(enemyCfg.getId(), worldPoint.getId());
				} else {
					areaObj.deleteActivityMonster(enemyCfg.getId(), worldPoint.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return 0;
	}

	/**
	 * 获取怪物最大血量
	 * @param monsterCfg
	 * @return
	 */
	public int getMaxEnemyBlood(int monsterId) {
		// 野怪配置
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if (monsterCfg == null) {
			return 0;
		}
		
		int totalEnemyBlood = 0;
		List<ArmyInfo> armyList = monsterCfg.getArmyList();
		for (ArmyInfo army : armyList) {
			totalEnemyBlood += army.getTotalCount();
		}
		return totalEnemyBlood;
	}

	/**
	 * 野怪半径
	 * @param monsterType
	 * @return
	 */
	public int getMonsterRadius(int monsterId) {
		WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
		if (monsterCfg == null) {
			logger.error("getMonsterRadius, monsterCfg null, monsterId:{}", monsterId);
			return 0;
		}

		int radius = 1;
		switch (monsterCfg.getType()) {
		case MonsterType.TYPE_3_VALUE:
		case MonsterType.TYPE_4_VALUE:
		case MonsterType.TYPE_5_VALUE:
			radius = 2;
			break;
		default:
			radius = 1;
			break;
		}
		return radius;
	}
	
	/**
	 * 根据开服时间 获取当前怪物刷新规则
	 * @return
	 */
	public WorldEnemyRefreshCfg getCurrentEnemyRefreshCfg(){
		long hasOpenTime = HawkApp.getInstance().getCurrentTime() - GameUtil.getServerOpenTime();
		//如果还没到开服时间，则取第一组就行
		if(hasOpenTime <= 0){
			return HawkConfigManager.getInstance().getConfigByIndex(WorldEnemyRefreshCfg.class, 0);
		}
		WorldEnemyRefreshCfg res = null;
		//跟据开服时间, 取出最大的一组
		ConfigIterator<WorldEnemyRefreshCfg> it = HawkConfigManager.getInstance().getConfigIterator(WorldEnemyRefreshCfg.class);
		long lastTime = 0;
		while (it.hasNext()) {
			WorldEnemyRefreshCfg worldEnemyRefreshCfg = it.next();
			long openServiceTime = worldEnemyRefreshCfg.getOpenServiceTime() * 1000L;
			if (openServiceTime >= lastTime && hasOpenTime > openServiceTime){
				res = worldEnemyRefreshCfg;
				lastTime = openServiceTime;
			}
		}
		return res;
	}
	
	/**
	 * 通知区域刷新老版野怪
	 * @param areaId 区域id
	 * 
	 */
	public void notifyRefreshCommonMonster() {
		// 活动怪是否开启
		boolean isActivityMonsterOpen = isActivityMonsterOpen();
		refreshCapitalAreaCommonMonster(isActivityMonsterOpen);
		refreshAreaCommonMonster(1, isActivityMonsterOpen);
	}
	
	/**
	 * 刷新区域野怪
	 * @param areaId
	 */
	private void refreshAreaCommonMonster(int areaId, boolean isActivityMonsterOpen) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		
		try {
			refreshAreaCommonMonster(area, isActivityMonsterOpen);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getMonsterRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.WORLD_Old_MONSTER_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					refreshAreaCommonMonster(areaId + 1, isActivityMonsterOpen);
					return true;
				}
			});
		}
	}
	
	/**
	 * 区域生成老版野怪(不包含黑土地区域)
	 * @param areaObject
	 */
	public void refreshAreaCommonMonster(AreaObject areaObj, boolean isActivityMonsterOpen) {
		long startTime = HawkTime.getMillisecond();
		// 区域有效点列表
		List<Point> validPoints = areaObj.getValidPoints(WorldPointType.MONSTER, MonsterType.TYPE_1, true, false);
		// 列表乱序
		Collections.shuffle(validPoints);
		
		boolean isSpecialArea = WorldMapConstProperty.getInstance().isSpecialAreaId(areaObj.getId());
		
		// 需要刷新的野怪
		Map<Integer, Integer> refreshMonster = getCommonMonsterRefreshCfg().getRefreshCommon();
		
		// 如果是特殊区块
		if (!isActivityMonsterOpen && isSpecialArea) {
			refreshMonster = getCommonMonsterRefreshCfg().getRefreshSpecial();
			
			// 活动怪开启，并且是普通区块
		} else if (isActivityMonsterOpen && !isSpecialArea) {
			refreshMonster = getCommonMonsterRefreshCfg().getRefreshActiNormalCommon();
			
			// 活动怪开启，并且是特殊区块
		} else if (isActivityMonsterOpen && isSpecialArea) {
			refreshMonster = getCommonMonsterRefreshCfg().getRefreshActiNormalSpecial();
		}
		
		int validPointIndex  = 0;
		for (Integer monsterId : getMonsterIds(MonsterType.TYPE_1_VALUE)) {
			int currentCount = areaObj.getCommonMonsterNum(monsterId);
			int targetCount = MapUtil.getIntValue(refreshMonster, monsterId);
			
			// 如果当前数量大于目标数量，随机删除野怪。
			if (currentCount > targetCount) {
				int deleteNum = currentCount - targetCount;
				List<Integer> removeList = new ArrayList<>();
				
				// 区域内野怪点
				Set<Integer> monsterPoints = areaObj.getConmmonMonsterPoints(monsterId);
				for (int monsterPoint : monsterPoints) {
					if (removeList.size() >= deleteNum) {
						break;
					}
					removeList.add(monsterPoint);
				}
				
				for (int remove : removeList) {
					WorldPointService.getInstance().removeWorldPoint(remove, false);
					areaObj.deleteCommonMonster(monsterId, remove);
				}
			}
			// 如果当前数量小于目标数量，生成新的新的野怪。
			if (currentCount < targetCount) {
				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() -  validPointIndex ? validPoints.size() -  validPointIndex : refreshCount;  
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.MONSTER_VALUE);
					worldPoint.setMonsterId(enemyCfg.getId());
					worldPoint.setCityLevel(enemyCfg.getLevel());
					WorldPointService.getInstance().addPoint(worldPoint);
					areaObj.addCommonMonster(monsterId, bornPoint.getId());
				}
				
			}
		}
		logger.info("refresh area common monster, areaId:{}, costtime:{}", areaObj.getId(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 区域生成老版野怪
	 * @param areaObject
	 */
	public void refreshCapitalAreaCommonMonster(boolean isActivityMonsterOpen) {
		List<Point> validPoints = new ArrayList<>();
		for (int specialAreaId : WorldMapConstProperty.getInstance().getSpecialAreaIds()) {
			AreaObject area = WorldPointService.getInstance().getArea(specialAreaId);
			validPoints.addAll(area.getValidPoints(WorldPointType.MONSTER, MonsterType.TYPE_1, true, true));
		}
		
		if (validPoints == null || validPoints.isEmpty()) {
			return;
		}
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		
		// 列表乱序
		Collections.shuffle(validPoints);
		// 需要刷新的野怪
		Map<Integer, Integer> refreshMonster = getCommonMonsterRefreshCfg().getRefreshCapital();
		if (isActivityMonsterOpen) {
			refreshMonster = getCommonMonsterRefreshCfg().getRefreshActiNormalCapital();
		}
		
		int validPointIndex = 0;
		for (Integer monsterId : getMonsterIds(MonsterType.TYPE_1_VALUE)) {
			int currentCount = captialArea.getCommonMonsterNum(monsterId);
			int targetCount = MapUtil.getIntValue(refreshMonster, monsterId);
			
			// 如果当前数量大于目标数量，随机删除野怪。
			if (currentCount > targetCount) {
				int deleteNum = currentCount - targetCount;
				List<Integer> removeList = new ArrayList<>();
				
				// 区域内野怪点
				Set<Integer> monsterPoints = captialArea.getConmmonMonsterPoints(monsterId);
				for (int monsterPoint : monsterPoints) {
					if (removeList.size() >= deleteNum) {
						break;
					}
					removeList.add(monsterPoint);
				}
				
				for (int remove : removeList) {
					WorldPointService.getInstance().removeWorldPoint(remove, false);
					captialArea.deleteCommonMonster(monsterId, remove);
				}
			}
			
			// 如果当前数量小于目标数量，生成新的新的野怪。
			if (currentCount < targetCount) {
				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() -  validPointIndex ? validPoints.size() -  validPointIndex : refreshCount;
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.MONSTER_VALUE);
					worldPoint.setMonsterId(enemyCfg.getId());
					worldPoint.setCityLevel(enemyCfg.getLevel());
					WorldPointService.getInstance().addPoint(worldPoint);
					captialArea.addCommonMonster(monsterId, bornPoint.getId());
				}
			}
		}
		logger.info("refresh capital area common monster");
	}
	
	/**
	 * 获取普通野怪刷新配置
	 * @param resAreaLevel
	 * @return
	 */
	public WorldMonsterRefreshCfg getCommonMonsterRefreshCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldMonsterRefreshCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldMonsterRefreshCfg.class);
		while(configIterator.hasNext()) {
			WorldMonsterRefreshCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldMonsterRefreshCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldMonsterRefreshCfg.class, size - 1);
	}
	
	/**
	 * 获取当前最大等级的普通野怪
	 * @return
	 */
	public int getMaxCommonMonsterLvl() {
		int maxLvl = 0;
		WorldMonsterRefreshCfg oldMonsterRefreshCfg = getCommonMonsterRefreshCfg();
		Map<Integer, Integer> commonRefresh = oldMonsterRefreshCfg.getRefreshCommon();
		for (int monsterId : commonRefresh.keySet()) {
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
			int monsterLvl = monsterCfg.getLevel();
			if (monsterLvl <= maxLvl) {
				continue;
			}
			maxLvl = monsterLvl;
		}
		return maxLvl;
	}
	
	
	/**
	 * 通知区域刷活动怪
	 * @param areaId 区域id
	 * 
	 */
	public void notifyRefreshActivityMonster() {
		// 活动怪是否开启
		boolean isActivityMonsterOpen = isActivityMonsterOpen();
		
		if (isActivityMonsterOpen == false) {
			removeCapitalAreaActivityMonster();
			removeAreaActivityMonster(1);
		} else {
			refreshCapitalAreaActivityMonster();
			refreshAreaActivityMonster(1);
		}
	}
	
	/**
	 * 刷新区域野怪
	 * @param areaId
	 */
	private void removeAreaActivityMonster(int areaId) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		
		try {
			removeAreaActivityMonster(area);
			refreshAreaMonsterBoss(area);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getMonsterRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.WORLD_Old_MONSTER_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					removeAreaActivityMonster(areaId + 1);
					return true;
				}
			});
		}
	}
	
	/**
	 * 刷新区域野怪
	 * @param areaId
	 */
	private void refreshAreaActivityMonster(int areaId) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		refreshAreaActivityMonster(area);
		refreshAreaMonsterBoss(area);
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getMonsterRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.WORLD_Old_MONSTER_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					refreshAreaActivityMonster(areaId + 1);
					return true;
				}
			});
		}
	}
	
	/**
	 * 删除区域活动野怪
	 * @param areaObj
	 */
	public void removeCapitalAreaActivityMonster() {
		long startTime = HawkTime.getMillisecond();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		List<Integer> removeList = new ArrayList<>();
		
		Map<Integer, Set<Integer>> activityMonsterPoints = captialArea.getActivityMonsterPoints();
		for (Set<Integer> typePoints : activityMonsterPoints.values()) {
			for (Integer pointId : typePoints) {
				removeList.add(pointId);
			}
		}
		for (int remove : removeList) {
			WorldPointService.getInstance().removeWorldPoint(remove, false);
		}
		captialArea.clearActivityMonster();
		logger.info("remove captial area activity monster, costTime:{}", HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除区域活动野怪
	 * @param areaObj
	 */
	public void removeAreaActivityMonster(AreaObject areaObj) {
		long startTime = HawkTime.getMillisecond();
		List<Integer> removeList = new ArrayList<>();
		Map<Integer, Set<Integer>> activityMonsterPoints = areaObj.getActivityMonsterPoints();
		for (Set<Integer> typePoints : activityMonsterPoints.values()) {
			for (Integer pointId : typePoints) {
				removeList.add(pointId);
			}
		}
		for (int remove : removeList) {
			WorldPointService.getInstance().removeWorldPoint(remove, false);
		}
		areaObj.clearActivityMonster();
		logger.info("remove area activity monster, areaId:{}, costTime:{}", areaObj.getId(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 区域生成活动野怪
	 * @param areaObject
	 */
	public void refreshAreaActivityMonster(AreaObject areaObj) {
		long startTime = HawkTime.getMillisecond();
		// 区域有效点列表
		List<Point> validPoints = areaObj.getValidPoints(WorldPointType.MONSTER, MonsterType.TYPE_2, true, false);
		// 列表乱序
		Collections.shuffle(validPoints);
		Map<Integer, Integer> refreshMonster = new HashMap<>();
		// 需要刷新的野怪
		if (WorldMapConstProperty.getInstance().isSpecialAreaId(areaObj.getId())) {
			//老活动野怪
			if(this.isActivityMonsterOpenOld()){
				refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiSpecial());
			}
			//185活动的怪
			if(this.isActivityMonsterOpen185()){
				refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiSpecial185());
			}
		}else{
			//老活动野怪
			if(this.isActivityMonsterOpenOld()){
				refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiCommon());
			}
			//185活动的怪
			if(this.isActivityMonsterOpen185()){
				refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiCommon185());
			}
		}
		int validPointIndex = 0;
		for (Integer monsterId : getMonsterIds(MonsterType.TYPE_2_VALUE)) {
			int currentCount = areaObj.getActivityMonsterNum(monsterId);
			int targetCount = MapUtil.getIntValue(refreshMonster, monsterId);
			
			// 如果当前数量大于目标数量，随机删除野怪。
			if (currentCount > targetCount) {
				int deleteNum = currentCount - targetCount;
				List<Integer> removeList = new ArrayList<>();
				
				// 区域内野怪点
				Set<Integer> monsterPoints = areaObj.getActivityMonsterPoints(monsterId);
				for (int monsterPoint : monsterPoints) {
					if (removeList.size() >= deleteNum) {
						break;
					}
					removeList.add(monsterPoint);
				}
				
				for (int remove : removeList) {
					WorldPointService.getInstance().removeWorldPoint(remove, false);
					areaObj.deleteActivityMonster(monsterId, remove);
				}
			}
			// 如果当前数量小于目标数量，生成新的新的野怪。
			if (currentCount < targetCount) {
				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() -  validPointIndex ? validPoints.size() -  validPointIndex : refreshCount;
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.MONSTER_VALUE);
					worldPoint.setMonsterId(enemyCfg.getId());
					worldPoint.setCityLevel(enemyCfg.getLevel());
					WorldPointService.getInstance().addPoint(worldPoint);
					areaObj.addActivityMonster(monsterId, bornPoint.getId());
				}
				
			}
		}
		logger.info("refresh area activity monster, areaId:{}, costTime:{}", areaObj.getId(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 区域生成活动野怪(不包含黑土地区域)
	 * @param areaObject
	 */
	public void refreshCapitalAreaActivityMonster() {
		List<Point> validPoints = new ArrayList<>();
		for (int specialAreaId : WorldMapConstProperty.getInstance().getSpecialAreaIds()) {
			AreaObject area = WorldPointService.getInstance().getArea(specialAreaId);
			validPoints.addAll(area.getValidPoints(WorldPointType.MONSTER, MonsterType.TYPE_2, true, true));
		}
		
		if (validPoints == null || validPoints.isEmpty()) {
			return;
		}
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		
		// 列表乱序
		Collections.shuffle(validPoints);
		// 需要刷新的野怪
		Map<Integer, Integer> refreshMonster = new HashMap<>();
		//老活动野怪
		if(this.isActivityMonsterOpenOld()){
			refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiCapital());
		}
		//185活动的怪
		if(this.isActivityMonsterOpen185()){
			refreshMonster.putAll(getActivityMonsterRefreshCfg().getRefreshActiCapital185());
		}
		int validPointIndex = 0;
		for (Integer monsterId : getMonsterIds(MonsterType.TYPE_2_VALUE)) {
			int currentCount = captialArea.getActivityMonsterNum(monsterId);
			int targetCount = MapUtil.getIntValue(refreshMonster, monsterId);
			
			// 如果当前数量大于目标数量，随机删除野怪。
			if (currentCount > targetCount) {
				int deleteNum = currentCount - targetCount;
				List<Integer> removeList = new ArrayList<>();
				
				// 区域内野怪点
				Set<Integer> monsterPoints = captialArea.getActivityMonsterPoints(monsterId);
				for (int monsterPoint : monsterPoints) {
					if (removeList.size() >= deleteNum) {
						break;
					}
					removeList.add(monsterPoint);
				}
				
				for (int remove : removeList) {
					WorldPointService.getInstance().removeWorldPoint(remove, false);
					captialArea.deleteActivityMonster(monsterId, remove);
				}
			}
			
			// 如果当前数量小于目标数量，生成新的新的野怪。
			if (currentCount < targetCount) {
				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() -  validPointIndex ? validPoints.size() -  validPointIndex : refreshCount;
				WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.MONSTER_VALUE);
					worldPoint.setMonsterId(enemyCfg.getId());
					worldPoint.setCityLevel(enemyCfg.getLevel());
					WorldPointService.getInstance().addPoint(worldPoint);
					captialArea.addActivityMonster(monsterId, bornPoint.getId());
				}
			}
		}
		logger.info("refresh capital area activity monster");
	}
	
	/**
	 * 获取普通野怪刷新配置
	 * @param resAreaLevel
	 * @return
	 */
	public WorldMonsterRefreshCfg getActivityMonsterRefreshCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldMonsterRefreshCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldMonsterRefreshCfg.class);
		while(configIterator.hasNext()) {
			WorldMonsterRefreshCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldMonsterRefreshCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldMonsterRefreshCfg.class, size - 1);
	}
	
	/**
	 * 获取当前最大等级的普通野怪
	 * @return
	 */
	public int getMaxActivityMonsterLvl() {
		int maxLvl = 0;
		WorldMonsterRefreshCfg oldMonsterRefreshCfg = getActivityMonsterRefreshCfg();
		Map<Integer, Integer> commonRefresh = oldMonsterRefreshCfg.getRefreshActiCommon();
		for (int monsterId : commonRefresh.keySet()) {
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
			int monsterLvl = monsterCfg.getLevel();
			if (monsterLvl <= maxLvl) {
				continue;
			}
			maxLvl = monsterLvl;
		}
		return maxLvl;
	}
	
	/**
	 * 活动怪是否开启
	 * @return
	 */
	public boolean isActivityMonsterOpenOld() {
		boolean debugOpen = GameConstCfg.getInstance().isDebugControlWorldRefresh();
		if (debugOpen) {
			return GameConstCfg.getInstance().isOpenMonsterActivity();
		}
		
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MONSTER_2_VALUE);
		boolean isOpen = activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
		Optional<ActivityBase> activity2 = ActivityManager.getInstance().getGameActivityByType(ActivityType.YURI_ACHIEVE_TWO_VALUE);
		boolean isOpen2 = activity2.isPresent() && activity2.get().getActivityEntity().getActivityState() == ActivityState.OPEN;

		Optional<ActivityBase> activity3 = ActivityManager.getInstance().getGameActivityByType(ActivityType.RADIATION_WAR_VALUE);
		boolean isOpen3 = activity3.isPresent() && activity3.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
		return isOpen || isOpen2 || isOpen3;
	}
	
	/**
	 * 活动怪是否开启
	 * @return
	 */
	public boolean isActivityMonsterOpen185() {
		boolean debugOpen = GameConstCfg.getInstance().isDebugControlWorldRefresh();
		if (debugOpen) {
			return GameConstCfg.getInstance().isOpenMonsterActivity();
		}
		
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.RADIATION_WAR_TWO_VALUE);
		boolean isOpen = activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
		return isOpen;
	}
	
	
	/**
	 * 是否有怪物活动开启中
	 * @return
	 */
	public boolean isActivityMonsterOpen(){
		return this.isActivityMonsterOpenOld()
				|| this.isActivityMonsterOpen185();
	}
	
	
	
	public List<Integer> getMonsterIds(int monsterType) {
		List<Integer> monsterIds = new ArrayList<>();
		ConfigIterator<WorldEnemyCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldEnemyCfg.class);
		while(configIterator.hasNext()) {
			WorldEnemyCfg config = configIterator.next();
			if (config.getType() != monsterType) {
				continue;
			}
			monsterIds.add(config.getId());
		}
		return monsterIds;
	}
	
	/**
	 * 刷新区域野怪boss
	 * @param areaObject
	 */
	public void refreshAreaMonsterBoss(AreaObject areaObj) {
		long currentTime = HawkTime.getMillisecond();
		
		List<WorldPoint> remove = new ArrayList<>();
		
		Set<Integer> monsterBoss = areaObj.getMonsterBoss();
		for (Integer pointId : monsterBoss) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.MONSTER_VALUE) {
				continue;
			}
			WorldEnemyCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, worldPoint.getMonsterId());
			if (config.getType() != MonsterType.TYPE_3_VALUE) {
				continue;
			}
			int lifeTime = config.getLifeTime();
			if (currentTime - worldPoint.getLifeStartTime() < lifeTime * 1000L) {
				continue;
			}
			remove.add(worldPoint);
		}
		
		for (WorldPoint point : remove) {
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
			
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			area.removeMonsterBoss(point.getId());
		}
		
		WorldPointProxy.getInstance().batchDelete(remove);
	}
}
