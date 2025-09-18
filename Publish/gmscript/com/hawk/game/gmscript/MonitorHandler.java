package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 读取监控器数据
 * 
 * @author hawk
 */
public class MonitorHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!params.containsKey("key")) {
			return "[]";
		}
		
		return readMonitorInfo(params);
	}
	
	/**
	 * 读取性能监视数据
	 * 
	 * @param paramsMap
	 * @return
	 */
	private String readMonitorInfo(Map<String, String> paramsMap) {
		// 参数：gameId, areaId, dataType, rowCount, endTime, serverId, key
		try {
			int count = -1;
			if (paramsMap.containsKey("count")) {
				count = Integer.valueOf(paramsMap.get("count"));
			}
			
			if (count == -1 && paramsMap.containsKey("rowCount")) {
				count = Integer.valueOf(paramsMap.get("rowCount"));
			}
			
			String type = paramsMap.containsKey("type") ? paramsMap.get("type") : "";
			int start = paramsMap.containsKey("start") ? Integer.valueOf(paramsMap.get("start")) : 0;
			String serverId = paramsMap.get("serverId");	
			String key = paramsMap.get("key");
			
			if (start == 0 && paramsMap.containsKey("endTime")) {
				long endTime = 0;
				String endTimeStr = paramsMap.get("endTime");
				endTimeStr = HawkOSOperator.urlDecode(endTimeStr);
				if (endTimeStr.contains(" ")) {
					endTime = HawkTime.parseTime(endTimeStr);
				} else {
					endTime = HawkTime.parseTime(endTimeStr, "yyyy-MM-ddHH:mm:ss");
				}
					
				long now = HawkTime.getMillisecond();
				endTime = Math.min(endTime, now);
				start = (int) (Math.abs(now - endTime) / HawkTime.MINUTE_MILLI_SECONDS);
			}
			
			// 全服在线人数统计
			if (key.indexOf("global_online") >= 0) {
				return readOnlineCountInfo("global_online", start, count);
			}
			
			// 超时统计
			if (GsConfig.getInstance().getTimeoutStatKeyList().contains(key)) {
				return readTimeoutByTime(key, serverId, count, paramsMap.get("endTime"));
				//return readTimeoutMonitorInfo(key, serverId, start, count);
			}
			
			// 消息推送统计
			if (key.equals("pushMsg")) {
				return readPushStatInfo(start, count);
			}
			
			List<String> monitorInfo = null;
			if (HawkOSOperator.isEmptyString(serverId)) {
				monitorInfo = HawkApp.getInstance().analyzerRead(key, start, count, type);
			} else {
				if (key.indexOf("anchor") >= 0) {
					serverId = "";
				}
				monitorInfo = HawkApp.getInstance().analyzerRead(serverId, key, start, count, type);
			}
			
			if(type.equals("hash")) {
				return monitorInfo.get(0);
			}
			
			// 按照固定格式返回JsonArray的性能监视数据表
			StringJoiner sj = new StringJoiner(",", "[", "]");
			
			for (String info : monitorInfo) {
				sj.add(info);
			}
			
			return sj.toString();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return "[]";
	}
	
	/**
	 * 读取全服同时在线人数信息
	 * 
	 * @param key
	 * @param start
	 * @param count
	 * @return
	 */
	private String readOnlineCountInfo(String key, int start, int count) {
		key = key + ":" + HawkTime.formatNowTime("yyyy-MM-dd");
		Map<String, String> countData = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		if (countData.isEmpty()) {
			return "[]";
		}
		
		List<JSONObject> dataList = new LinkedList<JSONObject>();
		try {
			for (Entry<String, String> entry : countData.entrySet()) {
				JSONObject statisInfo = new JSONObject();
				
				String minTs = entry.getKey();
				int onlineCount = Integer.valueOf(entry.getValue());
				statisInfo.put("ts", minTs);
				statisInfo.put("在线人数", onlineCount);
				dataList.add(statisInfo);
			}
			
			Collections.sort(dataList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					return o1.getString("ts").compareTo(o2.getString("ts"));
				}
			});
			
			int length = dataList.size();
			int fromIndex = length - start - count - 1;
			int toIndex = length - start;
			fromIndex = Math.max(fromIndex, 0);
			toIndex = Math.max(toIndex, 0);
			if (fromIndex >= toIndex) {
				fromIndex = 0;
				toIndex = length;
			}
			
			dataList = dataList.subList(fromIndex, toIndex);
			String minuteNow = HawkTime.formatNowTime("yyyy-MM-dd HH:mm");
			if (!dataList.isEmpty() && minuteNow.equals(dataList.get(dataList.size() - 1).getString("ts"))) {
				// 当前这一分钟的数据舍弃掉。因为各个服务器的数据上报时间可能不一致，以致当前这一分钟的数据还在动态变化中，在界面上会看到有较大的落差
				dataList = dataList.subList(0, dataList.size() - 1);
			}
			
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		List<String> monitorInfo = dataList.stream().map(e -> e.toJSONString()).collect(Collectors.toList());
		
		// 按照固定格式返回JsonArray的性能监视数据表
		StringJoiner sj = new StringJoiner(",", "[", "]");
		
		for (String info : monitorInfo) {
			sj.add(info);
		}
		
		return sj.toString();
	}
	
	/**
	 * 读取超时信息
	 * 
	 * @param key
	 * @param serverId
	 * @param start =0， 读取当天的；>0，结合paramCount最多读三天
	 * @param paramCount start=0情况下，paramCount无效，都是读当天数据；start>0, 最多读三天数据
	 */
	protected String readTimeoutMonitorInfo(String key, String serverId, int start, int paramCount) {
		long timeEnd = HawkTime.getMillisecond();
		// 获取当日零点时间
		long minTime = HawkTime.getAM0Date().getTime();
		if (start > 0) {
			timeEnd -= start * HawkTime.MINUTE_MILLI_SECONDS;
			minTime = timeEnd - paramCount * HawkTime.MINUTE_MILLI_SECONDS;
			if (timeEnd - minTime > HawkTime.DAY_MILLI_SECONDS * 3) {
				minTime = timeEnd - HawkTime.DAY_MILLI_SECONDS * 3;
			}
		}
		
		
		String monitorKey = String.format("monitor:%s:%s", key, serverId);
		// 默认取当前时间前3天的数据
		List<Object> objList = null;
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			int index = 0;
			while (true) {
				long time = timeEnd - index * HawkTime.HOUR_MILLI_SECONDS;
				if (time <= minTime) {
					break;
				}
				
				index++;
				// 按小时取数据
				String dayHour = HawkTime.formatTime(time, "yyyyMMddHH");
				String mKey = String.format("%s:%s", monitorKey, dayHour);
				pip.lrange(mKey, 0, -1);
			}
			
			objList = pip.syncAndReturnAll();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (objList == null || objList.isEmpty()) {
			return "[]";
		}
		
		List<String> dataList = new ArrayList<String>();
		for (Object obj : objList) {
			@SuppressWarnings("unchecked")
			List<String> strList = (List<String>) obj;
			dataList.addAll(strList);
		}
		
		// 对数据按关键字分类
		Map<String, List<JSONObject>> dataMap = new HashMap<String, List<JSONObject>>();
		for (String data : dataList) {
			JSONObject json = JSONObject.parseObject(data);
			String datakey = json.getString("name");
			if (HawkOSOperator.isEmptyString(datakey)) {
				continue;
			}
			
			List<JSONObject> keyDataList = dataMap.putIfAbsent(datakey, new ArrayList<JSONObject>());
			if (keyDataList == null) {
				keyDataList = dataMap.get(datakey);
			}
			
			keyDataList.add(json);
		}
		
		List<String> statObjList = new ArrayList<String>();
		// 数据条数，最大值， 平均值
		for (Entry<String, List<JSONObject>> entry : dataMap.entrySet()) {
			String dataKey = entry.getKey();
			List<JSONObject> keyDataList = entry.getValue();
			int count = keyDataList.size();
			int max = 0;
			int total = 0;
			for (JSONObject obj : keyDataList) {
				int costtime = obj.getIntValue("value");
				total += costtime;
				if (costtime > max) {
					max = costtime;
				}
			}
			
			JSONObject dataObj = new JSONObject();
			dataObj.put("name", dataKey);
			dataObj.put("超时次数", count);
			dataObj.put("最大耗时", max);
			dataObj.put("平均耗时", Math.ceil(total * 1D / count));
			statObjList.add(dataObj.toJSONString());
		}
		
		StringJoiner sj = new StringJoiner(",", "[", "]");
		
		for (String info : statObjList) {
			sj.add(info);
		}
		
		return sj.toString();
	}
	
	/**
	 * 按时间范围读取timeout数据（以endTime为终点，往前推 count 分钟）
	 * 
	 * gameId=hj&areaId=1&dataType=5&rowCount=20&endTime=2023-12-21+23%3A59%3A59&serverId=10024&user=hjol&key=msgTimeoutStat
	 * 
	 * @param key
	 * @param serverId
	 * @param dataCount
	 * @param endTimeStr
	 * @return
	 */
	protected String readTimeoutByTime(String key, String serverId, int dataCount, String endTimeStr) {
		long endTime = 0;
		if (!HawkOSOperator.isEmptyString(endTimeStr)) {
			endTimeStr = HawkOSOperator.urlDecode(endTimeStr);
			if (endTimeStr.contains(" ")) {
				endTime = HawkTime.parseTime(endTimeStr);
			} else {
				endTime = HawkTime.parseTime(endTimeStr, "yyyy-MM-ddHH:mm:ss");
			}
		}
		// 截止时间
		if (endTime == 0) {
			endTime = HawkTime.getMillisecond();
		} else {
			endTime = Math.min(endTime, HawkTime.getMillisecond());
		}
		// 没有参数默认取1小时
		if (dataCount <= 0) {
			dataCount = 60;
		}
		// 最多支持取3天整的数据
		dataCount = Math.min(dataCount, 60 * 24 * 3);
		// 起始时间
		long startTime = endTime - dataCount * HawkTime.MINUTE_MILLI_SECONDS;
		
		List<Object> objList = null;
		String monitorKey = String.format("monitor:%s:%s", key, serverId);
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			int index = 0;
			while (true) {
				long time = endTime - index * HawkTime.HOUR_MILLI_SECONDS;
				if (time <= startTime) {
					break;
				}
				
				index++;
				// 按小时取数据
				String dayHour = HawkTime.formatTime(time, "yyyyMMddHH");
				String mKey = String.format("%s:%s", monitorKey, dayHour);
				pip.lrange(mKey, 0, -1);
			}
			objList = pip.syncAndReturnAll();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (objList == null || objList.isEmpty()) {
			return "[]";
		}
		
		List<String> dataList = new ArrayList<String>();
		for (Object obj : objList) {
			@SuppressWarnings("unchecked")
			List<String> strList = (List<String>) obj;
			dataList.addAll(strList);
		}
		
		// 对数据按关键字分类
		Map<String, List<JSONObject>> dataMap = new HashMap<String, List<JSONObject>>();
		for (String data : dataList) {
			try {
				JSONObject json = JSONObject.parseObject(data);
				String datakey = json.getString("name");
				if (HawkOSOperator.isEmptyString(datakey)) {
					continue;
				}
				
				String formatTime = json.getString("ts");
				if (HawkTime.parseTime(formatTime) < startTime) {
					continue;
				}
				
				List<JSONObject> keyDataList = dataMap.putIfAbsent(datakey, new ArrayList<JSONObject>());
				if (keyDataList == null) {
					keyDataList = dataMap.get(datakey);
				}
				
				keyDataList.add(json);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		List<String> statObjList = new ArrayList<String>();
		// 数据条数，最大值， 平均值
		for (Entry<String, List<JSONObject>> entry : dataMap.entrySet()) {
			String dataKey = entry.getKey();
			List<JSONObject> keyDataList = entry.getValue();
			int max = 0, total = 0, count = 0;
			for (JSONObject obj : keyDataList) {
				try {
					int costtime = obj.getIntValue("value");
					max = Math.max(max, costtime);
					total += costtime;
					count += 1;
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			JSONObject dataObj = new JSONObject();
			dataObj.put("name", dataKey);
			dataObj.put("超时次数", count);
			dataObj.put("最大耗时", max);
			dataObj.put("平均耗时", Math.ceil(total * 1D / count));
			statObjList.add(dataObj.toJSONString());
		}
		
		StringJoiner sj = new StringJoiner(",", "[", "]");
		for (String info : statObjList) {
			sj.add(info);
		}
		
		return sj.toString();
	}
	
	/**
	 * 读取信鸽推送统计信息
	 * 
	 * @param start
	 * @param count
	 * 
	 * @return
	 */
	protected String readPushStatInfo(int start, int count) {
		// 根据start和count计算查询的起始时间和结束时间
		long now = HawkTime.getMillisecond();
		long endTime = now - start * HawkTime.MINUTE_MILLI_SECONDS;
		// 一次的数据量最多只支持6个小时
		count = Math.min(count, 360); 
		long startTime = endTime - count * HawkTime.MINUTE_MILLI_SECONDS;
		String startDayTs = HawkTime.formatTime(startTime, "yyyy-MM-dd");
		String endDayTs = HawkTime.isSameDay(startTime, endTime) ? null : HawkTime.formatTime(endTime, "yyyy-MM-dd");
		
		String recKey =  String.format("pushMsg:rec:%s", startDayTs);
		Map<String, String> recMsgData = RedisProxy.getInstance().getRedisSession().hGetAll(recKey);
		if (endDayTs != null) {
			recKey =  String.format("pushMsg:rec:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(recKey);
			recMsgData.putAll(map);
		}
		
		if (recMsgData.isEmpty()) {
			return "[]";
		}
		
		String sendKey =  String.format("pushMsg:send:%s", startDayTs);
		Map<String, String> sendMsgData = RedisProxy.getInstance().getRedisSession().hGetAll(sendKey);
		if (endDayTs != null) {
			sendKey =  String.format("pushMsg:send:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(sendKey);
			sendMsgData.putAll(map);
		}
		
		if (sendMsgData.isEmpty()) {
			return "[]";
		}
		
		String failedKey =  String.format("pushMsg:failed:%s", startDayTs);
		Map<String, String> failedMsgData = RedisProxy.getInstance().getRedisSession().hGetAll(failedKey);
		if (endDayTs != null) {
			failedKey =  String.format("pushMsg:failed:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(failedKey);
			failedMsgData.putAll(map);
		}
		
		String taskAmassKey =  String.format("pushMsg:taskAmass:%s", startDayTs);
		Map<String, String> taskAmassData = RedisProxy.getInstance().getRedisSession().hGetAll(taskAmassKey);
		if (endDayTs != null) {
			taskAmassKey =  String.format("pushMsg:taskAmass:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(taskAmassKey);
			taskAmassData.putAll(map);
		}
		
		String taskPushKey =  String.format("pushMsg:taskPush:%s", startDayTs);
		Map<String, String> taskPushData = RedisProxy.getInstance().getRedisSession().hGetAll(taskPushKey);
		if (endDayTs != null) {
			taskPushKey =  String.format("pushMsg:taskPush:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(taskPushKey);
			taskPushData.putAll(map);
		}
		
		String taskPopKey =  String.format("pushMsg:taskPop:%s", startDayTs);
		Map<String, String> taskPopData = RedisProxy.getInstance().getRedisSession().hGetAll(taskPopKey);
		if (endDayTs != null) {
			taskPopKey =  String.format("pushMsg:taskPop:%s", endDayTs);
			Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(taskPopKey);
			taskPopData.putAll(map);
		}
		
		List<JSONObject> dataList = new LinkedList<JSONObject>();
		try {
			for (Entry<String, String> entry : recMsgData.entrySet()) {
				JSONObject statisInfo = new JSONObject();
				
				String minTs = entry.getKey();
				int recCount = Integer.valueOf(entry.getValue());
				int sendCount = Integer.valueOf(sendMsgData.getOrDefault(minTs, "0"));
				int failedCount = Integer.valueOf(failedMsgData.getOrDefault(minTs, "0"));
				
				int taskAmass = Integer.valueOf(taskAmassData.getOrDefault(minTs, "0"));
				int taskPush = Integer.valueOf(taskPushData.getOrDefault(minTs, "0"));
				int taskPop = Integer.valueOf(taskPopData.getOrDefault(minTs, "0"));
				
				statisInfo.put("ts", minTs);
				statisInfo.put("推送接收数", recCount);
				statisInfo.put("推送发送数", sendCount);
				statisInfo.put("推送失败数", failedCount);
				
				statisInfo.put("任务堆压数", taskAmass);
				statisInfo.put("任务投递数", taskPush);
				statisInfo.put("任务执行数", taskPop);
				
				dataList.add(statisInfo);
			}
			
			Collections.sort(dataList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					return o1.getString("ts").compareTo(o2.getString("ts"));
				}
			});
			
			int length = dataList.size();
			int fromIndex = length - start - count - 1;
			int toIndex = length - start;
			fromIndex = Math.max(fromIndex, 0);
			toIndex = Math.max(toIndex, 0);
			if (fromIndex >= toIndex) {
				fromIndex = 0;
				toIndex = length;
			}
			
			dataList = dataList.subList(fromIndex, toIndex);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		List<String> monitorInfo = dataList.stream().map(e -> e.toJSONString()).collect(Collectors.toList());
		
		// 按照固定格式返回JsonArray的性能监视数据表
		StringJoiner sj = new StringJoiner(",", "[", "]");
		
		for (String info : monitorInfo) {
			sj.add(info);
		}
		
		return sj.toString();
	}
	
}
