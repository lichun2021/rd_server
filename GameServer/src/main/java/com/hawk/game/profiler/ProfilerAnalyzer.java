package com.hawk.game.profiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.LoginStatis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.service.QQScoreBatch;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelog.GameLog;
import com.hawk.zoninesdk.ZonineSDK;

/**
 * 性能分析
 * 
 * @author hawk
 *
 */
public class ProfilerAnalyzer {
	/**
	 * 上一次分析的时间
	 */
	private long lastAnalyzeTime;
	/**
	 * 上一次分析时的redis操作次数
	 */
	private long redisOpCount;
	/**
	 * redis分析信息
	 */
	private JSONObject redisAnalyzeInfo;
	/**
	 * 在线数据分析
	 */
	private JSONObject onlineDataInfo;
	/**
	 * 全服运营数据分析
	 */
	private JSONObject globalDataInfo;
	/**
	 * 单服运营数据分析
	 */
	private JSONObject serverDataInfo;
	/**
	 * 推送线程分析
	 */
	private JSONObject pushThreadInfo;
	
	/**
	 * 世界线程分析
	 */
	private JSONObject worldThreadInfo;
	
	/**
	 * 世界线程任务分析
	 */
	private JSONObject worldThreadTaskInfo;
	
	private long localRedisOpCount = 0;
	
	private long globalRedisOpCount = 0;
	/**
	 * 上一次分析时tlog总条数
	 */
	private long tlogCountHistory = 0;
	/**
	 * 上一次分析时tlog总大小
	 */
	private long tlogSizeHistory = 0;

	/**
	 * 上一次上报主播信息的时间 
	 */
	private String lastAnchorTime;
	
	/**
	 * redis key操作次数信息<key, count>
	 */
	Map<String, Integer> redisOperCntMap = new HashMap<String, Integer>();
	
	private JSONObject redisOperCntInfo = new JSONObject();
	
