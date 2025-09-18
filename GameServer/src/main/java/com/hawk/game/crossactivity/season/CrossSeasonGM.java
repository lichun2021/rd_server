package com.hawk.game.crossactivity.season;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossServerInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.service.college.CollegeService;

import redis.clients.jedis.Jedis;

public class CrossSeasonGM {
	
	
	
	public String onCMD(Map<String, String> map){
		String cmd =map.get("cmd");
		switch (cmd) {
		case "info":
			return this.printInfo();
		case "score":
			return this.scoreInfo();
		case "clearData":
			return this.clearData();
		case "doMatchTest":
			return this.doMatchTest();
		case "crossForceOpen":
			return this.crossForceOpen();
		case "crossForceLoad":
			return this.crossForceLoad();
		case "crossForceCLear":
			return this.crossForceCLear();
		default:
			return this.printInfo();
		}
	}

	
	
	 public String printInfo(){
	        //页面信息
	        String info = "";
	        try {
	            //获取本地InetAddress对象
	            InetAddress localAddress = InetAddress.getLocalHost();
	            //获取本地IP地址
	            String ipAddress = localAddress.getHostAddress();
	            //获得gm端口
	            int port = GsConfig.getInstance().getGmPort();
	            //组装页面信息
	            
	            CrossSeaonStateData state = CrossActivitySeasonService.getInstance().getCrossSeaonStateData();
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=info\">刷新</a>          &nbsp;&nbsp; ";
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=score\">查看积分</a>        &nbsp;&nbsp;    ";
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=doMatchTest\">匹配测试</a>       &nbsp;&nbsp;    ";
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=clearData\">清除数据(所有)</a>        &nbsp;&nbsp;  |&nbsp;  ";
	            
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=crossForceOpen\">强制开启跨服</a>       &nbsp;&nbsp;   ";
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=crossForceLoad\">强制开启加载</a>       &nbsp;&nbsp;   ";
	            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CROSSSEASON&cmd=crossForceCLear\">强制开启清除</a>       &nbsp;&nbsp;   <br>";
	            
	            info += ("=====================当前赛季状态===================<br>");
	            info += ("===赛季:"+state.getSeason() + "<br>");
	            info += ("===状态:"+ state.getState().getStateEnum().name()+ "<br>");
	            info += ("===================================================<br>");
	        }catch (Exception e){
	            HawkException.catchException(e);
	        }
	        return info;
	    }
	 
