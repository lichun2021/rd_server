package com.hawk.game.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.TiberiumWar.TWBattleLog;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TWPlayerData;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.service.tiberium.logunit.TLWWarResultLogUnit;
import com.hawk.game.util.LogUtil;

/**
 * localhost:8080/script/tbNewMatch?marchServer=10549&termId=11&timeIndex=0
 * localhost:8080/script/tbNewMatch?serverCnt=500&guildCnt=20
 * localhost:8080/script/tbNewMatch?tlogTest=1
 * 
 *
 */
public class TBNewMatchTestHandler extends HawkScript {
	//参与的队伍
	private static final String TBMATCH_TEST_CWACTIVITY_JOIN_TEAM = "tbmatch_test_join_team";
	//队伍的战斗记录
	private static final String TBMATCH_TEST_TEAM_BATTLE_HISTORY = "tbmatch_test_team_battle_history";
	//参与的玩家
	private static final String TBMATCH_TEST_PLAYER_POWER = "tbmatch_test_player_power";
	//失效时间
	private static final int OUT_TIME = 3600;
	//3分钟后开始 匹配
	private static final int MATCH_DELAY = 300000;
	//耗时记录
	private static final String TBMATCH_TEST_TIME_COST = "tbmatch_test_time_cost";
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String tlogParam = params.get("tlogTest");
			if(!HawkOSOperator.isEmptyString(tlogParam)){
				String roomId = HawkUUIDGenerator.genUUID();
				int seson = 1000;
				int termId = 1;
				String guildIdA = "guildA";
				String guildIdAServer = "guildA60017";
				String guildIdB = "guildB";
				String guildIdBServer = "guildA60018";
				LogUtil.logTimberiumLeaguaMatchInfo(roomId, "60017", seson, termId, guildIdA, guildIdAServer, guildIdB,
						guildIdBServer, HawkTime.getMillisecond(),0, 1);
				TLWWarResultLogUnit resultLogUnit = new TLWWarResultLogUnit(roomId, seson, termId, guildIdA, 56565, guildIdB, 6454546, guildIdA, guildIdA, guildIdB, guildIdA,0, 1);
				LogUtil.logTimberiumLeaguaWarResult(resultLogUnit);
				return "sucess";
			}
			//QA用得
			String serverCntStr = params.get("serverCnt");
			if(!HawkOSOperator.isEmptyString(serverCntStr)){
				int serverCnt = NumberUtils.toInt(params.get("serverCnt"));
				//时间点
				int guildCnt = NumberUtils.toInt(params.get("guildCnt"));
				this.qaMatch(serverCnt,guildCnt);
				return "sucess";
			}
			//正式线上数据匹配
			//负责匹配的服务器ID
			String matchServcer = params.get("marchServer");
			//期数
			int termId = NumberUtils.toInt(params.get("termId"));
			//时间点
			int timeIndex = NumberUtils.toInt(params.get("timeIndex"));
			//搞一下匹配队伍的信息
			this.flushSignerInfo(termId, timeIndex);
			//如果是匹配服,则去匹配对阵
			String serverId = GsConfig.getInstance().getServerId();
			if(serverId.equals(matchServcer)){
				HawkDelayTask task = new HawkDelayTask(MATCH_DELAY, MATCH_DELAY, 1) {
					
					@Override
					public Object run() {
						HawkTuple3<List<TWRoomData> ,List<TWGuildData>,Map<String,TWGuildData>>  tuple = doMatch(termId, timeIndex);
						List<TWRoomData> roomList = tuple.first;
						List<TWGuildData> failList = tuple.second;
						Map<String,TWGuildData> dataMap = tuple.third;
						Set<String> guilds = new HashSet<>();
						guilds.addAll(dataMap.keySet());
						
						List<String> roomInfos = getRoomistTextInfo(roomList,dataMap);
						List<String> historys = getHistoryTextInfo(guilds, termId, timeIndex);
						List<String> playerPowers = getPlayerPowerTextInfo(guilds, termId, timeIndex);
						List<String> fails = getFialsTextInfo(failList);
						List<String> costTime = getTimeCostInfo(termId, timeIndex);
						
						String output = System.getProperty("user.dir") + "/logs/TBMatch.log";
						File file = new File(output);
						if (file.exists()) {
							file.delete();
						}
						FileWriter fileWriter = null;
						try {
							fileWriter = new FileWriter(output, true);
							for(String str : roomInfos){
								fileWriter.write(str);
							}
							
							for(String str : playerPowers){
								fileWriter.write(str);
							}
							
							for(String str : historys){
								fileWriter.write(str);
							}
							
							for(String str : fails){
								fileWriter.write(str);
							}
							
							for(String str : costTime){
								fileWriter.write(str);
							}
								
							
						} catch (IOException e) {
							HawkException.catchException(e);
						} finally {
							if (fileWriter != null) {
								try {
									fileWriter.flush();
									fileWriter.close();
								} catch (IOException e1) {
									HawkException.catchException(e1);
								}
							}
						}
						
						return null;
					}
				};
				HawkTaskManager.getInstance().postExtraTask(task);
			}
			return successResponse("succese");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	
	public List<String> getTimeCostInfo(int termId,int timeIndex){
		List<String> costList = new ArrayList<>();
		costList.add("********************************************执行耗时*****************************************************");
		costList.add("\r\n");
		//记录参与玩家的战力
		String timeCostKey = TBMATCH_TEST_TIME_COST + ":flushSignerInfo:" + termId+":"+ timeIndex;
		Map<String,String> costMap = RedisProxy.getInstance().getRedisSession().hGetAll(timeCostKey);
		StatisManager.getInstance().incRedisKey(TBMATCH_TEST_TEAM_BATTLE_HISTORY);
		for(Entry<String, String> entry : costMap.entrySet()){
			costList.add(entry.getKey()+":"+ entry.getValue());
			costList.add("\r\n");
		}
		return costList;
	}
	