	/**
	 * 实例对象
	 */
	private static ProfilerAnalyzer instance;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static ProfilerAnalyzer getInstance() {
		if (instance == null) {
			instance = new ProfilerAnalyzer();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		redisAnalyzeInfo = new JSONObject();
		onlineDataInfo = new JSONObject();
		globalDataInfo = new JSONObject();
		serverDataInfo = new JSONObject();
		pushThreadInfo = new JSONObject();
		worldThreadInfo = new JSONObject();
		worldThreadTaskInfo = new JSONObject();
	}

	/**
	 * 开启一次分析
	 */
	public void doAnalyze() {
		redisAnalyze();
		serverStatusAnalyze();
	}
	
	/**
	 * redis分析
	 */
	private void redisAnalyze() {
		long now = HawkApp.getInstance().getCurrentTime();
		long localRedis = LocalRedis.getInstance().getRedisOpCount();
		long globalRedis = RedisProxy.getInstance().getRedisOpCount();
		String time = HawkTime.formatNowTime();
		if(lastAnalyzeTime > 0) {
			double redisQPS = (localRedis + globalRedis - redisOpCount) * 1000 * 1.0d / (now - lastAnalyzeTime);
			redisAnalyzeInfo.clear();
			redisAnalyzeInfo.put("ts", time);
			redisAnalyzeInfo.put("本地redis操作增量", localRedis - localRedisOpCount);
			redisAnalyzeInfo.put("全局redis操作增量", globalRedis - globalRedisOpCount);
			redisAnalyzeInfo.put("QPS", (int) redisQPS);
			HawkApp.getInstance().analyzerWrite("redisQPS", redisAnalyzeInfo.toJSONString(), "list");
		}
		
		redisOpCount = localRedis + globalRedis;
		localRedisOpCount = localRedis;
		globalRedisOpCount = globalRedis;
		lastAnalyzeTime = now;
		
		Map<String, Integer> redisKeyOpCntMap = StatisManager.getInstance().getRedisKeyStatis();
		redisOperCntInfo.clear();
		for (Entry<String, Integer> entry : redisKeyOpCntMap.entrySet()) {
			Integer oldVal = redisOperCntMap.get(entry.getKey());
			redisOperCntInfo.put(entry.getKey(), entry.getValue() - (oldVal == null ? 0 : oldVal));
		}
		
		redisOperCntInfo.put("ts", time);
		HawkApp.getInstance().analyzerWrite("redisOpDetail", redisOperCntInfo.toJSONString(), "list");
		
		redisOperCntMap = redisKeyOpCntMap;
	}
	
	/**
	 * 应用状态数据分析
	 * 
	 * @param data
	 */
	private void serverStatusAnalyze() {
		try {
			onlineDataInfo.clear();
			globalDataInfo.clear();
			serverDataInfo.clear();
			worldThreadInfo.clear();
			worldThreadTaskInfo.clear();
			
			String time = HawkTime.formatNowTime();
			int onlineCount = GlobalData.getInstance().getOnlineUserCount();
			RedisProxy.getInstance().addOnlineUserCount(onlineCount);
			
			String dayTime = HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
			Map<String, String> serverDailyInfoMap = RedisProxy.getInstance().getServerDailyInfoMap(dayTime);
			onlineDataInfo.put("ts", time);
			onlineDataInfo.put("在线人数", onlineCount);
			onlineDataInfo.put("活跃玩家数", serverDailyInfoMap.containsKey(DailyInfoField.DAY_LOGIN) ? serverDailyInfoMap.get(DailyInfoField.DAY_LOGIN) : 0);
			onlineDataInfo.put("总注册人数", GlobalData.getInstance().getRegisterCount());
			onlineDataInfo.put("dataCache缓存数", GlobalData.getInstance().getCacheCount());
			onlineDataInfo.put("playerCache缓存数", GsApp.getInstance().getObjMan(GsConst.ObjType.PLAYER).getObjCount());
			onlineDataInfo.put("行军数量", WorldMarchService.getInstance().getMarchsSize());
			onlineDataInfo.put("平均登录时长", LoginStatis.getInstance().getAvgLoginTime());
			onlineDataInfo.put("平均组装时长", LoginStatis.getInstance().getAvgAssembleTime());
			long countNow = GameLog.getInstance().getTlogCount();
			long sizeNow = GameLog.getInstance().getTlogSize();
			long countAdd = countNow - tlogCountHistory;
			long sizeAdd = sizeNow - tlogSizeHistory;
			tlogCountHistory = countNow;
			tlogSizeHistory = sizeNow;
			onlineDataInfo.put("tlog上报条数", countAdd);
			onlineDataInfo.put("tlog上报字节数(KB)", (long)(sizeAdd / 1024D));
			HawkApp.getInstance().analyzerWrite("appStatus", onlineDataInfo.toJSONString(), "list");
			
			// 单服日新增、全服日新增、单服总量、全服总量
			serverDataInfo.put("ts", time);
			serverDataInfo.put("本服日活跃", serverDailyInfoMap.containsKey(DailyInfoField.DAY_LOGIN) ? serverDailyInfoMap.get(DailyInfoField.DAY_LOGIN) : 0);
			serverDataInfo.put("本服日新增", serverDailyInfoMap.containsKey(DailyInfoField.DAY_REGISTER) ? serverDailyInfoMap.get(DailyInfoField.DAY_REGISTER) : 0);
			serverDataInfo.put("本服日付费用户", serverDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? serverDailyInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER) : 0);
			serverDataInfo.put("本服日付费钻石", serverDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? serverDailyInfoMap.get(DailyInfoField.DAY_RECHARGE) : 0);
			serverDataInfo.put("本服日付费月卡", serverDailyInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? serverDailyInfoMap.get(DailyInfoField.DAY_MONTHCARD) : 0);
			serverDataInfo.put("本服日付费直购", serverDailyInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? serverDailyInfoMap.get(DailyInfoField.DAY_PAYITEM) : 0);
			
			Map<String, String> serverTotalInfoMap = RedisProxy.getInstance().getServerTotalInfoMap();
			serverDataInfo.put("本服总注册", GlobalData.getInstance().getRegisterCount());
			serverDataInfo.put("本服总付费用户", serverTotalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? serverTotalInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER) : 0);
			serverDataInfo.put("本服总付费钻石", serverTotalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? serverTotalInfoMap.get(DailyInfoField.DAY_RECHARGE) : 0);
			serverDataInfo.put("本服总付费月卡", serverTotalInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? serverTotalInfoMap.get(DailyInfoField.DAY_MONTHCARD) : 0);
			serverDataInfo.put("本服总付费直购", serverTotalInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? serverTotalInfoMap.get(DailyInfoField.DAY_PAYITEM) : 0);
			HawkApp.getInstance().analyzerWrite("serverData", serverDataInfo.toJSONString(), "list");
			
			globalDataInfo.put("ts", time);
			Map<String, String> totalDailyInfoMap = RedisProxy.getInstance().getGlobalDailyInfoMap(dayTime);
			globalDataInfo.put("全服日活跃", totalDailyInfoMap.containsKey(DailyInfoField.DAY_LOGIN) ? totalDailyInfoMap.get(DailyInfoField.DAY_LOGIN) : 0);
			globalDataInfo.put("全服日新增", totalDailyInfoMap.containsKey(DailyInfoField.DAY_REGISTER) ? totalDailyInfoMap.get(DailyInfoField.DAY_REGISTER) : 0);
			globalDataInfo.put("全服日付费用户数", totalDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? totalDailyInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER) : 0);
			globalDataInfo.put("全服日付费钻石", totalDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? totalDailyInfoMap.get(DailyInfoField.DAY_RECHARGE) : 0);
			globalDataInfo.put("全服日付费月卡", totalDailyInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? totalDailyInfoMap.get(DailyInfoField.DAY_MONTHCARD) : 0);
			globalDataInfo.put("全服日付费直购", totalDailyInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? totalDailyInfoMap.get(DailyInfoField.DAY_PAYITEM) : 0);
			
