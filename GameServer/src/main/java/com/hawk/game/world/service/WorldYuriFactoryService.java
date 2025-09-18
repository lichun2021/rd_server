package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsConfig;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.object.YuriFactoryPoint;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 尤里兵工厂全局服务
 * 
 * @author zhenyu.shang
 * @since 2017年9月18日
 */
public class WorldYuriFactoryService extends HawkAppObj {

	private static Logger logger = LoggerFactory.getLogger("Server");

	private static WorldYuriFactoryService instance = null;

	/**
	 * 所有尤里点映射表
	 */
	private Map<Integer, YuriFactoryPoint> factoryPoint;

	/**
	 * 清理点时候的临时变量
	 */
	private Map<Integer, List<Integer>> tempPointMap;

	public static WorldYuriFactoryService getInstance() {
		return instance;
	}

	public WorldYuriFactoryService(HawkXID xid) {
		super(xid);
		instance = this;
		factoryPoint = new ConcurrentHashMap<Integer, YuriFactoryPoint>();
		tempPointMap = new HashMap<Integer, List<Integer>>();
	}

	public boolean init() {
		
		HawkLog.logPrintln("world yuri service init start");
		
		List<WorldPoint> yuriFactories = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.YURI_FACTORY);
		if (yuriFactories.isEmpty()) {
			createYuriFactoriesPoints();
		} else {
			long now = HawkTime.getMillisecond();
			for (WorldPoint worldPoint : yuriFactories) {
				// 检测过期点
				if (now > worldPoint.getLifeStartTime()) {
					if (tempPointMap.containsKey(worldPoint.getAreaId())) {
						tempPointMap.get(worldPoint.getAreaId()).add(worldPoint.getId());
					} else {
						List<Integer> list = new ArrayList<Integer>();
						list.add(worldPoint.getId());
						tempPointMap.put(worldPoint.getAreaId(), list);
					}
					continue;
				}
				factoryPoint.put(worldPoint.getId(), new YuriFactoryPoint(worldPoint));
			}

			// 清理并重生世界点
			clearAndBornPoint();
		}