	public List<String> getRoomistTextInfo(List<TWRoomData> roomList,Map<String,TWGuildData> dataMap){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************房间列表*****************************************************");
		teamList.add("\r\n");
		teamList.add("房间id_房间服务器id_联盟A_联盟B");
		teamList.add("\r\n");
		for(TWRoomData roomData : roomList){
			StringBuilder sb = new StringBuilder();
			sb.append(roomData.getId())
				.append("_")
				.append(roomData.getRoomServerId())
				.append("_")
				.append(roomData.getGuildA())
				.append("_")
				.append(dataMap.get(roomData.getGuildA()).getGuildStrength())
				.append("_")
				.append(roomData.getGuildB())
				.append("_")
				.append(dataMap.get(roomData.getGuildB()).getGuildStrength());
			teamList.add(sb.toString());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> getPlayerPowerTextInfo(Set<String> guilds,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************玩家战斗力*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("玩家ID");
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("玩家实力");
		sb.append("_").append("实力排行");
		sb.append("_").append("排行权重");
		sb.append("_").append("(power * powerWeight)");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(String guildId : guilds){
			//记录参与玩家的战力
			String powerKey = TBMATCH_TEST_PLAYER_POWER + ":" + termId+":"+ timeIndex +":" + guildId;
			Map<String,String> powerMap = RedisProxy.getInstance().getRedisSession().hGetAll(powerKey);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_PLAYER_POWER);
			for(Entry<String, String> entry : powerMap.entrySet()){
				teamList.add(entry.getValue());
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
	public List<String> getFialsTextInfo(List<TWGuildData> list){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************匹配失败队伍*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("联盟ID");
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("联盟实力");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(TWGuildData tguild : list){
			teamList.add(tguild.getId()+"_"+tguild.getServerId()+"_"+tguild.getGuildStrength());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> getHistoryTextInfo(Set<String> guilds,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************历史战绩*****************************************************");
		
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("期数");
		sb.append("_").append("胜负");
		sb.append("_").append("权重参数");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		
		for(String guildId : guilds){
			//记录参与玩家的战力
			String powerKey = TBMATCH_TEST_TEAM_BATTLE_HISTORY + ":" + termId+":"+ timeIndex +":" + guildId;
			Map<String,String> powerMap = RedisProxy.getInstance().getRedisSession().hGetAll(powerKey);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_TEAM_BATTLE_HISTORY);
			for(Entry<String, String> entry : powerMap.entrySet()){
				teamList.add(entry.getValue());
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
	
	private void flushSignerInfo(int termId,int timeIndex){
		long startTime = HawkTime.getMillisecond();
		String serverId = GsConfig.getInstance().getServerId();
		List<String> guildIds = RedisProxy.getInstance().getTWSignInfo(termId, timeIndex);
		for(String guildId : guildIds){
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guild == null) {
				continue;
			}
			Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
			int memberCnt = 0;
			long totalPower = 0;
			List<HawkTuple2<String, Long>> powerList = new ArrayList<>();
			//玩家战力
			Map<String,String> powerMap = new HashMap<>();
			//历史战绩参数
			Map<String,String> historyMap = new HashMap<>();
			
			for (String playerId : idList) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
				// 非本盟玩家
				if (member == null || !guildId.equals(member.getGuildId())) {
					continue;
				}
				memberCnt++;
				totalPower += member.getPower();
				//获取玩家对象
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				long playerStrength = player.getStrength();
				//获取玩家实力值
				powerList.add(new HawkTuple2<String, Long>(playerId,playerStrength));
				//玩家泰伯数据
				TWPlayerData playerData = new TWPlayerData();
				playerData.setId(playerId);
				playerData.setGuildAuth(member.getAuthority());
				playerData.setGuildId(guildId);
				playerData.setGuildOfficer(member.getOfficeId());
				playerData.setServerId(serverId);
				playerData.setPlayerStrength(playerStrength);
			}
			
			TWGuildData guildData = new TWGuildData();
			guildData.setId(guildId);
			guildData.setServerId(serverId);
			guildData.setName(guild.getName());
			guildData.setTag(guild.getTag());
			guildData.setFlag(guild.getFlagId());
			guildData.setMemberCnt(memberCnt);
			guildData.setTotalPower(totalPower);
			guildData.setTimeIndex(timeIndex);
			guildData.setEloScore(0);
			guildData.setGuildPower(GuildService.getInstance().getGuildBattlePoint(guildId));
			//如果开启新的战力匹配，则赋值
			long guildStrength = this.getMatchPower(serverId,guildId, powerList,powerMap,historyMap);
			guildData.setGuildStrength(guildStrength);
			
			long endTime = HawkTime.getMillisecond();
			long costTime = endTime - startTime; 
			//20分钟过期
			//记录参与的队伍信息
			String jsonString = JSON.toJSONString(guildData);
			String key = TBMATCH_TEST_CWACTIVITY_JOIN_TEAM + ":" + termId+":"+ timeIndex;
			RedisProxy.getInstance().getRedisSession().hSet(key, guildData.getId(), jsonString, OUT_TIME);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_CWACTIVITY_JOIN_TEAM);
			//记录参与玩家的战力
			String powerKey = TBMATCH_TEST_PLAYER_POWER + ":" + termId+":"+ timeIndex +":" + guildId;
			RedisProxy.getInstance().getRedisSession().hmSet(powerKey, powerMap, OUT_TIME);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_PLAYER_POWER);
			//记录参与玩家的战力
			String historyKey = TBMATCH_TEST_TEAM_BATTLE_HISTORY + ":" + termId+":"+ timeIndex +":" + guildId;
			RedisProxy.getInstance().getRedisSession().hmSet(historyKey, historyMap, OUT_TIME);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_TEAM_BATTLE_HISTORY);
			//记录耗时
			String timeCostKey = TBMATCH_TEST_TIME_COST + ":flushSignerInfo:" + termId+":"+ timeIndex;
			RedisProxy.getInstance().getRedisSession().hSet(timeCostKey, serverId + ":"+guildId, String.valueOf(costTime), OUT_TIME);
			StatisManager.getInstance().incRedisKey(TBMATCH_TEST_TEAM_BATTLE_HISTORY);
		}
		
	
	}
	
	
	/**
	 * 新得匹配战力
	 * @param teamId
	 * @param powerList
	 * @return
	 */
	public long getMatchPower(String serverId,String guildId,List<HawkTuple2<String, Long>> powerList,Map<String,String> powerMap,
			Map<String,String> historyMap){
		try {
			Collections.sort(powerList, new Comparator<HawkTuple2<String, Long>>() {
				@Override
				public int compare(HawkTuple2<String, Long> o1, HawkTuple2<String, Long> o2) {
					long power1 = o1.second;
					long power2 = o2.second;
					if(power1 == power2){
						return 0;
					}
					if(power1 > power2){
						return -1;
					}
					return 1;
				};
			});
			double memberPower = 0;
			for(int i =0;i<powerList.size();i++){
				HawkTuple2<String, Long> tuple = powerList.get(i);
				int rank = i+1;
				long power = tuple.second;
				double powerWeight = this.getPowerWeight(rank);
				memberPower += (power * powerWeight);
				
				StringBuilder sb = new StringBuilder();
				sb.append(tuple.first);
				sb.append("_").append(serverId);
				sb.append("_").append(guildId);
				sb.append("_").append(power);
				sb.append("_").append(rank);
				sb.append("_").append(powerWeight);
				sb.append("_").append((power * powerWeight));
				powerMap.put(tuple.first, sb.toString());
				
			}
			double teamParam = this.getTeamMatchParam(serverId,guildId,historyMap);
			long matchPower = (long) (teamParam * memberPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(10);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}
	
	/**
	 * 队伍磨合参数
	 * @param teamId
	 * @return
	 */
	private double getTeamMatchParam(String serverId,String guildId,Map<String,String> historyMap){
		int count = TiberiumConstCfg.getInstance().getTiberiumMatchTimesLimit() -1;
		count = Math.max(count, 0);
		List<TWBattleLog> logList = RedisProxy.getInstance().getTWBattleLog(guildId,count);
		double param = 0;
		for(TWBattleLog log : logList){
			double addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultLoss();
			int win = 0;
			if(log.getWinGuild().equals(guildId)){
				addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultWin();
				win = 1;
			}
			param += addParam;
			StringBuilder sb = new StringBuilder();
			sb.append("_").append(serverId);
			sb.append("_").append(guildId);
			sb.append("_").append(log.getTermId());
			sb.append("_").append(win);
			sb.append("_").append(addParam);
			historyMap.put(String.valueOf(log.getTermId()), sb.toString());
		}
		param = Math.min(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMaxValue());
		param = Math.max(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMinValue());
		param += 1;
		return param;
	}
	
	
	

	
	public Set<String> getTeamList(List<TWRoomData> rooms,List<TWGuildData> fails){
		Set<String> teamList = new HashSet<>();
		for(TWRoomData roomData : rooms){
			teamList.add(roomData.getGuildA());
			teamList.add(roomData.getGuildB());
		}
		
		for(TWGuildData data : fails){
			teamList.add(data.getId());
		}
		return teamList;
	}
	
	

	/**
	 * 匹配
	 * 
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public HawkTuple3<List<TWRoomData> ,List<TWGuildData>,Map<String,TWGuildData>> doMatch(int termId,int timeIndex) {
		
		Map<String,TWGuildData> dataMap = new HashMap<>();
		String key = TBMATCH_TEST_CWACTIVITY_JOIN_TEAM + ":" + termId+":"+ timeIndex;
		Map<String,String> teamDataMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		StatisManager.getInstance().incRedisKey(TBMATCH_TEST_CWACTIVITY_JOIN_TEAM);
		List<TWGuildData> dataList = new ArrayList<>();
		for (Entry<String, String> entry : teamDataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TWGuildData teamData = JSON.parseObject(dataStr, TWGuildData.class);
			dataList.add(teamData);
			dataMap.put(teamData.getId(), teamData);
		}
		
		List<TWGuildData> matchList = new ArrayList<>(dataList);
		List<HawkTuple2<TWGuildData, TWGuildData>> resultList = new ArrayList<>();
		
		
		Random random = new Random();
		do {
			if (matchList.size() <= 1) {
				break;
			}
			
			List<TWGuildData> sortList = matchList.stream().sorted().collect(Collectors.toList());
			if (sortList.size() <= 1) {
				break;
			}
			TWGuildData selectGuild = sortList.get(0);
			List<TWGuildData> filterList = matchList.stream().filter(e -> !e.serverId.equals(selectGuild.serverId) || e.id.equals(selectGuild.id)).sorted()
					.collect(Collectors.toList());
			int paramN = filterList.size();
			if (paramN <= 1) {
				break;
			}
			int paramR = filterList.indexOf(selectGuild) + 1;
			int minIndex = Math.max(1, paramR - (int) Math.ceil(1d * paramN / 20)) - 1;
			int maxIndex = Math.min(paramN, paramR + (int) Math.ceil(1d * paramN / 20)) - 1;
			List<TWGuildData> selects = new ArrayList<>();
			for (int i = minIndex; i <= maxIndex; i++) {
				TWGuildData select = filterList.get(i);
				if (!select.getId().equals(selectGuild.getId())) {
					selects.add(select);
				}
			}
			Collections.shuffle(selects);
			TWGuildData matchGuild = selects.get(0);
			resultList.add(new HawkTuple2<TWGuildData, TWGuildData>(selectGuild, matchGuild));
			matchList.remove(selectGuild);
			matchList.remove(matchGuild);
		} while (true);
		// 存在未能成功匹配的联盟
		if (!matchList.isEmpty()) {
			for (TWGuildData guildData : matchList) {
				guildData.setMatchFailed(true);
				guildData.setRoomId("");
			}
			//RedisProxy.getInstance().updateTWGuildData(matchList, termId);
		}
		List<TWGuildData> updateList = new ArrayList<>();
		List<TWRoomData> roomList = new ArrayList<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple2<TWGuildData, TWGuildData> tuple : resultList) {
			TWRoomData roomData = new TWRoomData();
			String server1 = tuple.first.getServerId();
			String server2 = tuple.second.getServerId();
			TWGuildData guildA;
			TWGuildData guildB;
			String roomServer = "";
			boolean result = server1.compareTo(server2) < 0;
			if (TiberiumConstCfg.getInstance().isRoomServerRandomOpen()) {
				result = random.nextInt(2) == 0;
			}
			if (result) {
				roomServer = server1;
				roomData.setGuildA(tuple.first.getId());
				roomData.setGuildB(tuple.second.getId());
				guildA = tuple.first;
				guildB = tuple.second;
			} else {
				roomServer = server2;
				roomData.setGuildA(tuple.second.getId());
				roomData.setGuildB(tuple.first.getId());
				guildA = tuple.second;
				guildB = tuple.first;
			}
			roomData.setTimeIndex(timeIndex);
			roomData.setId(HawkOSOperator.randomUUID());
			roomData.setRoomServerId(roomServer);
			roomData.setGroup(TLWGroupType.NORMAL);
			roomList.add(roomData);

			tuple.first.setRoomId(roomData.getId());
			tuple.first.setOppGuildId(tuple.second.getId());
			tuple.first.setMatchFailed(false);

			tuple.second.setRoomId(roomData.getId());
			tuple.second.setOppGuildId(tuple.first.getId());
			tuple.second.setMatchFailed(false);
			updateList.add(tuple.first);
			updateList.add(tuple.second);
			
			HawkLog.logPrintln("TBNewMatchTestHandler do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}, guildA: {}, guildStrengthA:{}, serverA: {}, guildB: {},  guildStrengthB:{}, serverB: {} ",
					roomData.getId(), roomData.getRoomServerId(), termId, timeIndex, guildA.getId(), guildA.getGuildStrength(), guildA.getServerId(), guildB.getId(), guildA.getGuildStrength(), guildB.getServerId());
		}
		return new HawkTuple3<List<TWRoomData>, List<TWGuildData>,Map<String,TWGuildData>>(roomList, matchList,dataMap);
	}
	
	
	
	
	private void qaMatch(int serverCnt,int guildCnt){
		//搞一下匹配队伍的信息
		HawkTuple3<Map<String,TWGuildData>,Map<String,List<String>>,Map<String,List<String>>>  rlt =
				this.qAflushSignerInfo(serverCnt,guildCnt,9999, 0);
		 
		Map<String,TWGuildData> teamJoinData = rlt.first;
		Map<String,List<String>> powers = rlt.second;
		Map<String,List<String>> rankHistorys = rlt.third;
		HawkTuple2<List<TWRoomData> ,List<TWGuildData>>  tuple = qaDoMatch(9999, 0,teamJoinData);
		List<TWRoomData> roomList = tuple.first;
		List<TWGuildData> failList = tuple.second;
		List<String> roomInfos = qaGetRoomistTextInfo(roomList,teamJoinData);
		List<String> playerPowers = qaGetPlayerPowerTextInfo(powers, 9999, 0);
		List<String> historys = qaGetHistoryTextInfo(rankHistorys, 9999, 0);
		List<String> fails = qaGetFialsTextInfo(failList);
		
		
		String output = System.getProperty("user.dir") + "/logs/TBQAMatch.log";
		File file = new File(output);
		if (file.exists()) {
			file.delete();
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(output, true);
			for(String str : roomInfos){
				fileWriter.write(str);
			}
			
			for(String str : playerPowers){
				fileWriter.write(str);
			}
			
			for(String str : historys){
				fileWriter.write(str);
			}
			
			for(String str : fails){
				fileWriter.write(str);
			}
				
			
		} catch (IOException e) {
			HawkException.catchException(e);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e1) {
					HawkException.catchException(e1);
				}
			}
		}
		
		
		
	}
	
	
	private HawkTuple3<Map<String,TWGuildData>,Map<String,List<String>>,Map<String,List<String>>> qAflushSignerInfo(
			int serverCount,int guildCount,int termId,int timeIndex){
		
		Map<String,TWGuildData> joinDataList = new HashMap<>();
		//玩家战力
		Map<String,List<String>> powerMap = new HashMap<>();
		//历史战绩参数
		Map<String,List<String>> historyMap = new HashMap<>();
		for(int s=0;s<serverCount;s++){
			for(int g=0;g<guildCount;g++){
				//玩家数据
				List<HawkTuple2<String, Long>> powerList = new ArrayList<>();
				int memberCount = HawkRand.randInt(30, 50);
				for(int p=0;p<memberCount;p++){
					String playerId = "server"+s+"-guild"+g+"-p"+p;
					long power = HawkRand.randInt(1000000, 200000000);
					powerList.add(new HawkTuple2<String, Long>(playerId, power));
				}
				//历史战绩
				List<HawkTuple2<Integer, Integer>> rankList = new ArrayList<>();
				int rankCount = 10;
				for(int h=1;h<=rankCount;h++){
					int rank = HawkRand.randInt(0, 1);
					rankList.add(new HawkTuple2<Integer, Integer>(h, rank));
				}
				String guildId = "server"+s+"-guild"+g;
				String serverId = "server"+s;
				
				TWGuildData guildData = new TWGuildData();
				guildData.setId(guildId);
				guildData.setServerId(serverId);
				guildData.setName(guildId);
				guildData.setTag(guildId);
				guildData.setFlag(0);
				guildData.setMemberCnt(memberCount);
				guildData.setTotalPower(0);
				guildData.setTimeIndex(timeIndex);
				guildData.setEloScore(0);
				guildData.setGuildPower(GuildService.getInstance().getGuildBattlePoint(guildId));
				//如果开启新的战力匹配，则赋值
				long guildStrength = this.qaGetMatchPower(serverId,guildId, powerList,powerMap,rankList,historyMap);
				guildData.setGuildStrength(guildStrength);
				
				joinDataList.put(guildId, guildData);
				
			}
		}
		return new HawkTuple3<Map<String,TWGuildData>, Map<String,List<String>>, Map<String,List<String>>>
			(joinDataList, powerMap, historyMap);
	}
	
	
	
	public long qaGetMatchPower(String serverId,String guildId,List<HawkTuple2<String, Long>> powerList,Map<String,List<String>> powerMap,
			List<HawkTuple2<Integer, Integer>> historyList,Map<String,List<String>> historyMap){
		try {
			Collections.sort(powerList, new Comparator<HawkTuple2<String, Long>>() {
				@Override
				public int compare(HawkTuple2<String, Long> o1, HawkTuple2<String, Long> o2) {
					long power1 = o1.second;
					long power2 = o2.second;
					if(power1 == power2){
						return 0;
					}
					if(power1 > power2){
						return -1;
					}
					return 1;
				};
			});
			
			List<String> list =new ArrayList<>();
			powerMap.put(guildId, list);
			
			double memberPower = 0;
			for(int i =0;i<powerList.size();i++){
				HawkTuple2<String, Long> tuple = powerList.get(i);
				int rank = i+1;
				long power = tuple.second;
				double powerWeight = this.getPowerWeight(rank);
				memberPower += (power * powerWeight);
				
				StringBuilder sb = new StringBuilder();
				sb.append(tuple.first);
				sb.append("_").append(serverId);
				sb.append("_").append(guildId);
				sb.append("_").append(power);
				sb.append("_").append(rank);
				sb.append("_").append(powerWeight);
				sb.append("_").append((power * powerWeight));
				list.add(sb.toString());
			}
			double teamParam = this.qaGetTeamMatchParam(serverId,guildId,historyList,historyMap);
			long matchPower = (long) (teamParam * memberPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	
	
	private double qaGetTeamMatchParam(String serverId,String guildId,
			List<HawkTuple2<Integer, Integer>> historyRank,Map<String,List<String>> historyMap){
		List<String> list =new ArrayList<>();
		historyMap.put(guildId, list);
		double param = 0;
		for(HawkTuple2<Integer, Integer> log : historyRank){
			double addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultLoss();
			int win = 0;
			if(log.second > 0){
				addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultWin();
				win = 1;
			}
			param += addParam;
			StringBuilder sb = new StringBuilder();
			sb.append("_").append(serverId);
			sb.append("_").append(guildId);
			sb.append("_").append(log.first);
			sb.append("_").append(win);
			sb.append("_").append(addParam);
			list.add(sb.toString());
		}
		param = Math.min(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMaxValue());
		param = Math.max(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMinValue());
		param += 1;
		return param;
	}
	
	
	
	/**
	 * 匹配
	 * 
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public HawkTuple2<List<TWRoomData> ,List<TWGuildData>> qaDoMatch(
			int termId,int timeIndex,Map<String,TWGuildData> dataList) {
		
		List<TWGuildData> matchList = new ArrayList<>(dataList.values());
		List<HawkTuple2<TWGuildData, TWGuildData>> resultList = new ArrayList<>();
		
		Random random = new Random();
		do {
			if (matchList.size() <= 1) {
				break;
			}
			
			List<TWGuildData> sortList = matchList.stream().sorted().collect(Collectors.toList());
			if (sortList.size() <= 1) {
				break;
			}
			TWGuildData selectGuild = sortList.get(0);
			List<TWGuildData> filterList = matchList.stream().filter(e -> !e.serverId.equals(selectGuild.serverId) || e.id.equals(selectGuild.id)).sorted()
					.collect(Collectors.toList());
			int paramN = filterList.size();
			if (paramN <= 1) {
				break;
			}
			int paramR = filterList.indexOf(selectGuild) + 1;
			int minIndex = Math.max(1, paramR - (int) Math.ceil(1d * paramN / 20)) - 1;
			int maxIndex = Math.min(paramN, paramR + (int) Math.ceil(1d * paramN / 20)) - 1;
			List<TWGuildData> selects = new ArrayList<>();
			for (int i = minIndex; i <= maxIndex; i++) {
				TWGuildData select = filterList.get(i);
				if (!select.getId().equals(selectGuild.getId())) {
					selects.add(select);
				}
			}
			Collections.shuffle(selects);
			TWGuildData matchGuild = selects.get(0);
			resultList.add(new HawkTuple2<TWGuildData, TWGuildData>(selectGuild, matchGuild));
			matchList.remove(selectGuild);
			matchList.remove(matchGuild);
		} while (true);
		// 存在未能成功匹配的联盟
		if (!matchList.isEmpty()) {
			for (TWGuildData guildData : matchList) {
				guildData.setMatchFailed(true);
				guildData.setRoomId("");
			}
			//RedisProxy.getInstance().updateTWGuildData(matchList, termId);
		}
		List<TWGuildData> updateList = new ArrayList<>();
		List<TWRoomData> roomList = new ArrayList<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple2<TWGuildData, TWGuildData> tuple : resultList) {
			TWRoomData roomData = new TWRoomData();
			String server1 = tuple.first.getServerId();
			String server2 = tuple.second.getServerId();
//			TWGuildData guildA;
//			TWGuildData guildB;
			String roomServer = "";
			boolean result = server1.compareTo(server2) < 0;
			if (TiberiumConstCfg.getInstance().isRoomServerRandomOpen()) {
				result = random.nextInt(2) == 0;
			}
			if (result) {
				roomServer = server1;
				roomData.setGuildA(tuple.first.getId());
				roomData.setGuildB(tuple.second.getId());
//				guildA = tuple.first;
//				guildB = tuple.second;
			} else {
				roomServer = server2;
				roomData.setGuildA(tuple.second.getId());
				roomData.setGuildB(tuple.first.getId());
//				guildA = tuple.second;
//				guildB = tuple.first;
			}
			roomData.setTimeIndex(timeIndex);
			roomData.setId(HawkOSOperator.randomUUID());
			roomData.setRoomServerId(roomServer);
			roomData.setGroup(TLWGroupType.NORMAL);
			roomList.add(roomData);

			tuple.first.setRoomId(roomData.getId());
			tuple.first.setOppGuildId(tuple.second.getId());
			tuple.first.setMatchFailed(false);

			tuple.second.setRoomId(roomData.getId());
			tuple.second.setOppGuildId(tuple.first.getId());
			tuple.second.setMatchFailed(false);
			updateList.add(tuple.first);
			updateList.add(tuple.second);
		}
		return new HawkTuple2<List<TWRoomData>, List<TWGuildData>>(roomList, matchList);
	}

	
	
	public List<String> qaGetRoomistTextInfo(List<TWRoomData> roomList,Map<String,TWGuildData> teamJoinData){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************房间列表*****************************************************");
		teamList.add("\r\n");
		teamList.add("房间id_房间服务器id_联盟A_联盟B");
		teamList.add("\r\n");
		for(TWRoomData roomData : roomList){
			StringBuilder sb = new StringBuilder();
			sb.append(roomData.getId())
				.append("_")
				.append(roomData.getRoomServerId())
				.append("_")
				.append(roomData.getGuildA())
				.append("_")
				.append(teamJoinData.get(roomData.getGuildA()).getGuildStrength())
				.append("_")
				.append(roomData.getGuildB())
				.append("_")
				.append(teamJoinData.get(roomData.getGuildB()).getGuildStrength());
			teamList.add(sb.toString());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> qaGetPlayerPowerTextInfo(Map<String,List<String>> powers,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************玩家战斗力*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("玩家ID");
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("玩家实力");
		sb.append("_").append("实力排行");
		sb.append("_").append("排行权重");
		sb.append("_").append("(power * powerWeight)");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(Entry<String, List<String>> entry : powers.entrySet()){
			//记录参与玩家的战力
			for(String str : entry.getValue()){
				teamList.add(str);
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
	public List<String> qaGetFialsTextInfo(List<TWGuildData> list){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************匹配失败队伍*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("联盟ID");
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("联盟实力");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(TWGuildData tguild : list){
			teamList.add(tguild.getId()+"_"+tguild.getServerId()+"_"+tguild.getGuildStrength());
			teamList.add("\r\n");
		}
		return teamList;
		
	}
	
	public List<String> qaGetHistoryTextInfo(Map<String,List<String>> historys,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************历史战绩*****************************************************");
		
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("_").append("服务器ID");
		sb.append("_").append("联盟ID");
		sb.append("_").append("期数");
		sb.append("_").append("胜负");
		sb.append("_").append("权重参数");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(Entry<String, List<String>> entry : historys.entrySet()){
			for(String str : entry.getValue()){
				teamList.add(str);
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
}
