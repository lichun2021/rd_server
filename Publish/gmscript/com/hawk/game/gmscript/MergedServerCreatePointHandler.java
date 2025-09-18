package com.hawk.game.gmscript;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.nation.wearhouse.NationalDiamondRecord;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.service.MergeService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldTaskType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class MergedServerCreatePointHandler extends HawkScript {

	@Override
	public String action(Map<String, String> map, HawkScriptHttpInfo scriptInfo) {
		String type = map.get("type");
		String addRole = map.get("addRole");
		int num = map.get("num") == null ? 15000 : Integer.parseInt(map.get("num"));
		boolean addRoleFlag = HawkOSOperator.isEmptyString(addRole) ? false : Boolean.valueOf(addRole);
		try {
			if(type.equals("0")) {
				cleanAllPoint();
			} else {
				MergeService.createPoint(num, addRoleFlag);
			}
			
			mergeExtraData();
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(-1, "fail");
	}
	
	/**
	 * 删除所有的城点
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void cleanAllPoint() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		WorldPlayerService worldPlayerService = WorldPlayerService.getInstance();
		Field field = worldPlayerService.getClass().getDeclaredField("playerPos");
		field.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Map<String, Integer> map = (Map<String, Integer>) field.get(worldPlayerService);
		AtomicInteger counter = new AtomicInteger();
		long startTime = HawkTime.getMillisecond();
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(WorldTaskType.CLEAN_CITY) {
			@Override
			public boolean onInvoke() {
				WorldPointService.getInstance().removeWorldPoints(map.values(), true);
				int count = counter.incrementAndGet();
				if (count >= map.values().size()) {
					HawkLog.logPrintln("clean all point finish costTime:{}", HawkTime.getMillisecond() - startTime);
				}
				return false;
			}
		});
		HawkLog.logPrintln("clean world point :{}", map.values().size());
	}
	
	/**
	 *  除撒点外的其它处理
	 */
	public void mergeExtraData() {
		// 更新合服信息，给idip中转服（GM服）提供信息
		MergeService.updateMergeServerInfo();
		
		// 国家仓库，4项资源保留各服种最高的那个；国家金砖相加保留；国家仓库的捐献记录，主服从服合并，按时间排序；建筑等级再mergeserver中处理
		mergeNationWarehouseDonate();
		
		List<String> serverList = GlobalData.getInstance().getMergeServerList(GsConfig.getInstance().getServerId());
		nationTech(serverList);
		
		// 清空空的联盟entity数据
		MergeService.clearEmptyGuild();
	}
	
	/**
	 *  国家仓库捐献（4种资源、金砖、捐献记录）
	 */
	private void mergeNationWarehouseDonate() {
		// 获取主服和从服的区服ID
		List<String> serverList = GlobalData.getInstance().getSlaveServerList();
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_STOREHOUSE_DONATE) {
			@Override
			public boolean onInvoke() {
				mergeWarehouseResource(serverList);
				mergeWarehouseDonateRecord(serverList);
				return true;
			}
		});
	}
	
	/**
	 * 合并国家仓库中的资源数据
	 * @param slaveServer
	 */
	private void mergeWarehouseResource(List<String> slaveServerList) {
		String flagKey = "mergeWarehouseResource:"+ GsConfig.getInstance().getServerId();
		String flag = RedisProxy.getInstance().getRedisSession().getString(flagKey);
		if (!HawkOSOperator.isEmptyString(flag)) {
			return;
		}
		
		RedisProxy.getInstance().getRedisSession().setString(flagKey, "1", 300);
		List<Map<Integer, Long>> slaveResMapList = new ArrayList<>();
		try {
			Map<Integer,Long> mainResMap = NationService.getInstance().getNationalWarehouseResourse(GsConfig.getInstance().getServerId());
			for (String slaveServer : slaveServerList) {
				Map<Integer, Long> slaveResMap = NationService.getInstance().getNationalWarehouseResourse(slaveServer);
				slaveResMapList.add(slaveResMap);
			}
			Map<Integer, Long> mergeResMap = new HashMap<Integer, Long>();
			//对主库数据进行遍历更新
			for (Entry<Integer, Long> entry : mainResMap.entrySet()) {
				int resType = entry.getKey();
				long mainCount = entry.getValue();
				if (resType == PlayerAttr.DIAMOND_VALUE) {
					long totalCount = mainCount;
					for (Map<Integer, Long> slaveResMap : slaveResMapList) {
						totalCount += slaveResMap.getOrDefault(resType, 0L);
					}
					HawkLog.logPrintln("nation warehouse before merge, resId: {}, main resCount: {}, slave resCount: {}", resType, mainCount, totalCount - mainCount);
					mergeResMap.put(resType, totalCount);
				} else {
					long newCount = mainCount;
					for (Map<Integer, Long> slaveResMap : slaveResMapList) {
						newCount = Math.max(newCount, slaveResMap.getOrDefault(resType, 0L));
					}
					HawkLog.logPrintln("nation warehouse before merge, resId: {}, main resCount: {}, slave resCount: {}", resType, mainCount, newCount);
					mergeResMap.put(resType, newCount);
				}
			}
			
			for (Map<Integer, Long> slaveResMap : slaveResMapList) {
				for (Entry<Integer, Long> entry : slaveResMap.entrySet()) {
					int resType = entry.getKey();
					if (mainResMap.containsKey(resType)) {
						continue;
					}
					long oldMergeVal = mergeResMap.getOrDefault(resType, 0L);
					long slaveVal = entry.getValue();
					if (resType == PlayerAttr.DIAMOND_VALUE) {
						mergeResMap.put(resType, oldMergeVal + slaveVal);
					} else {
						mergeResMap.put(resType, Math.max(oldMergeVal, slaveVal));
					}
				}
			}
			
			final String finalRedisKey = NationService.getInstance().getDonateRedisKey(GsConfig.getInstance().getServerId());
			RedisProxy.getInstance().getRedisSession().del(finalRedisKey);
			for (Entry<Integer, Long> entry : mergeResMap.entrySet()) {
				long count = entry.getValue();
				int resourceItemId = entry.getKey();
				HawkLog.logPrintln("nation warehouse after merge, resId: {}, resCount: {}", entry.getKey(), count);
				if (count <= Integer.MAX_VALUE) {
					RedisProxy.getInstance().getRedisSession().hIncrBy(finalRedisKey, String.valueOf(resourceItemId), (int)count);
				} else {
					RedisProxy.getInstance().getRedisSession().hIncrBy(finalRedisKey, String.valueOf(resourceItemId), Integer.MAX_VALUE);
					RedisProxy.getInstance().getRedisSession().hIncrBy(finalRedisKey, String.valueOf(resourceItemId), (int) (count - Integer.MAX_VALUE));
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 合并国家仓库捐献记录数据
	 * 
	 * @param slaveServer
	 */
	private void mergeWarehouseDonateRecord(List<String> slaveServerList) {
		String flagKey = "mergeWarehouseDonateRecord:"+ GsConfig.getInstance().getServerId();
		String flag = RedisProxy.getInstance().getRedisSession().getString(flagKey);
		if (!HawkOSOperator.isEmptyString(flag)) {
			return;
		}
		
		RedisProxy.getInstance().getRedisSession().setString(flagKey, "1", 300);
		
		try {
			String mainDonateKey = NationService.getInstance().getDonateRecordRedisKey(GsConfig.getInstance().getServerId());
			List<String> mainRecords = RedisProxy.getInstance().getRedisSession().lRange(mainDonateKey, 0, -1, 0);
			for (String slaveServer : slaveServerList) {
				String slaveDonateKey = NationService.getInstance().getDonateRecordRedisKey(slaveServer);
				List<String> slaveRecords = RedisProxy.getInstance().getRedisSession().lRange(slaveDonateKey, 0, -1, 0);
				mainRecords.addAll(slaveRecords);
			}
			
			List<String> recordList = mainRecords.stream().map(record -> JSONObject.parseObject(record, NationalDiamondRecord.class))
					.sorted(new Comparator<NationalDiamondRecord>() {
						@Override
						public int compare(NationalDiamondRecord arg0, NationalDiamondRecord arg1) {
							return (int)(arg1.getTime() - arg0.getTime());
						}
					}).map(e -> JSONObject.toJSONString(e)).collect(Collectors.toList());
			
			RedisProxy.getInstance().getRedisSession().del(mainDonateKey);
			long size = RedisProxy.getInstance().getRedisSession().lPush(mainDonateKey, 0, recordList.toArray(new String[recordList.size()]));
			if (size > NationService.getInstance().getRecordLimit()) {
				RedisProxy.getInstance().getRedisSession().lTrim(mainDonateKey, 0, NationService.getInstance().getRecordLimit());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家科技
	 * @param serverIds
	 */
	private void nationTech(List<String> serverIds) {
		try {
			resetNationTech(serverIds);
			resetNationTechValue(serverIds);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 重置国家科技
	 * @param serverIds
	 */
	private void resetNationTech(List<String> serverIds) {
		try {
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
			
			Map<String, String> finalMap = new HashMap<>();
			
			for (String serverId : serverIds) {
				String key = RedisProxy.NATION_TECH + ":" + serverId;
				Map<String, String> techMap = redisSession.hGetAll(key);
				if (!MapUtils.isEmpty(techMap)) {
					for (Entry<String, String> tech : techMap.entrySet()) {
						if (!finalMap.containsKey(tech.getKey())) {
							finalMap.put(tech.getKey(), tech.getValue());
						} else {
							if (Integer.parseInt(finalMap.get(tech.getKey())) < Integer.parseInt(tech.getValue())) {
								finalMap.put(tech.getKey(), tech.getValue());
							}
						}
					}
				}
			}
			
			NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
			if (center != null) {
				center.resetTech(finalMap);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 重置国家科技值
	 * @param serverIds
	 */
	private void resetNationTechValue(List<String> serverIds) {
		try {
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
			
			int techValue = 0;
			for (String serverId : serverIds) {
				String key = RedisProxy.NATION_TECH_VALUE + ":" + serverId;
				String value = redisSession.getString(key);
				if (HawkOSOperator.isEmptyString(value)) {
					continue;
				}
				int serverTech = Integer.parseInt(value);
				if (serverTech > techValue) {
					techValue = serverTech;
				}
			}
			RedisProxy.getInstance().updateNationTechValue(techValue);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