			Map<String, String> totalInfoMap = RedisProxy.getInstance().getGlobalStatInfoMap();
			globalDataInfo.put("全服总注册数", totalInfoMap.containsKey(DailyInfoField.DAY_REGISTER) ? totalInfoMap.get(DailyInfoField.DAY_REGISTER) : 0);
			globalDataInfo.put("全服总付费用户数", totalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? totalInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER) : 0);
			globalDataInfo.put("全服总付费钻石", totalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? totalInfoMap.get(DailyInfoField.DAY_RECHARGE) : 0);
			globalDataInfo.put("全服总付费月卡", totalInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? totalInfoMap.get(DailyInfoField.DAY_MONTHCARD) : 0);
			globalDataInfo.put("全服总付费直购", totalInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? totalInfoMap.get(DailyInfoField.DAY_PAYITEM) : 0);
			RedisProxy.getInstance().writeMonitorInfo(0, "list", "globalData", globalDataInfo.toJSONString());
			
			if (GsConfig.getInstance().isPushEnable()) {
				pushThreadInfo.put("ts", time);
				pushThreadInfo.put("手Q积分上报任务堆压", QQScoreBatch.getInstance().getWaitCount());
				HawkApp.getInstance().analyzerWrite("pushAmassTask", pushThreadInfo.toJSONString(), "list");
			}
			
			worldThreadInfo.put("ts", time);
			worldThreadInfo.put("世界线程队列任务加入数量", WorldThreadScheduler.getInstance().getPushTaskCnt());
			worldThreadInfo.put("世界线程队列任务取出数量", WorldThreadScheduler.getInstance().getPopTaskCnt());
			worldThreadInfo.put("世界线程队列任务堆压数量", WorldThreadScheduler.getInstance().getAmassTaskCnt());
			HawkApp.getInstance().analyzerWrite("worldAmassTask", worldThreadInfo.toJSONString(), "list");
						
			worldThreadTaskInfo.put("ts", time);
			worldThreadTaskInfo.put("玩家登陆初始化城点", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.MOVE_CITY));
			worldThreadTaskInfo.put("同步城防信息", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.SYNC_CITY_DEF));
			worldThreadTaskInfo.put("玩家主动迁城", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.PLAYER_MOVE_CITY));
			worldThreadTaskInfo.put("放置联盟堡垒", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.CREATE_GUILD_MANOR));
			worldThreadTaskInfo.put("放置联盟建筑", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.CREATE_GUILD_BUILD));
			worldThreadTaskInfo.put("移除联盟建筑", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.REMOVE_GUILD_BUILD));
			worldThreadTaskInfo.put("使用技能", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.USE_SKILL));
			worldThreadTaskInfo.put("活动开启", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.ACTIVITY_OPEN));
			worldThreadTaskInfo.put("活动结束", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.ACTIVITY_CLOSE));
			worldThreadTaskInfo.put("重置账号时移除世界点", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.ACCOUNT_RESET_REMOVE_CITY));
			worldThreadTaskInfo.put("清理城点", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.CLEAN_CITY));
			worldThreadTaskInfo.put("生成怪物点", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.WORLD_MONSTER_POINT_GENERATE));
			worldThreadTaskInfo.put("清除领地资源点", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.CLEAR_RESOURCE));
			worldThreadTaskInfo.put("清除领地资源点次数重置", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.RESET_CLEAR_RESOURCE_NUM));
			worldThreadTaskInfo.put("世界行军加速", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.WORLD_MARCH_SPEED));
			worldThreadTaskInfo.put("迁出玩家", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.MIGRATE_OUT_PLAYER));
			worldThreadTaskInfo.put("行军强制回城", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.MARCH_BACK_FORCED));
			worldThreadTaskInfo.put("行军召回", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.WORLD_MARCH_CALLBACK));
			worldThreadTaskInfo.put("行军点召回", WorldThreadScheduler.getInstance().getPushTaskCnt("WorldTask-" + GsConst.WorldTaskType.WORLD_MARCH_POINT_CALLBACK));
			HawkApp.getInstance().analyzerWrite("worldTaskPushCount", worldThreadTaskInfo.toJSONString(), "list");
			
			sendZonineData(time, onlineCount, serverDailyInfoMap, serverTotalInfoMap);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 向zonine发送特殊监控数据
	 * 
	 * @param time
	 * @param onlineCount
	 * @param serverDailyInfoMap
	 * @param serverTotalInfoMap
	 */
	private void sendZonineData(String time, int onlineCount, Map<String, String> serverDailyInfoMap, Map<String, String> serverTotalInfoMap) {
		if (!GsConfig.getInstance().isZonineEnable()) {
			return;
		}
		
		JSONObject globalOnlineData = new JSONObject();
		globalOnlineData.put("ts", time);
		JSONArray globalOnlineDataArray = new JSONArray();
		globalOnlineDataArray.add(buildFieldData("在线人数", "onlineData", onlineCount));
		globalOnlineData.put("data", globalOnlineDataArray);
		ZonineSDK.getInstance().globalMonitorReport("global_online", globalOnlineData.toJSONString());
		
		JSONObject globalData = new JSONObject();
		globalData.put("ts", time);
		JSONArray array = new JSONArray();
		int count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_LOGIN) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_LOGIN)) : 0;
		array.add(buildFieldData("全服日活跃", "dailyActive", count));
	
		count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_REGISTER) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_REGISTER)) : 0;
		array.add(buildFieldData("全服日新增", "dailyRegister", count));
		
		count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER)) : 0;
		array.add(buildFieldData("全服日付费用户数", "dailyPayRole", count));
		
		count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_RECHARGE)) : 0;
		array.add(buildFieldData("全服日付费钻石", "dailyPayDiamonds", count));
		
		count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_MONTHCARD)) : 0;
		array.add(buildFieldData("全服日付费月卡", "dailyPayCard", count));
		
		count = serverDailyInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? Integer.parseInt(serverDailyInfoMap.get(DailyInfoField.DAY_PAYITEM)) : 0;
		array.add(buildFieldData("全服日付费直购", "dailyPayItem", count));
		
		count = GlobalData.getInstance().getRegisterCount();
		array.add(buildFieldData("全服总注册数", "totalRegister", count));
		
		count = serverTotalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE_PLAYER) ? Integer.parseInt(serverTotalInfoMap.get(DailyInfoField.DAY_RECHARGE_PLAYER)) : 0;
		array.add(buildFieldData("全服总付费用户数", "totalPayRole", count));
		
		count = serverTotalInfoMap.containsKey(DailyInfoField.DAY_RECHARGE) ? Integer.parseInt(serverTotalInfoMap.get(DailyInfoField.DAY_RECHARGE)) : 0;
		array.add(buildFieldData("全服总付费钻石", "totalPayDiamonds", count));
		
		count = serverTotalInfoMap.containsKey(DailyInfoField.DAY_MONTHCARD) ? Integer.parseInt(serverTotalInfoMap.get(DailyInfoField.DAY_MONTHCARD)) : 0;
		array.add(buildFieldData("全服总付费月卡", "totalPayCard", count));
		
		count = serverTotalInfoMap.containsKey(DailyInfoField.DAY_PAYITEM) ? Integer.parseInt(serverTotalInfoMap.get(DailyInfoField.DAY_PAYITEM)) : 0;
		array.add(buildFieldData("全服总付费直购", "totalPayItems", count));
		
		globalData.put("data", array);
		ZonineSDK.getInstance().globalMonitorReport("globalData", globalData.toJSONString());
		
		try {
			String key = "monitor:anchorInfo";
			List<String> anchorList = RedisProxy.getInstance().getRedisSession().lRange(key, 0, 0, 0);
			if (!anchorList.isEmpty()) {
				String anchorInfoStr = anchorList.get(0);
				JSONObject anchorInfo = JSONObject.parseObject(anchorInfoStr);
				String timeMs = anchorInfo.getString("ts");
				long timeLong = HawkTime.parseTime(timeMs);
				String minTs = HawkTime.formatTime(timeLong, "yyyy-MM-dd HH:mm");
				if (lastAnchorTime == null  || !minTs.equals(lastAnchorTime)) {
					lastAnchorTime = minTs;
					ZonineSDK.getInstance().singleMonitorReport("anchorInfo", anchorInfoStr);
				}
			}
		} catch (Exception e) {
			HawkLog.errPrintln("fetch anchorInfo failed");
		}
	}
	
	/**
	 * 数据构建
	 * @param name
	 * @param rkey
	 * @param count
	 * @return
	 */
	private JSONObject buildFieldData(String name, String rkey, int count) {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("rkey", rkey);
		json.put("count", count);
		return json;
	}
}
