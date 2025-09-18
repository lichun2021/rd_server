package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldPylonCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxDelete;
import com.hawk.game.protocol.CrossActivity.CrossFightPeriod;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 能量塔
 * @author golden
 *
 */
public class WorldPylonService extends HawkAppObj {
	
	/**
	 * 生成能量塔随机次数
	 */
	public static final int RANDTIMES = 100;
	
	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 单例
	 */
	private static WorldPylonService instance;
	
	
	/**
	 * 检测移除时间
	 */
	public long checkRemoveTime;
	
	/**
	 * 世界上资源列表
	 */
	public Map<Integer, WorldPoint> pylons;
	
	
	private long resourceSpreeBoxCheckTime;
	
	/**
	 * 构造
	 */
	public WorldPylonService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 获取单例
	 */
	public static WorldPylonService getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 */
	public boolean init() {
		pylons = new ConcurrentHashMap<>();
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.PYLON);
		for (WorldPoint point : points) {
			pylons.put(point.getId(), point);
			logger.info("initPylon, x:{}, y:{}, pylonId:{}", point.getX(), point.getY(), point.getResourceId());
		}
		
		return true;
	}
	
	@Override
	public boolean onTick() {
		// 检测能量塔移除
		checkPylon();
		//刷新资源狂欢宝箱
		checkResourceSpreeBoxRemove();
		return true;
	}
	
	/**
	 * 世界上能量塔数量
	 */
	public int currentPylonCount() {
		return pylons.size();
	}

	
	/**
	 * 是否需要刷新能量塔
	 * @param curTime
	 * @param lastTickTime
	 * @return
	 */
	public boolean needRefeshPylon(long curTime,long lastTickTime){
		if (CrossActivityService.getInstance().getTermId() <= 0) {
			return false;
		}
		List<Integer> points =  WorldMapConstProperty.getInstance().getPylonRefreshTimePoints();
		if(Objects.isNull(points)){
			return false;
		}
		if(points.size() <= 0){
			return false;
		}
		long scoreStart = CrossActivityService.getInstance().getActivityInfo().getOpenTime();
		long fightPrepareTime = scoreStart + CrossConstCfg.getInstance().getFightPrepareTime();
		if(curTime >= fightPrepareTime){
			return false;
		}
		for(int tp : points){
			long refreshTime = scoreStart + tp * 1000;
			if(curTime >= refreshTime && lastTickTime < refreshTime){
				return true;
			}
		}
		return false;
	}
	
	
	
	
	/**
	 * 刷新能量塔
	 */
	public void refreshPylon(long curTime,long lastTickTime) {
		
		
		if(!this.needRefeshPylon(curTime, lastTickTime)){
			return;
		}
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.RESOURCE_PYLON) {
			@Override
			public boolean onInvoke() {
				// 当前已经达到最大值，不需要刷
				int maxCount = WorldMapConstProperty.getInstance().getPylonRefreshCount();
				if (currentPylonCount() >= maxCount) {
					logger.info("refreshPylon, current:{}, refreshCount:{}", currentPylonCount(), maxCount);
					return true;
				}
				
				int refreshCount = maxCount - currentPylonCount();
				
				// 生成点列表
				List<WorldPoint> createPoints = new ArrayList<>();
				
				for (int i = 0; i < refreshCount; i++) {
					
					try {
						
						WorldPoint point = createPylonPoint();
						if (point == null) {
							continue;
						}
						
						createPoints.add(point);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
				
				WorldPointProxy.getInstance().batchCreate(createPoints);
				logger.info("refreshPylon, refresh:{}, current:{}", refreshCount, currentPylonCount());
				
				LogUtil.logCrossActivityPylonRefresh(currentPylonCount() - refreshCount, currentPylonCount());
				return true;
			}
		});
	}
	
	/**
	 * 生成能量塔世界点
	 */
	public WorldPoint createPylonPoint() {
		
		// 生成能量塔起始终止区域
		int[] pylonAreaBegin = WorldMapConstProperty.getInstance().getPylonAreaBegin();
		int[] pylonAreaEnd = WorldMapConstProperty.getInstance().getPylonAreaEnd();
		
		int randTimes = 0;
		
		while(true) {
			
			randTimes++;
			
			if (randTimes > RANDTIMES) {
				break;
			}
			
			int randX = HawkRand.randInt(pylonAreaBegin[0], pylonAreaEnd[0]);
			int randY = HawkRand.randInt(pylonAreaBegin[1], pylonAreaEnd[1]);
			
			Point point = WorldPointService.getInstance().getAreaPoint(randX, randY, true);
			if (point == null) {
				continue;
			}
			
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
			
			WorldPylonCfg pylonCfg = HawkConfigManager.getInstance().getConfigByIndex(WorldPylonCfg.class, 0);
			if (pylonCfg == null) {
				break;
			}
			
			int resId = HawkConfigManager.getInstance().getConfigByIndex(WorldPylonCfg.class, 0).getId();
			
			// 创建世界点对象
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.PYLON_VALUE);
			worldPoint.setResourceId(pylonCfg.getId());
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			WorldPointService.getInstance().addPoint(worldPoint);
			
			pylons.put(worldPoint.getId(), worldPoint);
			
			logger.info("createPylon, x:{}, y:{}, areaId:{}, resId:{}", point.getX(), point.getY(), point.getAreaId(), resId);
			
			return worldPoint;
		}
		
		return null;
	}
	
	/**
	 * 检测能量塔移除
	 */
	public void checkPylon() {
		if (HawkTime.getMillisecond() - checkRemoveTime < 10000) {
			return;
		}
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}
		if(!WorldThreadScheduler.getInstance().isRunning()){
			return;
		}
		long lastCheckTime = this.checkRemoveTime;
		checkRemoveTime = HawkTime.getMillisecond();
		//删除到期的能量塔
		this.removePylon();
		// 刷新能量塔
		this.refreshPylon(checkRemoveTime,lastCheckTime);
	}
	
	
	public void removePylon(){
		if(this.pylons.size() <= 0){
			return;
		}
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.RESOURCE_PYLON) {
			@Override
			public boolean onInvoke() {
				// 移除过期能量塔
				List<WorldPoint> rmPylons = new ArrayList<>();
				long removeTime = 0;
				if (CrossActivityService.getInstance().getTermId() > 0) {
					long scoreStart = CrossActivityService.getInstance().getActivityInfo().getOpenTime();
					removeTime = scoreStart + CrossConstCfg.getInstance().getFightPrepareTime();
				}
				long curTime = HawkTime.getMillisecond();
				for (WorldPoint pylon : pylons.values()) {
					// 有人占领，不删除
					if (!HawkOSOperator.isEmptyString(pylon.getPlayerId())) {
						continue;
					}
					
					WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, pylon.getResourceId());
					if (cfg == null) {
						rmPylons.add(pylon);
						continue;
					}
					if(curTime > removeTime){
						rmPylons.add(pylon);
						continue;
					}
					if (curTime - pylon.getLifeStartTime() < cfg.getLifeTime() * 1000L) {
						continue;
					}
					rmPylons.add(pylon);
				}

				for (WorldPoint point : rmPylons) {
					notifyPylonRemove(point.getId(), null, null);
					WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
				}
				WorldPointProxy.getInstance().batchDelete(rmPylons);
				return true;
			}
		});
	}
	
	/**
	 * 通知能量塔移除
	 */
	public void notifyPylonRemove(int pointId, String playerId, String marchId) {
		pylons.remove(pointId);
		
		int[] pos = GameUtil.splitXAndY(pointId);
		logger.info("notifyPylonRemove, posX:{}, posY:{}, playerId:{}, marchId:{}, currentCount:{}", pos[0], pos[1], playerId, marchId, pylons.size());
	}
	
	
	
	
	private void checkResourceSpreeBoxRemove(){
		long curTime = HawkTime.getMillisecond();
		if(this.resourceSpreeBoxCheckTime <= 0){
			this.resourceSpreeBoxCheckTime = curTime;
			return;
		}
		if(curTime - this.resourceSpreeBoxCheckTime < HawkTime.MINUTE_MILLI_SECONDS * 10){
			return;
		}
		this.resourceSpreeBoxCheckTime = curTime;
		if(CrossActivityService.getInstance().isOpen()){
			return;
		}
		List<WorldPoint> box = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.RESOURCE_SPREE_BOX);
		if(box.size() <= 0){
			return;
		}
		logger.info("CrossActivityService checkResourceSpreeBoxRemove...");
		ResourceSpreeBoxDelete delete = new ResourceSpreeBoxDelete();
		WorldThreadScheduler.getInstance().postDelayWorldTask(delete);
		
	}
}
