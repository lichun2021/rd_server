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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple4;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CyborgWar.CWBattleLog;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CWTeamData;
import com.hawk.game.service.cyborgWar.CWTeamJoinData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;

/**
 * 打印玩家作用号属性 localhost:8080/script/cwNewMatch?playerName=l0001&heroId=11 playerName:
 * 玩家名字
 * 
 * @author Jesse
 *
 */
public class CWNewMatchTestHandler extends HawkScript {
	//参与的队伍
	private static final String CWMATCH_TEST_CWACTIVITY_JOIN_TEAM = "cwmatch_test_join_team";
	//队伍的战斗记录
	private static final String CWMATCH_TEST_TEAM_BATTLE_HISTORY = "cwmatch_test_team_battle_history";
	//参与的玩家
	private static final String CWMATCH_TEST_PLAYER_POWER = "cwmatch_test_player_power";
	//失效时间
	private static final int OUT_TIME = 1200;
	//3分钟后开始 匹配
	private static final int MATCH_DELAY = 180000;
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
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
						HawkTuple2<List<CWRoomData> ,List<CWTeamJoinData>>  tuple = doMatch(termId, timeIndex);
						List<CWRoomData> roomList = tuple.first;
						List<CWTeamJoinData> failList = tuple.second;
						Set<String> teams = getTeamList(roomList, failList);
						List<String> roomInfos = getRoomistTextInfo(roomList);
						List<String> historys = getHistoryTextInfo(teams, termId, timeIndex);
						List<String> playerPowers = getPlayerPowerTextInfo(teams, termId, timeIndex);
						List<String> fails = getFialsTextInfo(failList);
						String output = System.getProperty("user.dir") + "/logs/CWMatch.txt";
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
	
	
	public List<String> getRoomistTextInfo(List<CWRoomData> roomList){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************房间列表*****************************************************");
		teamList.add("\r\n");
		teamList.add("房间id\t"+"房间服务器id\t"+"战队id列表\t");
		teamList.add("\r\n");
		for(CWRoomData roomData : roomList){
			StringBuilder sb = new StringBuilder();
			sb.append(roomData.getId()).append("\t")
				.append(roomData.getRoomServerId()).append("\t")
				.append(new ArrayList<>(roomData.gtMaps.values()).toString());
			teamList.add(sb.toString());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> getPlayerPowerTextInfo(Set<String> teams,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************玩家战斗力*****************************************************");
		teamList.add("\r\n");
		for(String teamId : teams){
			//记录参与玩家的战力
			String powerKey = CWMATCH_TEST_PLAYER_POWER + ":" + termId+":"+ timeIndex +":" + teamId;
			Map<String,String> powerMap = RedisProxy.getInstance().getRedisSession().hGetAll(powerKey);
			StatisManager.getInstance().incRedisKey(CWMATCH_TEST_PLAYER_POWER);
			for(Entry<String, String> entry : powerMap.entrySet()){
				teamList.add(entry.getValue());
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
	public List<String> getFialsTextInfo(List<CWTeamJoinData> list){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************匹配失败队伍*****************************************************");
		teamList.add("\r\n");
		for(CWTeamJoinData team : list){
			teamList.add(team.getId()+"_"+team.getServerId()+"_"+team.getGuildId()+"_"+team.getMatchPower());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> getHistoryTextInfo(Set<String> teams,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************历史战绩*****************************************************");
		teamList.add("\r\n");
		for(String teamId : teams){
			//记录参与玩家的战力
			String powerKey = CWMATCH_TEST_TEAM_BATTLE_HISTORY + ":" + termId+":"+ timeIndex +":" + teamId;
			Map<String,String> powerMap = RedisProxy.getInstance().getRedisSession().hGetAll(powerKey);
			StatisManager.getInstance().incRedisKey(CWMATCH_TEST_TEAM_BATTLE_HISTORY);
			for(Entry<String, String> entry : powerMap.entrySet()){
				teamList.add(entry.getValue());
				teamList.add("\r\n");
			}
		}
		return teamList;
	}
	
	
	
	private void flushSignerInfo(int termId,int timeIndex){
		String serverId = GsConfig.getInstance().getServerId();
		List<String> teamIds = CyborgWarRedis.getInstance().getCWSignInfo(termId, timeIndex);
		for(String teamId : teamIds){
			CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
			if (teamData == null) {
				continue;
			}
			String guildId = teamData.getGuildId();
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guild == null) {
				continue;
			}
			Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
			int memberCnt = 0;
			long totalPower = 0;
			long matchPower = 0;
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
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if(player == null){
					continue;
				}
				totalPower += member.getPower();
				powerList.add(new HawkTuple2<String, Long>(playerId,player.getStrength()));
			}
			CWTeamJoinData teamJoinData = new CWTeamJoinData();
			teamJoinData.setId(teamId);
			teamJoinData.setGuildId(guildId);
			teamJoinData.setServerId(serverId);
			teamJoinData.setName(teamData.getName());
			teamJoinData.setTag(guild.getTag());
			teamJoinData.setFlag(guild.getFlagId());
			teamJoinData.setMemberCnt(memberCnt);
			teamJoinData.setTotalPower(totalPower);
			teamJoinData.setMatchPower(matchPower);
			teamJoinData.setTimeIndex(timeIndex);
			long newMatchPower = this.getMatchPower(serverId,guildId,teamId,powerList,powerMap,historyMap);
			teamJoinData.setMatchPower(newMatchPower);
			
			//20分钟过期
			//记录参与的队伍信息
			String jsonString = JSON.toJSONString(teamJoinData);
			String key = CWMATCH_TEST_CWACTIVITY_JOIN_TEAM + ":" + termId+":"+ timeIndex;
			RedisProxy.getInstance().getRedisSession().hSet(key, teamJoinData.getId(), jsonString, OUT_TIME);
			StatisManager.getInstance().incRedisKey(CWMATCH_TEST_CWACTIVITY_JOIN_TEAM);
			//记录参与玩家的战力
			String powerKey = CWMATCH_TEST_PLAYER_POWER + ":" + termId+":"+ timeIndex +":" + teamId;
			RedisProxy.getInstance().getRedisSession().hmSet(powerKey, powerMap, OUT_TIME);
			StatisManager.getInstance().incRedisKey(CWMATCH_TEST_PLAYER_POWER);
			//记录参与玩家的战力
			String historyKey = CWMATCH_TEST_TEAM_BATTLE_HISTORY + ":" + termId+":"+ timeIndex +":" + teamId;
			RedisProxy.getInstance().getRedisSession().hmSet(historyKey, historyMap, OUT_TIME);
			StatisManager.getInstance().incRedisKey(CWMATCH_TEST_TEAM_BATTLE_HISTORY);
		}
		
	
	}
	
	/**
	 * 新得匹配战力
	 * @param teamId
	 * @param powerList
	 * @return
	 */
	public long getMatchPower(String serverId,String guildId,String teamId,List<HawkTuple2<String, Long>> powerList,Map<String,String> powerMap,
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
				sb.append("_").append(teamId);
				sb.append("_").append(power);
				sb.append("_").append(rank);
				sb.append("_").append(powerWeight);
				sb.append("_").append((power * powerWeight));
				powerMap.put(tuple.first, sb.toString());
				
			}
			double teamParam = this.getTeamMatchParam(serverId,guildId,teamId,historyMap);
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
	private double getTeamMatchParam(String serverId,String guildId,String teamId,Map<String,String> historyMap){
		int count = CyborgConstCfg.getInstance().getCyborgMatchTimesLimit();
		count = Math.max(count, 0);
		List<CWBattleLog> logList = CyborgWarRedis.getInstance().getCWBattleLog(teamId,count);
		double param = 0;
		for(CWBattleLog log : logList){
			for(CWTeamInfo team : log.getTeamInfoList()){
				if(team.getId().equals(teamId)){
					int rank = team.getRank();
					double rankParam = CyborgConstCfg.getInstance().getCyborgMatchBattleResultValue(rank);
					param += rankParam;
					StringBuilder sb = new StringBuilder();
					sb.append(teamId);
					sb.append("_").append(serverId);
					sb.append("_").append(guildId);
					sb.append("_").append(log.getTermId());
					sb.append("_").append(rank);
					sb.append("_").append(rankParam);
					historyMap.put(String.valueOf(log.getTermId()), sb.toString());
				}
			} 
		}
		param = Math.min(param, CyborgConstCfg.getInstance().getCyborgMatchCofMaxValue());
		param = Math.max(param, CyborgConstCfg.getInstance().getCyborgMatchCofMinValue());
		return param + 1;
	}
	
	
	

	
	public Set<String> getTeamList(List<CWRoomData> rooms,List<CWTeamJoinData> fails){
		Set<String> teamList = new HashSet<>();
		for(CWRoomData roomData : rooms){
			Map<String,String> gtMap = roomData.getGtMaps();
			teamList.addAll(gtMap.values());
		}
		
		for(CWTeamJoinData data : fails){
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
	public HawkTuple2<List<CWRoomData> ,List<CWTeamJoinData>> doMatch(int termId,int timeIndex) {
		
		String key = CWMATCH_TEST_CWACTIVITY_JOIN_TEAM + ":" + termId+":"+ timeIndex;
		Map<String,String> teamDataMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		StatisManager.getInstance().incRedisKey(CWMATCH_TEST_CWACTIVITY_JOIN_TEAM);
		List<CWTeamJoinData> dataList = new ArrayList<>();
		for (Entry<String, String> entry : teamDataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			CWTeamJoinData teamData = JSON.parseObject(dataStr, CWTeamJoinData.class);
			dataList.add(teamData);
		}
		
		List<CWTeamJoinData> matchList = new ArrayList<>(dataList);
		List<HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData>> resultList = new ArrayList<>();

		// 匹配不到对手的联盟列表
		List<CWTeamJoinData> noMatchList = new ArrayList<>();
		do {
			// 不足四队,终止匹配
			if (matchList.size() < 4) {
				break;
			}

			List<CWTeamJoinData> sortList = matchList.stream().sorted(new Comparator<CWTeamJoinData>() {
				@Override
				public int compare(CWTeamJoinData arg0, CWTeamJoinData arg1) {
					if (arg0.getMatchPower() > arg1.getMatchPower()) {
						return -1;
					} else if (arg0.getMatchPower() < arg1.getMatchPower()) {
						return 1;
					} else {
						return arg0.getId().compareTo(arg1.getId());
					}
				}
			}).collect(Collectors.toList());
			CWTeamJoinData selectGuild = sortList.get(0);
			List<CWTeamJoinData> matchedList = new ArrayList<>();
			matchedList.add(selectGuild);
			sortList.remove(selectGuild);
			List<String> matchedGuild = new ArrayList<>();
			matchedGuild.add(selectGuild.getGuildId());
			
			int totalSize = sortList.size();
			int priorityIndex = (int) (1l * totalSize * CyborgConstCfg.getInstance().getMatchTopRate() / 10000);
			List<CWTeamJoinData> priorityList = sortList.subList(0, priorityIndex);
			// 前5%的战队顺序打散
			Collections.shuffle(priorityList);
			for (int i = 0; i < 3; i++) {
				for (CWTeamJoinData tData : sortList) {
					// 该战队联盟未匹配入本房间,符合入围标准
					if (!matchedGuild.contains(tData.getGuildId())) {
						matchedList.add(tData);
						matchedGuild.add(tData.getGuildId());
						break;
					}
				}
			}
			// 该战队匹配不满3个对手,则该战队匹配失败
			if (matchedList.size() != 4) {
				matchList.remove(selectGuild);
				noMatchList.add(selectGuild);
			} else {
				resultList.add(new HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData>(matchedList.get(0), matchedList.get(1), matchedList.get(2),
						matchedList.get(3)));
				matchList.removeAll(matchedList);
			}
		} while (true);
		// 未匹配成功的战队及剩余不足4队的战队
		matchList.addAll(noMatchList);
		if (!matchList.isEmpty()) {
			for (CWTeamJoinData guildData : matchList) {
				guildData.setMatchFailed(true);
				guildData.setRoomId("");
			}
			//CyborgWarRedis.getInstance().updateCWJoinTeamData(matchList, termId);
		}
		List<CWRoomData> roomList = new ArrayList<>();
		Map<String, Integer> roomServerMap = new HashMap<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData> tuple : resultList) {
			CWRoomData roomData = new CWRoomData();
			List<String> serverIds = new ArrayList<>();
			serverIds.add(tuple.first.getServerId());
			serverIds.add(tuple.second.getServerId());
			serverIds.add(tuple.third.getServerId());
			serverIds.add(tuple.fourth.getServerId());
			Map<String, String> guildTeamMap = new HashMap<>();
			guildTeamMap.put(tuple.first.getGuildId(), tuple.first.getId());
			guildTeamMap.put(tuple.second.getGuildId(), tuple.second.getId());
			guildTeamMap.put(tuple.third.getGuildId(), tuple.third.getId());
			guildTeamMap.put(tuple.fourth.getGuildId(), tuple.fourth.getId());
			roomData.setGtMaps(guildTeamMap);
			roomData.setTimeIndex(timeIndex);
			roomData.setId(HawkOSOperator.randomUUID());
			// 筛选当前负载最低的服作为房间服
			String roomServer = CyborgWarService.getInstance().selectRoomServer(roomServerMap, tuple);
			roomData.setRoomServerId(roomServer);
			if (roomServerMap.containsKey(roomServer)) {
				roomServerMap.put(roomServer, roomServerMap.get(roomServer) + 1);
			} else {
				roomServerMap.put(roomServer, 1);
			}
			roomData.setRoomServerId(roomServer);
			roomList.add(roomData);
			List<String> teamIdList = new ArrayList<>(guildTeamMap.values());
			Collections.sort(teamIdList);
			tuple.first.setRoomId(roomData.getId());
			tuple.first.setRoomTeams(teamIdList);
			tuple.first.setMatchFailed(false);

			tuple.second.setRoomId(roomData.getId());
			tuple.second.setRoomTeams(teamIdList);
			tuple.second.setMatchFailed(false);

			tuple.third.setRoomId(roomData.getId());
			tuple.third.setRoomTeams(teamIdList);
			tuple.third.setMatchFailed(false);

			tuple.fourth.setRoomId(roomData.getId());
			tuple.fourth.setRoomTeams(teamIdList);
			tuple.fourth.setMatchFailed(false);
		}
		 return new HawkTuple2<List<CWRoomData>, List<CWTeamJoinData>>(roomList, matchList);
	}
}
