package com.hawk.game.world.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkTickable;
import org.hawk.timer.HawkTimerEntry;
import org.hawk.timer.HawkTimerListener;
import org.hawk.timer.HawkTimerManager;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class WorldPointRedisProxy extends WorldPointProxy {
	/**
	 * 上次刷新数据时间
	 */
	private long lastFlushTime = 0;
	/**
	 * 更新缓存
	 */
	private Map<Integer, WorldPoint> updateCache;
	/**
	 * 删除缓存
	 */
	private Map<Integer, WorldPoint> deleteCache;
	
	@Override
	public boolean init() {
		updateCache = new ConcurrentHashMap<Integer, WorldPoint>();
		deleteCache = new ConcurrentHashMap<Integer, WorldPoint>();
		
		// 世界点代理对象更新
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkTickable(){
			@Override
			public void onTick() {
				update();
			}
		});
			
		try {
			HawkTimerManager.getInstance().addAlarm("WorldPointKey", 0, 0, 4, -1, -1, new HawkTimerListener() {
				protected void handleAlarm(HawkTimerEntry entry) {
					Collection<AreaObject> areas = WorldPointService.getInstance().getAreaVales();
					for (AreaObject area : areas) {
						int expireSeconds = getExpireSeconds();
						LocalRedis.getInstance().getRedisSession().expire(getRedisKey(area.getId()), expireSeconds);
					}
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return super.init();
	}
	
	/**
	 * 加载所有世界点
	 */
	@Override
	public List<WorldPoint> loadAllPoints(Map<Integer, AreaObject> areas) {
		List<WorldPoint> worldPoints = new LinkedList<WorldPoint>();
		
		long startTime = HawkTime.getMillisecond();
		for (Entry<Integer, AreaObject> areaEntry : areas.entrySet()) {
			int areaId = areaEntry.getKey();
			
			// 加载一个区域的所有点
			Map<byte[], byte[]> areaPoints = LocalRedis.getInstance().getRedisSession().hGetAllBytes(getRedisKey(areaId).getBytes());
			if (areaPoints == null) {
				HawkLog.errPrintln("[WorldPoint] load from redis failed, areaId: {}", areaId);
				return null;
			}
			HawkLog.logPrintln("[WorldPoint] load area point, areaId: {}, count: {}", areaId, areaPoints.size());
			
			int count = 0;
			for (Entry<byte[], byte[]> pointEntry : areaPoints.entrySet()) {
				try {
					int pointId = Integer.parseInt(new String(pointEntry.getKey()));
					
					// 解析世界点数据
					PointData.Builder builder = PointData.newBuilder().mergeFrom(pointEntry.getValue());
					if (builder == null) {
						HawkLog.errPrintln("[WorldPoint] parse point data failed, areaId: {}, pointId: {}", areaId, pointId);
						continue;
					}
					WorldPoint worldPoint = WorldPointFactory.getInstance().createWorldPoint(builder.getPointType());
					if (worldPoint == null) {
						HawkLog.errPrintln("[WorldPoint] build world point failed, areaId: {}, pointId: {}", areaId, pointId);
						continue;
					}
					worldPoint.mergeFromPointData(builder);
					// 组装数据并添加到列表中
					try {
						worldPoint.afterRead();
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					
					worldPoints.add(worldPoint);
					if (worldPoint.getFoggyFortressCfg() != null) {
						count++;
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			HawkLog.logPrintln("WorldPointService load area points, areaId: {}, FoggyFortress point count: {}", areaId, count);
		}
	
		// 日志记录
		HawkLog.logPrintln("[WorldPoint] world point load from redis success, count: {}, costtime: {}", 
				worldPoints.size(), HawkTime.getMillisecond() - startTime);
	
		return worldPoints;
	}
	
	/**
	 * 创建世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public boolean create(WorldPoint worldPoint) {
		if (worldPoint.isInvalid()) {
			return false;
		}
		
		// 设置时间状态
		if (worldPoint.getCreateTime() <= 0) {
			worldPoint.setCreateTime(HawkTime.getMillisecond());
		}

		// 写入数据
		boolean result = updateWorldPoint(worldPoint, getExpireSeconds());
		if (!result) {
			updateCache.putIfAbsent(worldPoint.getId(), worldPoint);
		}
		
		return true;
	}
	
	/**
	 * 批量创建世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	@Override
	public boolean batchCreate(List<WorldPoint> worldPoints) {
		if (worldPoints.size() <= 0) {
			return true;
		}
		
		// 设置时间状态
		for (WorldPoint worldPoint : worldPoints) {
			if (worldPoint.getCreateTime() <= 0) {
				worldPoint.setCreateTime(HawkTime.getMillisecond());
			}
		}
		
		return batchUpdateWorldPoint(worldPoints, getExpireSeconds());
	}
	
	/**
	 * 更新世界点
	 */
	@Override
	public boolean update(WorldPoint worldPoint) {
		if (!worldPoint.isInvalid()) {
			updateCache.putIfAbsent(worldPoint.getId(), worldPoint);
		}
		return true;
	}

	/**
	 * 删除世界点
	 */
	@Override
	public void delete(WorldPoint worldPoint) {
		// 先设置无效
		worldPoint.setInvalid(true);
		
		// 存储到redis
		int areaId = worldPoint.getAreaId();
		String pointId = String.valueOf(worldPoint.getId());
		
		// redis删除
		long result = LocalRedis.getInstance().getRedisSession().hDelBytes(getRedisKey(areaId), pointId.getBytes());
		if (result < 0) {
			deleteCache.putIfAbsent(worldPoint.getId(), worldPoint);
		}
	}
	
	/**
	 * 批量删除世界点
	 * 
	 * @param worldPoints
	 * @return
	 */
	@Override
	public boolean batchDelete(List<WorldPoint> worldPoints) {
		if (worldPoints.size() <= 0) {
			return true;
		}
		
		// 分区域分组删除
		Map<Integer, List<byte[]>> areaFields = new HashMap<Integer, List<byte[]>>();
		for (WorldPoint worldPoint : worldPoints) {
			// 先设置无效
			worldPoint.setInvalid(true);
			
			List<byte[]> fields = areaFields.get(worldPoint.getAreaId());
			if (fields == null) {
				fields = new ArrayList<byte[]>(worldPoints.size());
				areaFields.put(worldPoint.getAreaId(), fields);
			}
			
			String pointId = String.valueOf(worldPoint.getId());
			fields.add(pointId.getBytes());
		}
		
		// 按区域删除世界点
		for (Entry<Integer, List<byte[]>> entry : areaFields.entrySet()) {
			int areaId = entry.getKey();
			List<byte[]> fields = entry.getValue();
			
			// redis删除操作
			long result = LocalRedis.getInstance().getRedisSession().hDelBytes(getRedisKey(areaId), fields.toArray(new byte[0][0]));
			
			// 操作失败时添加到失败队列中, 下次处理
			if (result < 0) {
				for (WorldPoint worldPoint : worldPoints) {
					if (worldPoint.getAreaId() == areaId) {
						deleteCache.putIfAbsent(worldPoint.getId(), worldPoint);
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 更新世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	private boolean updateWorldPoint(WorldPoint worldPoint, int expireSeconds) {
		// 已被删除的情况
		if (worldPoint.isInvalid()) {
			return true;
		}
		
		// 创建前操作
		try {
			worldPoint.beforeWrite();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 设置时间状态
		worldPoint.setUpdateTime(HawkTime.getMillisecond());		
		
		// 存储到redis
		int areaId = worldPoint.getAreaId();
		String pointId = String.valueOf(worldPoint.getId());
		
		// 构建数据
		PointData.Builder builder = worldPoint.buildPointData();
		byte[] bytes = builder.build().toByteArray();
		
		// 真正写入redis
		return LocalRedis.getInstance().getRedisSession().hSetBytes(getRedisKey(areaId), pointId, bytes, expireSeconds) >= 0;
	}
	
	/**
	 * 批量更新世界点
	 * 
	 * @param worldPoint
	 * @return
	 */
	private boolean batchUpdateWorldPoint(List<WorldPoint> worldPoints, int expireSeconds) {
		if (worldPoints.size() <= 0) {
			return true;
		}
		
		// 分区域分组删除
		Map<Integer, Map<byte[], byte[]>> areaFields = new HashMap<Integer, Map<byte[], byte[]>>();
		for (WorldPoint worldPoint : worldPoints) {
			if (worldPoint.isInvalid()) {
				continue;
			}
			// 创建前操作
			try {
				worldPoint.beforeWrite();
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			// 设置时间状态
			worldPoint.setUpdateTime(HawkTime.getMillisecond());	
			
			// 构建存储数据
			PointData.Builder builder = worldPoint.buildPointData();
			if (builder == null) {
				continue;
			}
			
			Map<byte[], byte[]> fieldMap = areaFields.get(worldPoint.getAreaId());
			if (fieldMap == null) {
				fieldMap = new HashMap<byte[], byte[]>();
				areaFields.put(worldPoint.getAreaId(), fieldMap);
			}
			
			String pointId = String.valueOf(worldPoint.getId());
			byte[] bytes = builder.build().toByteArray();
			fieldMap.put(pointId.getBytes(), bytes);
		}
		
		// 按区批量写入数据
		for (Entry<Integer, Map<byte[], byte[]>> entry : areaFields.entrySet()) {
			int areaId = entry.getKey();
			Map<byte[], byte[]> fieldMap = entry.getValue();
			
			// redis创建操作
			boolean result = LocalRedis.getInstance().getRedisSession().hmSetBytes(getRedisKey(areaId), fieldMap, expireSeconds);
			
			// 创建失败处理, 缓存起来后面再写入
			if (!result) {
				for (WorldPoint worldPoint : worldPoints) {
					if (worldPoint.getAreaId() == areaId) {
						updateCache.putIfAbsent(worldPoint.getId(), worldPoint);
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 获取存储key
	 * 
	 * @param areaId
	 * @return
	 */
	private String getRedisKey(int areaId) {
		String key = String.format("%s:%s:wp:%d", GsConfig.getInstance().getServerId(), 
				GsApp.getInstance().getServerIdentify(), areaId);
		
		return key;
	}
	
	/**
	 * 获取失效时间
	 * 
	 * @return
	 */
	private int getExpireSeconds() {
		return 7 * 24 * 3600;
	}
	
	
	/**
	 * 帧更新
	 */
	public void update() {
		long currTime = HawkTime.getMillisecond(); 
		if (currTime - lastFlushTime >= GsConfig.getInstance().getPointFlushPeriod()) {
			lastFlushTime = currTime;
			flush();
		}
	}
	
	/**
	 * 刷新缓存数据到redis中
	 */
	@Override
	public void flush() {
		try {
			String serverIdentify = RedisProxy.getInstance().getServerIdentify(GsConfig.getInstance().getServerId());
			if (!HawkOSOperator.isEmptyString(serverIdentify)) {
				flushUpdateCache();
				flushDeleteCache();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 写出更新数据
	 */
	private synchronized void flushUpdateCache() {
		if (updateCache == null || updateCache.isEmpty()) {
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		
		// 把删除缓存取出来进行批量操作
		List<WorldPoint> worldPoints = new ArrayList<WorldPoint>(updateCache.size());
		worldPoints.addAll(updateCache.values());
		updateCache.clear();
		
		// 批量更新
		batchUpdateWorldPoint(worldPoints, 0);
		
		HawkLog.logPrintln("world point flush update cache, count: {}, costtime: {}", 
				worldPoints.size(), HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 缓存中待删除数据处理
	 */
	private synchronized void flushDeleteCache() {
		if (deleteCache == null || deleteCache.isEmpty()) {
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		
		// 把删除缓存取出来进行批量操作
		List<WorldPoint> worldPoints = new ArrayList<WorldPoint>(deleteCache.size());
		worldPoints.addAll(deleteCache.values());
		deleteCache.clear();
		
		// 批量删除
		batchDelete(worldPoints);
		
		HawkLog.logPrintln("world point flush delete cache, count: {}, costtime: {}", 
				worldPoints.size(), HawkTime.getMillisecond() - startTime);
	}
}

