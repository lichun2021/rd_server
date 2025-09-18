package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldResourceAreaCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.config.WorldResourceRefreshCfg;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventResourceCollectBegin;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CapitalAreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 世界资源
 * @author golden
 *
 */
public class WorldResourceService extends HawkAppObj{
	
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
	private static WorldResourceService instance = null;

	
	public static WorldResourceService getInstance() {
		return instance;
	}

	public WorldResourceService(HawkXID xid) {
		super(xid);
		instance = this;
		long currentTime = HawkTime.getMillisecond();
		lastTickTime = currentTime;
		lastRefreshTime = currentTime;
	}
	
	public boolean init(){
		List<WorldPoint> resourcePoints = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.RESOURCE);
		for (WorldPoint point : resourcePoints) {
			WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, point.getResourceId());
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
				captialArea.addResourcePoint(config.getResType(), point.getId());
			} else {
				AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
				area.addResourcePoint(config.getResType(), point.getId());
			}
		}
		return true;
	}
	
	public boolean init2() {
		try {
			refreshCapitalAreaResource();
			for (AreaObject area : WorldPointService.getInstance().getAreaVales()) {
				refreshAreaResource(area);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
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
		
		int resourceRefreshTime = WorldMapConstProperty.getInstance().getResourceRefreshTime();
		if (currentTime - lastRefreshTime > resourceRefreshTime * 1000L) {
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.RESOURCE_REFRESH) {
				@Override
				public boolean onInvoke() {
					lastRefreshTime = currentTime;
					notifyResourceRefresh();
					return true;
				}
			});
		}
		return true;
	}
	
	/**
	 * 通知区域刷新
	 */
	public void notifyResourceRefresh() {
		refreshCapitalAreaResource();
		refreshAreaCommonResource(1);
	}
	
	private void refreshAreaCommonResource(int areaId) {
		AreaObject area  = WorldPointService.getInstance().getArea(areaId);
		
		try {
			refreshAreaResource(area);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		// 延迟刷新下一个区域
		int areaSize = WorldPointService.getInstance().getAreaSize();
		if (areaId < areaSize) {
			long delay = GameConstCfg.getInstance().getResourceRefreshDelay();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.RESOURCE_REFRESH, delay, delay, 1) {
				@Override
				public boolean onInvoke() {
					refreshAreaCommonResource(areaId + 1);
					return true;
				}
			});
		}
	}
	
	private void refreshCapitalAreaResource() {
		long startTime = HawkTime.getMillisecond();
		
		// 删除过期资源点
		removeCapitalOverTimeResource();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		
		List<Point> validPoints = new ArrayList<>();
		for (int specialAreaId : WorldMapConstProperty.getInstance().getSpecialAreaIds()) {
			AreaObject area = WorldPointService.getInstance().getArea(specialAreaId);
			validPoints.addAll(area.getValidPoints(WorldPointType.RESOURCE, null, true, true));
		}
		
		if (validPoints == null || validPoints.isEmpty()) {
			return;
		}
		
		// 列表乱序
		Collections.shuffle(validPoints);
		
		// 需要刷新的资源
		WorldResourceRefreshCfg resourceRefreshCfg = getResourceRefreshCfg();
		Map<Integer, Integer> refreshResource = resourceRefreshCfg.getRefreshCapital();

		// 资源带对应资源等级配置
		WorldResourceAreaCfg resourceAreaCfg = getResourceAreaCfg();

		int validPointIndex = 0;
		for (Entry<Integer, Integer> refresh : refreshResource.entrySet()) {
			// 资源类型
			int resourceType = refresh.getKey();
			// 目标数量
			int targetCount = refresh.getValue();
			// 当前数量
			int currentCount = captialArea.getResourceNum(resourceType);

			// 移除多余的点
			if (currentCount > targetCount) {
				removeCapitalAreaResource(resourceType, currentCount - targetCount);
			}

			// 补全资源点
			if (currentCount < targetCount) {

				List<WorldPoint> addPoints = new ArrayList<>();

				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() - validPointIndex ? validPoints.size() - validPointIndex : refreshCount;

				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);

					// 资源等级
					int resourceLevel = resourceAreaCfg.randomResourceLevel(bornPoint.getZoneId());
					WorldResourceCfg resourceCfg = WorldUtil.getResourceCfg(resourceType, resourceLevel);

					// 创建世界点对象
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.RESOURCE_VALUE);
					worldPoint.setResourceId(resourceCfg.getId());
					
					int resNum = resourceCfg.getResNum();
					int effValue = 0;
					effValue += CrossSkillService.getInstance().getEffectValIfContinue(GsConfig.getInstance().getServerId(), EffType.CROSS_3012);
					resNum = (int)Math.ceil(resNum * (1 + (effValue * GsConst.EFF_PER )));
					worldPoint.setRemainResNum(resNum);
					
					worldPoint.setLifeStartTime(HawkTime.getMillisecond());
					addPoints.add(worldPoint);
					LogUtil.logWorldResourceRefrersh(resourceCfg.getId(), resourceCfg.getResType(), resourceCfg.getLevel(), worldPoint.getX(), worldPoint.getY(), worldPoint.getAreaId());
				}

				for (WorldPoint point : addPoints) {
					captialArea.addResourcePoint(resourceType, point.getId());
					WorldPointService.getInstance().addPoint(point);
				}

				WorldPointProxy.getInstance().batchCreate(addPoints);
			}
		}
		logger.info("refresh capital area resource, costtime:{}", HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 刷新区域资源
	 * @param area
	 */
	private void refreshAreaResource(AreaObject area) {
		long startTime = HawkTime.getMillisecond();
		
		// 删除过期资源点
		removeOverTimeResource(area);
		// 区域有效点列表
		List<Point> validPoints = area.getValidPoints(WorldPointType.RESOURCE, null, true, false);
		// 列表乱序
		Collections.shuffle(validPoints);
		// 需要刷新的资源
		WorldResourceRefreshCfg resourceRefreshCfg = getResourceRefreshCfg();
		Map<Integer, Integer> refreshResource = resourceRefreshCfg.getRefreshCommon();
		if (WorldMapConstProperty.getInstance().isSpecialAreaId(area.getId())) {
			refreshResource = resourceRefreshCfg.getRefreshSpecial();
		}
		
		// 资源带对应资源等级配置
		WorldResourceAreaCfg resourceAreaCfg = getResourceAreaCfg();
		
		int validPointIndex  = 0;
		for (Entry<Integer, Integer> refresh : refreshResource.entrySet()) {
			// 资源类型
			int resourceType = refresh.getKey();
			// 目标数量
			int targetCount = refresh.getValue();
			// 当前数量
			int currentCount = area.getResourceNum(resourceType);
			
			// 移除多余的点
			if (currentCount > targetCount) {
				removeAreaResource(area, resourceType, currentCount - targetCount);
			}
			// 补全资源点
			if (currentCount < targetCount) {
				
				List<WorldPoint> addPoints = new ArrayList<>();
				
				int refreshCount = targetCount - currentCount;
				refreshCount = refreshCount > validPoints.size() -  validPointIndex ? validPoints.size() -  validPointIndex : refreshCount;
				
				for (int i = 0; i < refreshCount; i++) {
					Point bornPoint = validPoints.get(validPointIndex++);
					
					// 资源等级
					int resourceLevel = resourceAreaCfg.randomResourceLevel(bornPoint.getZoneId());
					WorldResourceCfg resourceCfg = WorldUtil.getResourceCfg(resourceType, resourceLevel);
					
					// 创建世界点对象
					WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.RESOURCE_VALUE);
					worldPoint.setResourceId(resourceCfg.getId());
					
					int resNum = resourceCfg.getResNum();
					int effValue = 0;
					effValue += CrossSkillService.getInstance().getEffectValIfContinue(GsConfig.getInstance().getServerId(), EffType.CROSS_3012);
					resNum = (int)Math.ceil(resNum * (1 + (effValue * GsConst.EFF_PER )));
					
					worldPoint.setRemainResNum(resNum);
					worldPoint.setLifeStartTime(HawkTime.getMillisecond());
					addPoints.add(worldPoint);
					LogUtil.logWorldResourceRefrersh(resourceCfg.getId(), resourceCfg.getResType(), resourceCfg.getLevel(), worldPoint.getX(), worldPoint.getY(), worldPoint.getAreaId());
				}
				
				for (WorldPoint point : addPoints) {
					area.addResourcePoint(resourceType, point.getId());
					WorldPointService.getInstance().addPoint(point);
				}
				WorldPointProxy.getInstance().batchCreate(addPoints);
			}
		}
		
		logger.info("refresh area resource, areaId:{}, costtime:{}", area.getId(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 删除过期资源点
	 * @param area
	 * @param currentTime
	 */
	private void removeOverTimeResource(AreaObject area) {
		long currentTime = HawkTime.getMillisecond();
		
		List<WorldPoint> removePoints = new ArrayList<>();
		
		Map<Integer, Set<Integer>> resourcePoints = area.getResourcePoints();
		for (Set<Integer> pointIds : resourcePoints.values()) {
			for (Integer pointId : pointIds) {
				// 资源点
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
				// 点类型错误
				if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
					continue;
				}
				// 没到期
				WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
				if (currentTime - worldPoint.getLifeStartTime() < config.getLifeTime() * 1000L) {
					continue;
				}
				// 有人占领
				if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
					worldPoint.setLifeStartTime(currentTime);
					continue;
				}
				removePoints.add(worldPoint);
			}
		}
		
		for (WorldPoint wp : removePoints) {
			removeResourcePoint(wp, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removePoints);
	}
	
	/**
	 * 删除黑土地区域过期资源点
	 * @param area
	 * @param currentTime
	 */
	private void removeCapitalOverTimeResource() {
		long currentTime = HawkTime.getMillisecond();
		
		List<WorldPoint> removePoints = new ArrayList<>();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		Map<Integer, Set<Integer>> resourcePoints = captialArea.getResourcePoints();
		for (Set<Integer> pointIds : resourcePoints.values()) {
			for (Integer pointId : pointIds) {
				// 资源点
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
				// 点类型错误
				if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
					continue;
				}
				// 没到期
				WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
				if (currentTime - worldPoint.getLifeStartTime() < config.getLifeTime() * 1000L) {
					continue;
				}
				// 有人占领
				if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
					worldPoint.setLifeStartTime(currentTime);
					continue;
				}
				removePoints.add(worldPoint);
			}
		}
		
		for (WorldPoint wp : removePoints) {
			removeResourcePoint(wp, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removePoints);
	}
	
	/**
	 * 删除区域资源点
	 * @param area
	 * @param resourceType
	 * @param deleteNum
	 */
	private void removeAreaResource(AreaObject area, int resourceType, int deleteNum) {
		List<WorldPoint> removeList = new ArrayList<>();
		
		// 区域内野怪点
		Set<Integer> points = area.getResourcePoints(resourceType);
		for (int pointId : points) {
			if (removeList.size() >= deleteNum) {
				break;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint point : removeList) {
			removeResourcePoint(point, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removeList);
	}
	
	/**
	 * 删除黑土地区域资源点
	 * @param area
	 * @param resourceType
	 * @param deleteNum
	 */
	private void removeCapitalAreaResource(int resourceType, int deleteNum) {
		List<WorldPoint> removeList = new ArrayList<>();
		
		CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
		// 区域内野怪点
		Set<Integer> points = captialArea.getResourcePoints(resourceType);
		for (int pointId : points) {
			if (removeList.size() >= deleteNum) {
				break;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			// 点类型错误
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
				continue;
			}
			// 有人占领
			if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				continue;
			}
			removeList.add(worldPoint);
		}
		
		for (WorldPoint point : removeList) {
			removeResourcePoint(point, false);
		}
		
		WorldPointProxy.getInstance().batchDelete(removeList);
	}
	
	/**
	 * 通知资源被采集，采集结束会删除资源点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public long notifyResourceGather(WorldPoint worldPoint, long resNum) {
		try {
			if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
				return 0;
			}

			worldPoint.setRemainResNum(worldPoint.getRemainResNum() - resNum);
			//资源点资源数量低于一定值之后, 重新刷新资源点
			WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
			if (worldPoint.getRemainResNum() > cfg.getResRefreshNum()) {
				worldPoint.setPlayerId("");
				worldPoint.setPlayerName("");
				worldPoint.setPlayerIcon(0);
				worldPoint.setMarchId("");
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId()); // 通知场景本点数据更新
				return worldPoint.getRemainResNum();

			} else {
				WarFlagService.getInstance().addPointResource(worldPoint.getId(), cfg.getResType(), cfg.getResNum());
				removeResourcePoint(worldPoint, true); // 删除原有的资源点数据
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return 0;
	}
	
	/**
	 * 采集点被占领
	 * 
	 * @param player
	 * @param march
	 * @param x
	 * @param y
	 * @return
	 */
	public WorldPoint notifyResourcePointOccupy(Player player, IWorldMarch march, int x, int y) {
		// 目标点不存在
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(x, y);
		if (worldPoint == null) {
			return null;
		}
		// 点类型的判断
		if (worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
			return null;
		}

		if (worldPoint.getMarchId() != null && WorldMarchService.getInstance().getWorldMarch(worldPoint.getMarchId()) != null) {
			return worldPoint;
		}
		
		// 开始采集
		march.onMarchStop(WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE, march.getMarchEntity().getArmys(), worldPoint);

		// 初始化玩家和行军信息
		worldPoint.initPlayerInfo(player.getData());
		worldPoint.setMarchId(march.getMarchId());

		// 通知场景点的变化
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());

		// 任务刷新
		int resourceId = worldPoint.getResourceId();
		WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
		MissionManager.getInstance().postMsg(player, new EventResourceCollectBegin(resourceId, cfg.getLevel()));

		return worldPoint;
	}
	
	/**
	 * 生成指定资源点
	 * @param x
	 * @param y
	 * @param resourceId
	 * @return
	 */
	public org.hawk.result.Result<?> genResourcePoint(Player player, int x, int y, int resourceId) {
		int pointId = GameUtil.combineXAndY(x, y);

		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point != null) {
			// 点被占用
			return org.hawk.result.Result.fail(Status.Error.BUILDING_COORDINATE_USED_VALUE);
		}

		WorldResourceCfg resCfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, resourceId);
		if (resCfg == null) {
			return org.hawk.result.Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}

		AreaObject areaObj = WorldPointService.getInstance().getArea(x, y);
		Point bornPoint = areaObj.getFreePoint(x, y);
		if (bornPoint == null) {
			return org.hawk.result.Result.fail(Status.Error.BUILDING_COORDINATE_USED_VALUE);
		}

		// 创建世界点对象
		WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.RESOURCE_VALUE);
		worldPoint.setResourceId(resCfg.getId());
		
		int resNum = resCfg.getResNum();
		
		int effValue = 0;
		effValue += player.getData().getEffVal(EffType.SKILL_RESOURCE_NUM_UP);
		effValue += CrossSkillService.getInstance().getEffectValIfContinue(GsConfig.getInstance().getServerId(), EffType.CROSS_3012);
		resNum = (int)Math.ceil(resNum * (1 + (effValue * GsConst.EFF_PER )));
		worldPoint.setRemainResNum(resNum);
		
		worldPoint.setLifeStartTime(HawkTime.getMillisecond());
		
		int eff379Val = player.getData().getEffVal(EffType.SKILL_RESOURCE_COLLECT_SPEED_UP);
		EffectObject eff = new EffectObject(EffType.SKILL_RESOURCE_COLLECT_SPEED_UP_VALUE, eff379Val);
		worldPoint.setShowEffect(eff.toString());
		
		WorldPointProxy.getInstance().create(worldPoint);
		
		if (WorldPointService.getInstance().isInCapitalArea(bornPoint.getId())) {
			CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
			captialArea.addResourcePoint(resCfg.getResType(), pointId);
		} else {
			areaObj.addResourcePoint(resCfg.getResType(), pointId);
		}
		
		// 放入世界点列表信息中
		WorldPointService.getInstance().addPoint(worldPoint);
		return org.hawk.result.Result.success();
	}
	
	/**
	 * 获取资源刷新配置
	 * @return
	 */
	public WorldResourceRefreshCfg getResourceRefreshCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldResourceRefreshCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldResourceRefreshCfg.class);
		while(configIterator.hasNext()) {
			WorldResourceRefreshCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldResourceRefreshCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldResourceRefreshCfg.class, size - 1);
	}
	
	/**
	 * 获取资源带对应资源等级配置
	 * @return
	 */
	public WorldResourceAreaCfg getResourceAreaCfg() {
		int serverOpenSecond = (int)((HawkTime.getMillisecond() - GsApp.getInstance().getServerOpenTime()) / 1000);
		ConfigIterator<WorldResourceAreaCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldResourceAreaCfg.class);
		while(configIterator.hasNext()) {
			WorldResourceAreaCfg thisCfg = configIterator.next();
			if (serverOpenSecond >= thisCfg.getOpenServiceTimeLowerLimit() && serverOpenSecond < thisCfg.getOpenServiceTimeUpLimit()) {
				return thisCfg;
			}
		}
		
		// 没有符合条件的， 反向遍历。 找到最后一个符合resAreaLevel的。
		int size = HawkConfigManager.getInstance().getConfigSize(WorldResourceAreaCfg.class);
		return HawkConfigManager.getInstance().getConfigByIndex(WorldResourceAreaCfg.class, size - 1);
	}
	
	/**
	 * 删除资源点
	 * @param worldPoint
	 */
	public void removeResourcePoint(WorldPoint worldPoint, boolean deleteEntity) {
		WorldResourceCfg config = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
		boolean inCapitalArea = WorldPointService.getInstance().isInCapitalArea(worldPoint.getId());
		if (inCapitalArea) {
			CapitalAreaObject captialArea = WorldPointService.getInstance().getCaptialArea();
			captialArea.deleteResourcePoint(config.getResType(), worldPoint.getId());
		} else {
			AreaObject area = WorldPointService.getInstance().getArea(worldPoint.getAreaId());
			area.deleteResourcePoint(config.getResType(), worldPoint.getId());
		}
		WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), deleteEntity); 
	}
	
	/**
	 * 跨服战斗胜利方在黑土地刷新9级矿
	 */
	public void refreshCrossWinResource() {
		
		int specialResCount = 0;
		
		List<Point> validPoints = new ArrayList<>();
		for (int specialAreaId : WorldMapConstProperty.getInstance().getSpecialAreaIds()) {
			AreaObject area = WorldPointService.getInstance().getArea(specialAreaId);
			validPoints.addAll(area.getValidPoints(WorldPointType.RESOURCE, null, true, true));
			
			for (Point point : area.getUsedPoints()) {
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(point.getId());
				if (worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE) {
					continue;
				}
				WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class, worldPoint.getResourceId());
				if (cfg == null || cfg.getLevel() != CrossConstCfg.getInstance().getSpecialResourceLevel()) {
					continue;
				}
				specialResCount++;
			}
			
		}
		
		if (validPoints == null || validPoints.isEmpty()) {
			return;
		}
		
		// 列表乱序
		Collections.shuffle(validPoints);
		
		int validPointIndex = 0;
		
		int resCount = CrossConstCfg.getInstance().getSpecialResourceCount() - specialResCount;
		
		List<WorldPoint> addPoints = new ArrayList<>();
		
		for (int i = 0; i < resCount; i++) {
			Point bornPoint = validPoints.get(validPointIndex++);
			
			// 资源等级
			WorldResourceCfg resourceCfg = randomCrossWinResCfg();
			
			// 创建世界点对象
			WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.RESOURCE_VALUE);
			worldPoint.setResourceId(resourceCfg.getId());

			int resNum = resourceCfg.getResNum();
			int effValue = 0;
			effValue += CrossSkillService.getInstance().getEffectValIfContinue(GsConfig.getInstance().getServerId(), EffType.CROSS_3012);
			resNum = (int)Math.ceil(resNum * (1 + (effValue * GsConst.EFF_PER )));
			worldPoint.setRemainResNum(resNum);
			
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			addPoints.add(worldPoint);
			LogUtil.logWorldResourceRefrersh(resourceCfg.getId(), resourceCfg.getResType(), resourceCfg.getLevel(), worldPoint.getX(), worldPoint.getY(),worldPoint.getAreaId());
		}
		
		for (WorldPoint point : addPoints) {
			WorldPointService.getInstance().addPoint(point);
			WorldPointProxy.getInstance().batchCreate(addPoints);
		}
		
		logger.info("refresh crossWin resource success");		
	}
	
	/**
	 * 随机跨服战斗胜利方在黑土地刷新9级矿配置 
	 */
	public WorldResourceCfg randomCrossWinResCfg() {
		List<WorldResourceCfg> cfgs = new ArrayList<>();
		ConfigIterator<WorldResourceCfg> iter = HawkConfigManager.getInstance().getConfigIterator(WorldResourceCfg.class);
		while(iter.hasNext()) {
			WorldResourceCfg cfg = iter.next();
			if (cfg.getLevel() != CrossConstCfg.getInstance().getSpecialResourceLevel()) {
				continue;
			}
			cfgs.add(cfg);
		}
		
		if (cfgs.isEmpty()) {
			return null;
		}
		
		return cfgs.get(HawkRand.randInt(cfgs.size() - 1));
	}
}