	 public String crossForceCLear(){
		 String info = "<br>============================清除数据======================================<br>";
			try (Jedis jedis =  RedisProxy.getInstance().getRedisSession().getJedis()) {
				Set<String> keys = jedis.keys("CROSS_FORCE_*");
				for(String key : keys){
					jedis.del(key);
					info += (key+"<br>");
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return this.printInfo() + info;
	 }
	 
	 public String crossForceLoad(){
		 String info = "<br>============================加载匹配数据======================================<br>";
		 int termId = CrossConstCfg.getInstance().getDebugTermId();
		 String matchOverKey = "CROSS_FORCE_MATCH_OVER:" + termId ;
		 Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(matchOverKey);
		 if(map.size() > 0){
			 RedisProxy.getInstance().getRedisSession().del(matchOverKey);
			 Map<Integer,String> matchs = new HashMap<>();
			for(String key : map.keySet()){
				int cross =Integer.parseInt(key);
				matchs.put(cross, map.get(key));
			}
			 AssembleDataManager.getInstance().parseCrossServerList(matchs);
			 info +="加载完成";
			 
		 }else{
			 info +="数据不足";
		 }
		 return this.printInfo() + info;
	 }
	 
	 
	 public String crossForceOpen(){
		 
		int termId = CrossConstCfg.getInstance().getDebugTermId();
		String info = "==================强制开启航海远征===========================<br>";
		try {
			Field field = null;
			
			field = CrossConstCfg.class.getDeclaredField("debugTermId");
			field.setAccessible(true);
			field.set(CrossConstCfg.getInstance(), termId);
			
			//debug模式活动阶段 1SHOW 2OPEN 3END 4HIDDEN
			field = CrossConstCfg.class.getDeclaredField("debugActivityState");
			field.setAccessible(true);
			field.set(CrossConstCfg.getInstance(), 2);
			
			field = CrossConstCfg.class.getDeclaredField("debugMode");
			field.setAccessible(true);
			field.set(CrossConstCfg.getInstance(), true);
			
			String serverId = GsConfig.getInstance().getServerId();
			String matchKey = "CROSS_FORCE_SERVERS:" + termId ;
			RedisProxy.getInstance().getRedisSession().hSet(matchKey, serverId, String.valueOf(termId), (int)TimeUnit.MINUTES.toSeconds(3));
			Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(matchKey);
			if(map.size() >=2){
				
				RedisProxy.getInstance().getRedisSession().del(matchKey);
				int crossId = termId * 10 +1;
				List<String> servers = new ArrayList<>(map.keySet());
				Map<Integer,String> matchs = new HashMap<>();
				String matchStr = servers.get(0)+"_"+servers.get(1);
				matchs.put(crossId, matchStr);
				AssembleDataManager.getInstance().parseCrossServerList(matchs);
				
				String matchOverKey = "CROSS_FORCE_MATCH_OVER:" + termId ;
				RedisProxy.getInstance().getRedisSession().hSet(matchOverKey, String.valueOf(crossId), matchStr, (int)TimeUnit.MINUTES.toSeconds(3));
				info +="匹配完成:"+matchs.toString();
			}else{
				info += "匹配数量不足";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		 return this.printInfo() + info;
	 }




	 public String scoreInfo(){
		 CrossSeaonStateData state = CrossActivitySeasonService.getInstance().getCrossSeaonStateData();
		 Map<String,CrossSeasonServerData> scores = CrossSeasonServerData.loadAllData(state.getSeason());
		 String info = "<br>============================积分信息======================================<br>";
		 for(CrossSeasonServerData data : scores.values()){
			 String str = data.serialize();
			 info += (str+"<br>");
		 }
		 return this.printInfo() + info;
	 }
	 
	 
	 public String clearData(){
		String info = "<br>============================清除数据======================================<br>";
		try (Jedis jedis =  RedisProxy.getInstance().getRedisSession().getJedis()) {
			Set<String> keys = jedis.keys("CROSS_SEASON*");
			for(String key : keys){
				jedis.del(key);
				info += (key+"<br>");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return this.printInfo() + info;
	 }
	 
	 
	 public String doMatchTest(){
		 
		 int seasonId = 952700;
		 int crossId = 952701;
		 
		 int serverCnt = HawkRand.randInt(50, 100);
		 long timeStart = HawkTime.getMillisecond();
		 int openDaysMax = 1000;
		 int powerStart = 1000000;
		 int powerEnd = 1000000 * 20;
		 
		 int serverStart = 100000;
		 
		 int scoreStart = 100;
		 int scoreEnd = 2000;
		 
		 List<CrossServerInfo> list1 = new ArrayList<>();
		 Map<String,CrossSeasonServerData> seasonMap = new HashMap<>();
		 for(int i=1;i<=serverCnt;i++){
			 String serverId = String.valueOf(serverStart +i);
			 CrossServerInfo serverInfo = new CrossServerInfo();
			 serverInfo.setServerId(serverId);
			 serverInfo.setOpenServerTime(timeStart + HawkRand.randInt(1, openDaysMax) * HawkTime.DAY_MILLI_SECONDS);
			 serverInfo.setBattleValue(HawkRand.randInt(powerStart, powerEnd));
			 
			 RedisProxy.getInstance().addCrossMatchServerBattle(crossId, serverInfo, 10);
			
			int randomsame = HawkRand.randInt(1, 10);
			CrossSeasonServerData data = new CrossSeasonServerData();
		 	data.setSeason(952700);
			data.setServerId(serverId);
			data.setScore(HawkRand.randInt(scoreStart, scoreEnd));
			if(randomsame < 4){
				int tid = HawkRand.randInt(1, i);
				String tarId = String.valueOf(serverStart +tid);
				CrossSeasonServerData tar =  seasonMap.get(tarId);
				if(Objects.nonNull(tar)){
					data.setScore(tar.getScore());
				}
			}
			data.setInitTime(serverInfo.getOpenServerTime());
			data.setMergeTime(0);
			data.saveData();
			
			list1.add(serverInfo);
			seasonMap.put(serverId,data);
		 }
		 
		 
		 
		 Collections.sort(list1,new Comparator<CrossServerInfo>() {
				@Override
				public int compare(CrossServerInfo o1, CrossServerInfo o2) {
					CrossSeasonServerData sdata1 = seasonMap.get(o1.getServerId());
					CrossSeasonServerData sdata2 = seasonMap.get(o2.getServerId());
					int serverScore1 = 0;
					int serverScore2 = 0;
					if(Objects.nonNull(sdata1)){
						serverScore1 = sdata1.getScore();
					}
					if(Objects.nonNull(sdata2)){
						serverScore2 = sdata2.getScore();
					}
					if(serverScore1 != serverScore2){
						return serverScore1 - serverScore2 > 0?-1 : 1;
					}else if(o1.getBattleValue() != o2.getBattleValue()){
						return o1.getBattleValue() - o2.getBattleValue() > 0? -1 : 1;
					}else if (o1.getOpenServerTime() == o2.getOpenServerTime()) {
						return o1.getServerId().compareTo(o2.getServerId());
					} else {
						return o1.getOpenServerTime() - o2.getOpenServerTime() > 0 ? 1 : -1;
					}
				}
			});
		 
		 
		 String key = "cross_server_list:" + crossId;
		 RedisProxy.getInstance().getRedisSession().del(key);
		 int oldSeason = CrossActivitySeasonService.getInstance().getCrossSeaonStateData().getSeason();
		 CrossActivitySeasonService.getInstance().getCrossSeaonStateData().setSeason(seasonId);
		 CrossActivitySeasonService.getInstance().doMatch(crossId);
		 CrossActivitySeasonService.getInstance().getCrossSeaonStateData().setSeason(oldSeason);
		 
		 String key2 = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCORE , seasonId);
		 RedisProxy.getInstance().getRedisSession().del(key2);
		 
		 Map<Integer, String> map =  RedisProxy.getInstance().getCrossServerList(crossId);
		 
		 String info = "<br>============================匹配测试(间隔20S)======================================<br>";
		 
		 info += "<br>>>>>>>>>>>>>>>>赛季服务器数据<<<<<<<<<<<<<<<《<br>";
		 int i=1;
		 for(CrossServerInfo serverInfo :list1){
			 CrossSeasonServerData sdata2 = seasonMap.get(serverInfo.getServerId());
			 int score = -1;
			 if(Objects.nonNull(sdata2)){
				 score = sdata2.getScore();
			 }
			 info +=(String.format(i+"-服务器ID:%s,积分:%s,战力:%s", serverInfo.getServerId() ,score,serverInfo.getBattleValue())+"<br>");
			 i++;
		 }
		 info += "<br>>>>>>>>>>>>>>>>赛季匹配数据<<<<<<<<<<<<<<<《<br>";
		 for(int cid : map.keySet()){
			 info += (cid+":"+map.get(cid)+"<br>");
		 }
		 
		 
			Map<String, CrossServerInfo> serverMap = RedisProxy.getInstance().getCrossMatchServerBattleMap(crossId);
			long curTime = HawkTime.getMillisecond();
			Map<Integer,List<CrossServerInfo>> matchMap = new HashMap<>();
			List<CrossServerInfo> extList = new ArrayList<>();
			//填充匹配池
			CrossActivityService.getInstance().fillMatchPool(serverMap, matchMap, extList);
		 
		 
		 RedisProxy.getInstance().getRedisSession().del(key);
		 CrossActivityService.getInstance().doMatch(crossId);
		 Map<Integer, String> map2 =  RedisProxy.getInstance().getCrossServerList(crossId);
		 
		 info += "<br>>>>>>>>>>>>>>>>正常服务器数据<<<<<<<<<<<<<<<《<br>";
		 
		 i=1;
		 for(int intv : matchMap.keySet()){
			 info += "<br>>>>>>>>>>>>>>>>匹配池:"+intv+"<<<<<<<<<<<<<<<《<br>";
			 List<CrossServerInfo> matchServer = matchMap.get(intv);
			 Collections.sort(matchServer);
			 for(CrossServerInfo serverInfo :matchServer){
				 info +=(String.format(i+"-服务器ID:%s,战力:%s,开服天数:%s,", serverInfo.getServerId() ,serverInfo.getBattleValue(),serverInfo.getOpenServerDays(curTime))+"<br>");
				 i++;
			 }
		 }
		 
		 info += "<br>>>>>>>>>>>>>>>>正常匹配数据<<<<<<<<<<<<<<<《<br>";
		 for(int cid : map2.keySet()){
			 info += (cid+":"+map2.get(cid)+"<br>");
		 }
		 
		 return this.printInfo() + info;
	 }
	 
}
