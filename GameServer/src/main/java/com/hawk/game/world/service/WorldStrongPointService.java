 package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OccupyStrongpointFinishEvent;
import com.hawk.game.GsApp;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldStrongpointAreaCfg;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.config.WorldStrongpointRefreshCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.item.AwardItems;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.World.StrongpointStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventOccupyStrongpointTime;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 据点
 * @author golden
 *
 */
public class WorldStrongPointService extends HawkAppObj {
	
	public static Logger logger = LoggerFactory.getLogger("Server");

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
	 * 实例
	 */
	private static WorldStrongPointService instance = null;

	
	public static WorldStrongPointService getInstance() {
		return instance;
	}

	public WorldStrongPointService(HawkXID xid) {
		super(xid);
		instance = this;
		long currentTime = HawkTime.getMillisecond();
		lastTickTime = currentTime;
		lastRefreshTime = currentTime;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.STRONG_POINT);
		for (WorldPoint point : points) {
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
				captialArea.addStrongpointPoint(point.getId());
			} else {
				AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
				area.addStrongpointPoint(point.getId());
			}
		}
		
		if (isActivityOpen()) {
			refreshCapitalAreaStrongpoint();
			for (AreaObject area : WorldPointService.getInstance().getAreaVales()) {
				refreshAreaStrongpoint(area);
			}
		} else {
			removeCapitalAreaStrongpoint();
			for (AreaObject area : WorldPointService.getInstance().getAreaVales()) {
				removeAreaStrongpoint(area);
			}
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
		
		int refreshTime = WorldMapConstProperty.getInstance().getStrongpointRefreshTime();
		if (currentTime - lastRefreshTime > refreshTime * 1000L) {
			lastRefreshTime = currentTime;
			notifyStrongpointRefresh();
		}
		return true;
	}
	
	/**
	 * 获取据点刷新配置
	 * @return
	 */
	public WorldStrongpointRefreshCfg getStrongpointRefreshCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldStrongpointRefreshCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldStrongpointRefreshCfg.class);
		while(configIterator.hasNext()) {
			WorldStrongpointRefreshCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldStrongpointRefreshCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldStrongpointRefreshCfg.class, size - 1);
	}
	
	/**
	 * 获取资源带对应据点等级配置
	 * @return
	 */
	public WorldStrongpointAreaCfg getStrongpointAreaCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldStrongpointAreaCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldStrongpointAreaCfg.class);
		while(configIterator.hasNext()) {
			WorldStrongpointAreaCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldStrongpointAreaCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldStrongpointAreaCfg.class, size - 1);
	}
	
	/**
	 * 获取据点配置
	 * @param level
	 * @return
	 */
	public static WorldStrongpointCfg getStrongpointCfg(int level) {
		ConfigIterator<WorldStrongpointCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldStrongpointCfg.class);
		while(configIterator.hasNext()) {
			WorldStrongpointCfg thisCfg = configIterator.next();
			if (thisCfg.getLevel() == level) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldStrongpointCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldStrongpointCfg.class, size - 1);
		
	}

	/**
	 * 据点活动是否开启
	 * @return
	 */
	public boolean isActivityOpen() {
		boolean debugOpen = GameConstCfg.getInstance().isDebugControlWorldRefresh();
		if (debugOpen) {
			return GameConstCfg.getInstance().isOpenStrongpointActivity();
		}
		
		return true;
	}
	
	/**
	 * 通知据点刷新
	 */
	public void notifyStrongpointRefresh() {
		if (isActivityOpen()) {
			refreshCapitalAreaStrongpoint();
			refreshAreaStrongpoint(1);
		} else {
			removeCapitalAreaStrongpoint();
			removeAreaStrongpoint(1);
		}
	}
	
	/**
	 * 刷新黑土地区域据点
	 */
	private void refreshCapitalAreaStrongpoint() {
		long startTime = HawkTime.getMillisecond();

		// 删除过期资源点
		removeCapitalOverTimeStrongpoint();

		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();

		List<Point> validPoints = new ArrayList<>();
		for (int specialAreaId : WorldMapConstProperty.getInstance().getSpecialAreaIds()) {
			AreaObject area = WorldPointService.getInstance().getArea(specialAreaId);
			validPoints.addAll(area.getValidPoints(WorldPointType.STRONG_POINT, null, true, true));
		}

		if (validPoints == null || validPoints.isEmpty()) {
			return;
		}

		// 列表乱序
		Collections.shuffle(validPoints);

		// 需要刷新的资源
		WorldStrongpointRefreshCfg refreshCfg = getStrongpointRefreshCfg();
		
		// 目标数量
		int targetCount = refreshCfg.getCapitalNum();

		// 当前数量
		int currentCount = captialArea.getStrongpointNum();
		
		// 移除多余的点
		if (currentCount > targetCount) {
			removeCapitalAreaStrongpoint(currentCount - targetCount);
		}
		
		// 补全据点
		if (currentCount < targetCount) {
			
			List<WorldPoint> addPoints = new ArrayList<>();
			
			int refreshCount = targetCount - currentCount;
			refreshCount = refreshCount > validPoints.size() ? validPoints.size(): refreshCount;
			
			WorldStrongpointAreaCfg strongpointAreaCfg = getStrongpointAreaCfg();
			
			for (int i = 0; i < refreshCount; i++) {
				Point bornPoint = validPoints.get(i);
				
				// 据点配置
				int strongpointLevel = strongpointAreaCfg.randomResourceLevel(bornPoint.getZoneId());
				WorldStrongpointCfg strongpointCfg = getStrongpointCfg(strongpointLevel);
				
				WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.STRONG_POINT_VALUE);
				worldPoint.setMonsterId(strongpointCfg.getId());
				worldPoint.setLastActiveTime(strongpointCfg.getTickNum() * strongpointCfg.getTickTime() * 1000);
				worldPoint.setLifeStartTime(startTime);
				worldPoint.setPointStatus(StrongpointStatus.SP_INIT_VALUE);
				addPoints.add(worldPoint);
			}
			
			for (WorldPoint point : addPoints) {
				captialArea.addStrongpointPoint(point.getId());
				WorldPointService.getInstance().addPoint(point);
			}
			
			WorldPointProxy.getInstance().batchCreate(addPoints);
		}
		logger.info("refresh capital area strongpoint, costtime:{}", HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除黑土地区域过期资源点
	 * @param area
	 * @param currentTime
	 */
	private void removeCapitalOverTimeStrongpoint() {
		long currentTime = HawkTime.getMillisecond();
		
		List<WorldPoint> removePoints = new ArrayList<>();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		Set<Integer> srtongpoints = captialArea.getStrongpointPoints();
		for (Integer pointId : srtongpoints) {
			// 资源点
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
				continue;
			}
			// 据点配置
			WorldStrongpointCfg strongpointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, worldPoint.getMonsterId());

			if (currentTime - worldPoint.getLifeStartTime() < strongpointCfg.getLifeTime() * 1000L) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				worldPoint.setLifeStartTime(currentTime);
				continue;
			}
			removePoints.add(worldPoint);
		}
		
		for (WorldPoint wp : removePoints) {
			removeStrongpoint(wp, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removePoints);
	}
	
	/**
	 * 删除黑土地区域资源点
	 * @param area
	 * @param resourceType
	 * @param deleteNum
	 */
	private void removeCapitalAreaStrongpoint(int deleteNum) {
		List<WorldPoint> removeList = new ArrayList<>();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		// 区域内野怪点
		Set<Integer> points = captialArea.getStrongpointPoints();
		for (int pointId : points) {
			if (removeList.size() >= deleteNum) {
				break;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint point : removeList) {
			removeStrongpoint(point, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removeList);
	}
	
	/**
	 * 刷新区域据点
	 */
	private void refreshAreaStrongpoint(int areaId) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		
		try {
			refreshAreaStrongpoint(area);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getStrongpointRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.STRONGPOINT_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					refreshAreaStrongpoint(areaId + 1);
					return true;
				}
			});
		}
	}
	
	/**
	 * 刷新区域据点
	 * @param area
	 */
	private void refreshAreaStrongpoint(AreaObject area) {
		long startTime = HawkTime.getMillisecond();
		
		// 删除过期资源点
		removeOverTimeStrongpoint(area);
		// 区域有效点列表
		List<Point> validPoints = area.getValidPoints(WorldPointType.STRONG_POINT, null, true, false);
		// 列表乱序
		Collections.shuffle(validPoints);
		
		// 需要刷新的据点
		WorldStrongpointRefreshCfg refreshCfg = getStrongpointRefreshCfg();
		
		int targetCount = refreshCfg.getCommonNum();
		if (WorldMapConstProperty.getInstance().isSpecialAreaId(area.getId())) {
			targetCount = refreshCfg.getSpecialNum();
		}
		
		// 当前数量
		int currentCount = area.getStrongpointNum();

		// 移除多余的点
		if (currentCount > targetCount) {
			removeAreaStrongpoint(area, currentCount - targetCount);
		}

		// 补全据点
		if (currentCount < targetCount) {

			List<WorldPoint> addPoints = new ArrayList<>();

			int refreshCount = targetCount - currentCount;
			refreshCount = refreshCount > validPoints.size() ? validPoints.size() : refreshCount;

			WorldStrongpointAreaCfg strongpointAreaCfg = getStrongpointAreaCfg();
			
			for (int i = 0; i < refreshCount; i++) {
				Point bornPoint = validPoints.get(i);
				
				// 据点配置
				int strongpointLevel = strongpointAreaCfg.randomResourceLevel(bornPoint.getZoneId());
				WorldStrongpointCfg strongpointCfg = getStrongpointCfg(strongpointLevel);

				WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.STRONG_POINT_VALUE);
				worldPoint.setMonsterId(strongpointCfg.getId());
				worldPoint.setLastActiveTime(strongpointCfg.getTickNum() * strongpointCfg.getTickTime() * 1000);
				worldPoint.setLifeStartTime(startTime);
				worldPoint.setPointStatus(StrongpointStatus.SP_INIT_VALUE);
				addPoints.add(worldPoint);
			}

			for (WorldPoint point : addPoints) {
				area.addStrongpointPoint(point.getId());
				WorldPointService.getInstance().addPoint(point);
			}

			WorldPointProxy.getInstance().batchCreate(addPoints);
		}
		
		logger.info("refresh area strongpoint, areaId:{}, costtime:{}", area.getId(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除过期据点
	 * @param area
	 * @param currentTime
	 */
	private void removeOverTimeStrongpoint(AreaObject area) {
		long currentTime = HawkTime.getMillisecond();
		
		List<WorldPoint> removePoints = new ArrayList<>();
		
		Set<Integer> strongpoints = area.getStrongpointPoints();
		for (Integer pointId : strongpoints) {
			// 资源点
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
				continue;
			}
			// 据点配置
			WorldStrongpointCfg strongpointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, worldPoint.getMonsterId());
			if (currentTime - worldPoint.getLifeStartTime() < strongpointCfg.getLifeTime() * 1000L) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				worldPoint.setLifeStartTime(currentTime);
				continue;
			}
			removePoints.add(worldPoint);
		}
		
		for (WorldPoint wp : removePoints) {
			removeStrongpoint(wp, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removePoints);
	}
	
	/**
	 * 删除区域资源点
	 * @param area
	 * @param resourceType
	 * @param deleteNum
	 */
	private void removeAreaStrongpoint(AreaObject area, int deleteNum) {
		List<WorldPoint> removeList = new ArrayList<>();
		
		// 区域内野怪点
		Set<Integer> points = area.getStrongpointPoints();
		for (int pointId : points) {
			if (removeList.size() >= deleteNum) {
				break;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint point : removeList) {
			removeStrongpoint(point, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removeList);
	}
	
	/**
	 * 删除区域据点
	 * @param areaObj
	 */
	public void removeCapitalAreaStrongpoint() {
		long startTime = HawkTime.getMillisecond();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		List<WorldPoint> removeList = new ArrayList<>();
		
		Set<Integer> strongpoints = captialArea.getStrongpointPoints();
		for (Integer pointId : strongpoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint remove : removeList) {
			removeStrongpoint(remove, false);
		}
		
		if (!removeList.isEmpty()) {
			WorldPointProxy.getInstance().batchDelete(removeList);
		}
		logger.info("remove captial area strongpoint, costTime:{}", HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除区域据点
	 * @param areaId
	 */
	public void removeAreaStrongpoint(int areaId) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		removeAreaStrongpoint(area);
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getStrongpointRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.STRONGPOINT_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					removeAreaStrongpoint(areaId + 1);
					return true;
				}
			});
		}
	}
	
	/**
	 * 删除区域据点
	 * @param areaObj
	 */
	public void removeAreaStrongpoint(AreaObject areaObj) {
		long startTime = HawkTime.getMillisecond();
		List<WorldPoint> removeList = new ArrayList<>();
		
		Set<Integer> strongpoints = areaObj.getStrongpointPoints();
		for (Integer pointId : strongpoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint remove : removeList) {
			removeStrongpoint(remove, false);
		}
		if (!removeList.isEmpty()) {
			WorldPointProxy.getInstance().batchDelete(removeList);
		}
		logger.info("remove area strongpoint, costTime:{}", HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除据点
	 * @param worldPoint
	 */
	public void removeStrongpoint(WorldPoint worldPoint, boolean deleteEntity) {
		boolean inCapitalArea = WorldPointService.getInstance().isInCapitalArea(worldPoint.getId());
		if (inCapitalArea) {
			CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
			captialArea.deleteStrongpointPoint(worldPoint.getId());
		} else {
			AreaObject area = WorldPointService.getInstance().getArea(worldPoint.getAreaId());
			area.deleteStrongpointPoint(worldPoint.getId());
		}
		WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), deleteEntity); 
	}
	
	/**
	 * 奖励结算 & 行军返回 & 点状态更新
	 * @param march
	 * @param backArmys
	 * @param autoReturn 是否是tick时间到，自动返回
	 * @return 结算后点被移除则返回true
	 */
	public boolean doStrongpointReturn(IWorldMarch march, List<ArmyInfo> backArmys, boolean autoReturn) {
		try {
			if (march.isReturnBackMarch()) {
				return false;
			}
			
			if (march.isMarchState()) {
				WorldMarchService.getInstance().onMarchReturn(march, backArmys, 0);
				return true;
			}
			
			logger.info("strongpoint march, doStrongpointReturn, marchId:{}, autoReturn:{}", march.getMarchId(), autoReturn);
			
			// 据点
			int pointId = march.getMarchEntity().getTerminalId();
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			
			// 据点配置
			WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, point.getMonsterId());
			
			// 本次tick时间 = 当前时间 - 开始驻扎时间
			long thisTickTime = HawkTime.getMillisecond() - march.getMarchEntity().getResStartTime();
			thisTickTime = thisTickTime > point.getLastActiveTime() ? point.getLastActiveTime() : thisTickTime;
			
			// 本次tick次数 = 据点目前总的tick次数 - 据点之前已经tick的次数
			int beforeTickTimes = getHasTickTimes(strongPointCfg, getTotalTickTime(strongPointCfg) - point.getLastActiveTime());
			int totalTickTimes = getHasTickTimes(strongPointCfg, (getTotalTickTime(strongPointCfg) - point.getLastActiveTime() + thisTickTime));
			int thisTickTimes = totalTickTimes - beforeTickTimes; 
			
			// 还有多余的兵， 则计算奖励
			if (WorldUtil.getFreeArmyCnt(backArmys) > 0) {
				// 填充奖励
				AwardItems award = AwardItems.valueOf();
				for (int i = 0; i < thisTickTimes; i++) {
					award.addAwards(strongPointCfg.getFixedAwards());
					award.addAwards(strongPointCfg.getRandomAwards());
				}
				
				march.getMarchEntity().setAwardItems(award);
				march.getMarchEntity().setResEndTime(HawkTime.getMillisecond());
			}
			
			// 行军返回
			WorldMarchService.getInstance().onMarchReturn(march, backArmys, 0);
			OccupyStrongpointFinishEvent event = new OccupyStrongpointFinishEvent(march.getPlayerId(), strongPointCfg.getLevel(), (int)(thisTickTime/1000));
			ActivityManager.getInstance().postEvent(event);
			//跨服消息投递-占领据点
			CrossActivityService.getInstance().postEvent(event);
			MissionManager.getInstance().postMsg(march.getPlayerId(), new EventOccupyStrongpointTime(strongPointCfg.getLevel(), (int)(thisTickTime/1000)));
			// 更新点数据
			if (autoReturn || point.getLastActiveTime() - thisTickTime <= 0) {
				removeStrongpoint(point, true);
				return true;
			} else {
				point.setLastActiveTime((long) (point.getLastActiveTime() - thisTickTime));
				point.setPlayerId("");
				point.setPlayerName("");
				point.setPlayerIcon(0);
				point.setMarchId("");
				point.setPointStatus(StrongpointStatus.SP_EMPTY_VALUE);
				
				// 点行军数据刷新
				Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
				for (IWorldMarch pointMarch : worldPointMarchs) {
					if(pointMarch.isReturnBackMarch() || pointMarch.getMarchId().equals(march.getMarchId())) {
						continue;
					}
					// 更新行军信息(更新行军线颜色)
					pointMarch.updateMarch();
					// 删除联盟战争显示
					WorldMarchService.getInstance().rmGuildMarch(pointMarch.getMarchId());
				}
				
				// 通知场景点数据更新
				WorldPointService.getInstance().getWorldScene().update(point.getAoiObjId());
				return false;
			}
		} catch (Exception e) {
			logger.error("strongpoint march, doStrongpointReturn, marchId:{}, autoReturn:{}, backArmys:{}", march.getMarchId(), autoReturn, backArmys.toString());
			WorldMarchService.getInstance().onMarchReturn(march, backArmys, 0);
			HawkException.catchException(e);
			return false;
		}
	}
	
	/**
	 * 总tick时间
	 * @return
	 */
	public long getTotalTickTime(WorldStrongpointCfg strongPointCfg) {
		return strongPointCfg.getTickTime() * strongPointCfg.getTickNum() * 1000L;
	}
	
	/**
	 * 根据已经tick的时间计算已经tick次数
	 * @param hasTickTime
	 * @return
	 */
	public int getHasTickTimes(WorldStrongpointCfg strongPointCfg, long hasTickTime) {
		return (int)(hasTickTime / (strongPointCfg.getTickTime() * 1000L));
	}
}
