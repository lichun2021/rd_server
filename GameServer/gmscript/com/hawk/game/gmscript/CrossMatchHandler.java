package com.hawk.game.gmscript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossServerInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.world.service.WorldPylonService;

public class CrossMatchHandler extends HawkScript {
	/***
	 * termId 期数.
	 * type = 1 自动刷新本服的列表. 参数  minServerId, maxServerId, minBattleValue, maxBattleValue.
	 * type = 2 termId 查询某期的跨服列表.
	 * type = 3 termId 查询所有的区服信息
	 * type = 4 手动输入 serverId  battleValue  reset（是否重置,默认为false)
	 * type = 5 读取tmp/cross_server_list.txt 里面的配置当做跨服列表  read true 读取 false 写入, reset （是否重置, 只有写入的时候生效)
	 * type = 6 用已有的match列表重新排序. num 表示生成多少次.
	 * type = 7 查看哪些区服落单了.
	 *  type = 8 查看能量塔数量
	 */
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo arg1) {
		int termId = Integer.parseInt(params.getOrDefault("termId", "0"));
		int type = Integer.parseInt(params.getOrDefault("type", "0"));
		if (termId <= 0 ||  type <= 0) {
			return HawkScript.failedResponse(999, "termId 或者type 小于等于 0 ");
		}
		
		try {
			String value = "";
			switch (type) {
			case 1:
				value = type1(termId, params);
				break;
			case 2:
				value = type2(termId);
				break;
			case 3:
				value = type3(termId);
				break;
			case 4:
				value = type4(termId, params);
				break;
			case 5:
				value = type5(termId, params);
				break;
			case 6:
				value = type6(termId, params);
				break;
			case 7:
				value = type7(termId);
				break;
			case 8:
				value = type8();
				break;
			}			
			
			return HawkScript.successResponse(value);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(999, "出现异常了");
	}
	private String type8() {
		int count = WorldPylonService.getInstance().currentPylonCount();
		return String.valueOf(count);
	}
	
	private String type7(int termId) {
		Map<Integer, String> idServersMap = RedisProxy.getInstance().getCrossServerList(termId);
		Map<String, CrossServerInfo> idServerInfo = RedisProxy.getInstance().getCrossMatchServerBattleMap(termId);
		StringJoiner sj = new StringJoiner("_");
		for (String serverId : idServerInfo.keySet()) {
			boolean found = false;
			for (String servers : idServersMap.values()) {
				if (servers.contains(serverId)) {
					found = true; 
				}
			}
			if (!found) {
				sj.add(serverId);
			}
		}
		
		return sj.toString();
	}

	private String type6(int termId, Map<String, String> params) throws Exception{
		int num = Integer.parseInt(params.getOrDefault("num", "1"));
		List<String> valueList = new ArrayList<>();
		for (int i = 0; i < num; i ++) {
			valueList.add(">>>>>>>>>>>>>>>第"+i+"次匹配<<<<<<<<<<<<<<<<<<<<");
			String key = "cross_server_list" + ":" + termId;
			StringJoiner dup = new StringJoiner(",");
			Map<String, Integer> dupCheck = new HashMap<>();
			RedisProxy.getInstance().getRedisSession().del(key);
			Map<Integer, String> map = CrossActivityService.getInstance().doMatch(termId);			
			for (int cid : map.keySet()) {
				String cinfo = map.get(cid);
				valueList.add("组ID:"+cid+",服务器ID:"+cinfo);
				String[] arr = cinfo.trim().split("_");
				if(dupCheck.containsKey(arr[0])){
					dup.add(arr[0]);
				}
				if(dupCheck.containsKey(arr[1])){
					dup.add(arr[1]);
				}
				dupCheck.put(arr[0], 0);
				dupCheck.put(arr[1], 0);
				
				
			}
			valueList.add("重复匹配检查>>>>>>>>>>>>>>>>>");
			valueList.add(dup.toString());
		}
		
		
		
		FileWriter fw = new FileWriter(new File("match.log"));
		BufferedWriter bw = new BufferedWriter(fw);
		valueList.forEach(str->{
			try {
				bw.write(str);
			} catch (IOException e) {
				HawkException.catchException(e);
			}
			try {
				bw.newLine();
			} catch (IOException e) {
				HawkException.catchException(e);
			}
		});
		bw.close();
		fw.close();
		
		StringJoiner sj = new StringJoiner("<br>");
		sj.add("");
		for (String value : valueList) {
			sj.add(value);
		}		
		sj.add("");
		sj.add("保存到文件match.log");
		return sj.toString();
	}

	private String type5(int termId, Map<String, String> params) throws Exception {
		boolean read = Boolean.valueOf(params.getOrDefault("read", "false"));
		boolean reset = Boolean.valueOf(params.getOrDefault("reset", "false"));
		if (read) {
			CrossActivityService.getInstance().loadCrossServerList();
		} else {
			if (reset) {
				String key = RedisProxy.CROSS_SERVER_LIST + ":" + termId;
				RedisProxy.getInstance().getRedisSession().del(key);
			}
			
			String file = "tmp/cross_server_list.txt";
			List<String> stringList = new ArrayList<>();
			logger.info("load cross server list:{}", stringList);
			
			HawkOSOperator.readTextFileLines(file, stringList);
			Map<Integer, String> idServersMap = new HashMap<>(stringList.size());
			for (String lineString : stringList) {
				String[] idServers = lineString.split("\\t");
				idServersMap.put(Integer.parseInt(idServers[0]), idServers[1]);
			}
			RedisProxy.getInstance().addCrossMatchList(termId, idServersMap, GsConfig.getInstance().getPlayerRedisExpire());
		}
		
		return type3(termId);
	}

	private String type4(int termId, Map<String, String> params) {				
		boolean reset = Boolean.valueOf(params.get("reset"));
		if (reset) {
			String key = RedisProxy.CROSS_MATCH_SERVER_BATTLE + ":" + termId;
			RedisProxy.getInstance().getRedisSession().del(key);
		}
		
		CrossServerInfo crossServerInfo = new CrossServerInfo();
		crossServerInfo.setBattleValue(Integer.parseInt(params.get("battleValue")));
		crossServerInfo.setServerId(params.get("serverId"));
		crossServerInfo.setOpenServerTime(HawkRand.randInt(0, 100));
		RedisProxy.getInstance().addCrossMatchServerBattle(termId, crossServerInfo, GsConfig.getInstance().getPlayerRedisExpire());
		
		return type3(termId);
		
	}

	private String type3(int termId) {
		Map<String, CrossServerInfo> map = RedisProxy.getInstance().getCrossMatchServerBattleMap(termId);
		List<CrossServerInfo> serverList = new ArrayList<>(map.values());
		Collections.sort(serverList);
		StringJoiner sj = new StringJoiner("<br>");
		for (CrossServerInfo value : serverList) {
			sj.add(JSON.toJSONString(value));
		}		
		return sj.toString();
	}

	private String type2(int termId) {
		Map<Integer, String> idServers = RedisProxy.getInstance().getCrossServerList(termId);
		StringJoiner sj = new StringJoiner("<br>");
		for (Entry<Integer, String> entry : idServers.entrySet()) {
			sj.add(entry.toString());
		}
		return sj.toString();
	}

	public String type1(int termId, Map<String, String> params) {
		int  minServerId = Integer.parseInt(params.get("minServerId"));
		int maxServerId = Integer.parseInt(params.get("maxServerId"));
		int minBattleValue = Integer.parseInt(params.get("minBattleValue"));
		int maxBattleValue = Integer.parseInt(params.get("maxBattleValue"));
		int minOpenDays = Integer.parseInt(params.get("minOpenDays"));
		int maxOpenDays = Integer.parseInt(params.get("maxOpenDays"));
		long curTime = HawkTime.getMillisecond();
		
		
		String key = RedisProxy.CROSS_MATCH_SERVER_BATTLE + ":" + termId;
		RedisProxy.getInstance().getRedisSession().del(key);
		for (int i = minServerId; i <= maxServerId; i ++) {
			int openDay =  HawkRand.randInt(minOpenDays, maxOpenDays);
			long serverOpenTime = curTime - openDay * HawkTime.DAY_MILLI_SECONDS;
			
			int battleValue = HawkRand.randInt(minBattleValue, maxBattleValue);
			CrossServerInfo crossServerInfo = new CrossServerInfo();
			crossServerInfo.setBattleValue(battleValue);
			crossServerInfo.setOpenServerTime(serverOpenTime);
			crossServerInfo.setServerId(i+"");
			RedisProxy.getInstance().addCrossMatchServerBattle(termId, crossServerInfo, GsConfig.getInstance().getPlayerRedisExpire());
		}

		
		Map<String, CrossServerInfo> serverMap = RedisProxy.getInstance().getCrossMatchServerBattleMap(termId);
		
		Map<Integer,List<CrossServerInfo>> matchMap = new HashMap<>();
		List<CrossServerInfo> extList = new ArrayList<>();
		CrossActivityService.getInstance().fillMatchPool(serverMap, matchMap, extList);
		int size = CrossConstCfg.getInstance().getMatchOpenDaysPoolSize();
		
		StringJoiner sj = new StringJoiner("<br>");
		sj.add("");
		for(int i=0;i<size;i++ ){
			sj.add(">>>>>>>>>>>>>>>>>>>>>>>匹配分段:"+(i+1)+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			List<CrossServerInfo> list = matchMap.get(i);
			if(Objects.isNull(list)){
				continue;
			}
			Collections.sort(list);
			for (CrossServerInfo value : list) {
				sj.add("服务器ID:"+value.getServerId()+",战斗力:"+value.getBattleValue()+",开服天数:"+value.getOpenServerDays(curTime));
			}		
			sj.add("");
		}
		
		sj.add(">>>>>>>>>>>>>>>>>>>>>>>匹配分段:额外<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		Collections.sort(extList);
		for (CrossServerInfo value : extList) {
			sj.add("服务器ID:"+value.getServerId()+",战斗力:"+value.getBattleValue()+",开服天数:"+value.getOpenServerDays(curTime));
		}		
		sj.add("");
		
		return sj.toString(); 
	}
}