		// 注册yuri的更新周期, 以秒为单位, 算生命周期
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(1000, 1000) {
			@Override
			public void onPeriodTick() {
				long beginTimeMs = HawkTime.getMillisecond();
				try {
					updateYuriPoint();
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					// 时间消耗的统计信息
					long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
					if (costTimeMs > GsConfig.getInstance().getProtoTimeout()) {
						logger.warn("process updateYuriPoint tick too much time, costtime: {}", costTimeMs);
					}
				}
			}
		});
		
		HawkLog.logPrintln("world yuri service init end");
		
		return true;
	}

	/**
	 * 尤里矿场的心跳
	 */
	public void updateYuriPoint() {
		long now = HawkApp.getInstance().getCurrentTime();
		for (YuriFactoryPoint yuri : factoryPoint.values()) {
			// 判断尤里生命周期是否已经到了, 并且这个点没有在尤里复仇活动中, 到了就直接删除，并处理自身逻辑
			if (now <= yuri.getLifeStartTime()) {
				yuri.heartbeat();
				continue;
			}
			
			// 判断当前是否在尤里复仇活动中, 如果在则重置生命周期
			if (yuri.isInActive()) {
				yuri.resetLifeStartTime();
				continue;
			}

			// 世界点加入表，统一清理
			if (tempPointMap.containsKey(yuri.getWorldPoint().getAreaId())) {
				tempPointMap.get(yuri.getWorldPoint().getAreaId()).add(yuri.getWorldPoint().getId());
			} else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(yuri.getWorldPoint().getId());
				tempPointMap.put(yuri.getWorldPoint().getAreaId(), list);
			}
			
			// 映射点直接删除
			factoryPoint.remove(yuri.getId());
		}
		
		// 清理并重生世界点
		clearAndBornPoint();
	}

	/**
	 * 清理并重生世界点
	 */
	private void clearAndBornPoint() {
		if (tempPointMap.isEmpty()) {
			return;
		}

		for (Entry<Integer, List<Integer>> entry : tempPointMap.entrySet()) {
			// 清理所有点
			WorldPointService.getInstance().removeWorldPoints(entry.getValue(), true);
			AreaObject areaObject = WorldPointService.getInstance().getArea(entry.getKey());
			if (areaObject != null) {
				// 重生此区域的点
				bornYuriOnArea(areaObject, entry.getValue().size());
			}
		}
		
		tempPointMap.clear();
	}

	/**
	 * 初次创建世界所有怪点
	 * 
	 * @return
	 */
	private void createYuriFactoriesPoints() {
		// 按区域生成
		logger.info("init area yuri points...");
		for (AreaObject areaObj : WorldPointService.getInstance().getAreaVales()) {
			long startTime = HawkTime.getMillisecond();
			logger.info("start create area yuri points, areaId: {}", areaObj.getId());
			// 找出刷新的点
			int yuriCount = areaObj.getTotalPointCount() * WorldMapConstProperty.getInstance().getWorldYuriRefreshMax() / 1000 / GsConst.POINT_TO_GRUD;
			// 生成
			this.bornYuriOnArea(areaObj, yuriCount);
			// 记录信息
			logger.info("create area yuri points finish, areaId: {}, pointCount: {}, costtime: {}", areaObj.getId(), yuriCount, HawkTime.getMillisecond() - startTime);
		}
	}

	/**
	 * 在指定区域内出生N个怪
	 * 
	 * @param areaObj
	 * @return
	 */
	private void bornYuriOnArea(AreaObject areaObj, int count) {
		List<WorldPoint> bornYuriList = new ArrayList<WorldPoint>(count);
		// 不需要重生 || 没有世界点的区域 || (不是普通野怪 && 不是超级野怪)
		if (count <= 0) {
			return;
		}
		long startTime = HawkTime.getMillisecond();
		// 找出刷新怪物的点
		List<Point> pointList = areaObj.getValidPoints(WorldPointType.YURI_FACTORY_VALUE, null);
		if (pointList == null || pointList.size() <= 0) {
			logger.error("born yuri on area failed, areaId: {}, freePointSize: {}", areaObj.getId(), pointList.size());
			return;
		}
		// 乱序
		Collections.shuffle(pointList);
		int addCount = 0;
		// 生成所需要的点
		for (int i = 0; i < pointList.size(); i++) {
			Point bornPoint = pointList.get(i);
			// 检查是否能被占用
			if (!WorldPointService.getInstance().tryOccupied(areaObj, bornPoint, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}
			WorldPoint worldPoint = new WorldPoint(bornPoint.getX(), bornPoint.getY(), bornPoint.getAreaId(), bornPoint.getZoneId(), WorldPointType.YURI_FACTORY_VALUE);
			long lifeTime = WorldMapConstProperty.getInstance().getYuriLifeTime();
			long lifeStartTime = HawkApp.getInstance().getCurrentTime() + lifeTime;
			worldPoint.setLifeStartTime(lifeStartTime);

			// 将点加入到世界
			WorldPointService.getInstance().addPoint(worldPoint);
			// 将点加入到尤里工程
			factoryPoint.put(worldPoint.getId(), new YuriFactoryPoint(worldPoint));

			bornYuriList.add(worldPoint);
			logger.info("yuri born, pos: ({}, {}), areaId:{}, lifeTime:{}", bornPoint.getX(), bornPoint.getY(), areaObj.getId(), lifeTime);
			addCount++;
			if (addCount >= count) {
				break;
			}
		}
		// 入库
		WorldPointProxy.getInstance().batchCreate(bornYuriList);
		logger.info("born yuri on area, count: {}, pointSize: {}, costtime: {}", count, pointList.size(), HawkTime.getMillisecond() - startTime);
	}

	/**
	 * 获取尤里矿点
	 * 
	 * @param pointId
	 * @return
	 */
	public YuriFactoryPoint getYuriFactoryPoint(int pointId) {
		return factoryPoint.get(pointId);
	}

}
