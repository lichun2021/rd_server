package com.hawk.game.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple4;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CWTeamJoinData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;

/**
 * 打印玩家作用号属性 localhost:8080/script/effect?playerName=l0001&heroId=11 playerName:
 * 玩家名字
 * 
 * @author Jesse
 *
 */
public class CWMatchTestHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			int serverCnt = NumberUtils.toInt(params.get("serverCnt"));
			int guildCnt = NumberUtils.toInt(params.get("guildCnt"));
			int teamCnt = NumberUtils.toInt(params.get("teamCnt"));;
			List<CWTeamJoinData> joinList = randomJoinData(serverCnt,guildCnt,teamCnt);
			 HawkTuple2<List<CWRoomData> ,List<CWTeamJoinData>>  tuple = doMatch(0, joinList);
			List<String> teamList = getGuildList(joinList);
			List<CWRoomData> roomList = tuple.first;
			List<String> roomInfos = getRoomist(roomList);
			List<String> failList = getGuildList(tuple.second);
			String output = System.getProperty("user.dir") + "/logs/CWMatch.txt";
			File file = new File(output);
			if (file.exists()) {
				file.delete();
			}
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(output, true);
				fileWriter.write("********************************************战队列表*****************************************************");
				fileWriter.write("\r\n");
				fileWriter.write("战队id\t"+"服务器id\t"+"联盟id\t"+"出战数量\t"+"出战战力\t");
				fileWriter.write("\r\n");
				for (String str : teamList) {
					fileWriter.write(str);
					fileWriter.write("\r\n");
				}
				
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("********************************************房间列表*****************************************************");
				fileWriter.write("\r\n");
				fileWriter.write("房间id\t"+"房间服务器id\t"+"战队id列表\t");
				fileWriter.write("\r\n");
				for (String str : roomInfos) {
					fileWriter.write(str);
					fileWriter.write("\r\n");
				}
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("\r\n");
				fileWriter.write("********************************************失败列表*****************************************************");
				fileWriter.write("\r\n");
				fileWriter.write("战队id\t"+"服务器id\t"+"联盟id\t"+"出战数量\t"+"出战战力\t");
				fileWriter.write("\r\n");
				for (String str : failList) {
					fileWriter.write(str);
					fileWriter.write("\r\n");
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
			return successResponse("succese");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	public List<CWTeamJoinData> randomJoinData(int serverCnt,int guildCnt,int teamCnt) {
		List<CWTeamJoinData> joinList = new ArrayList<>();
		Random random = new Random();
		// 100个服
		for (int sid = 10001; sid <= 10000 + serverCnt; sid++) {
			String serverId = String.valueOf(sid);
			// 10个联盟
			for (int gid = 1; gid <= guildCnt; gid++) {
				String guildId = "gid-" + sid + "-" + gid;
				// 5个战队
				for (int tid = 1; tid <= teamCnt; tid++) {
					String teamId = "tid-" + sid + "-" + gid + "-" + tid;

					CWTeamJoinData joinData = new CWTeamJoinData();
					joinData.setId(teamId);
					joinData.setGuildId(guildId);
					joinData.setServerId(serverId);
					joinData.setMatchPower(5000000 + random.nextInt(100000000));
					joinData.setMemberCnt(25 + random.nextInt(26));
					joinList.add(joinData);
				}
			}
		}
		return joinList;
	}
	
	public List<String> getGuildList(List<CWTeamJoinData> joinList){
		List<String> teamList = new ArrayList<>();
		for(CWTeamJoinData joinData : joinList){
			StringBuilder sb = new StringBuilder();
			sb.append(joinData.getId()).append("\t")
				.append(joinData.getServerId()).append("\t")
				.append(joinData.getGuildId()).append("\t")
				.append(joinData.getMemberCnt()).append("\t")
				.append(joinData.getMatchPower());
			teamList.add(sb.toString());
		}
		return teamList;
	}
	
	public List<String> getRoomist(List<CWRoomData> roomList){
		List<String> teamList = new ArrayList<>();
		for(CWRoomData roomData : roomList){
			StringBuilder sb = new StringBuilder();
			sb.append(roomData.getId()).append("\t")
				.append(roomData.getRoomServerId()).append("\t")
				.append(new ArrayList<>(roomData.gtMaps.values()).toString());
			teamList.add(sb.toString());
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
	public HawkTuple2<List<CWRoomData> ,List<CWTeamJoinData>> doMatch(int timeIndex, List<CWTeamJoinData> dataList) {
		int termId = -1;
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
			int priorityIndex = totalSize * 15 / 10000;
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
			CyborgWarRedis.getInstance().updateCWJoinTeamData(matchList, termId);
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
