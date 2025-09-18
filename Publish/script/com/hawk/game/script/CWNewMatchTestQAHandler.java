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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple4;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.cfgElement.ArmourStarExploreCollect;
import com.hawk.game.cfgElement.ArmourStarExploreObj;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.ArmourStarExploreCfg;
import com.hawk.game.config.ArmourStarExploreCollectCfg;
import com.hawk.game.config.ArmourStarExploreUpgradeCfg;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.PlayerStrengthFactory;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CWTeamJoinData;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 打印玩家作用号属性 localhost:8080/script/cwNewMatchQA?serverCnt=10&guildCnt=2&teamCnt=2
 * 玩家名字
 * 
 * @author Jesse
 *
 */
public class CWNewMatchTestQAHandler extends HawkScript {
	
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String calStrength = params.get("calStrength");
			if(!HawkOSOperator.isEmptyString(calStrength)){
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
				}
				if(calStrength.equals("1")){
					// localhost:8080/script/cwNewMatchQA?calStrength=1
					this.calcStrength(player);
				}else if(calStrength.equals("2")){
					//// localhost:8080/script/cwNewMatchQA?calStrength=2&collectId=1001
					int collectId = Integer.parseInt(params.get("collectId"));
					
					CommanderEntity entity = player.getData().getCommanderEntity();
					ArmourStarExplores starExplores = entity.getStarExplores();
					ArmourStarExploreCollect collect = starExplores.getCollect(collectId);
					// 跃迁
					ArmourStarExploreCollectCfg cfg = collect.getCfg();
					collect.setUpCount(cfg.getRandomSkillFinal());
					collect.getFixAttrMap().put(cfg.getFixAttr().first, cfg.getFixAttrLimit().second);
					collect.getRandomAttrMap().put(cfg.getRandomAttr().first, cfg.getRandomAttrRange().third);
					// db更新
					entity.notifyUpdate();
					// 通用成功返回
					player.getPush().syncArmourStarExploreInfo();
					starExplores.loadEffMap();
					player.getEffect().syncStarExplore(player, starExplores);
					// 刷新作用号
					player.getEffect().resetEffectArmour(player);
				}else if(calStrength.equals("3")){
					// localhost:8080/script/cwNewMatchQA?calStrength=2&starId=1001
					int starId = Integer.parseInt(params.get("starId"));
					// 获取星能球star
					CommanderEntity entity = player.getData().getCommanderEntity();
					ArmourStarExplores starExplores = entity.getStarExplores();
					ArmourStarExploreObj star = starExplores.getStar(starId);
					// 等级配置
					ArmourStarExploreUpgradeCfg levelUpCfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, star.getCurrentLevel() + 1);
					while (levelUpCfg != null){
						// 算下都哪些属性可以进入随机升级列表
						ArmourStarExploreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarExploreCfg.class, star.getStarId());
						List<Integer> randStarIds = new ArrayList<>();
						for (Entry<Integer, Integer> progress : star.getProgressMap().entrySet()) {
							int attrId = progress.getKey();
							int currRate = progress.getValue();
							int maxRate = cfg.getRate(attrId);
							if (currRate < maxRate) {
								randStarIds.add(attrId);
							}
						}
						// 乱序后取第一个
						Collections.shuffle(randStarIds);
						int attrId = randStarIds.get(0);
						// 升级
						int beforeVal = star.getProgressMap().get(attrId);
						star.getProgressMap().put(attrId, beforeVal + 1);
						levelUpCfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, star.getCurrentLevel() + 1);
					}
					// 升级
					starExplores.checkCollect(player.getId());
					// db更新
					entity.notifyUpdate();
					// 通用成功返回
					player.getPush().syncArmourStarExploreInfo();
					starExplores.loadEffMap();
					player.getEffect().syncStarExplore(player, starExplores);
					// 刷新作用号
					player.getEffect().resetEffectArmour(player);
					// 刷新战力
					player.refreshPowerElectric(PowerChangeReason.STAR_EXPLORE);
				}
				return successResponse("succese");
			}

			int serverCnt = NumberUtils.toInt(params.get("serverCnt"));
			int guildCnt = NumberUtils.toInt(params.get("guildCnt"));
			int teamCnt = NumberUtils.toInt(params.get("teamCnt"));
			
			//搞一下匹配队伍的信息
			HawkTuple3<Map<String,CWTeamJoinData>,Map<String,List<String>>,Map<String,List<String>>>  rlt =
					this.flushSignerInfo(serverCnt,guildCnt,teamCnt,9999, 0);
			 
			Map<String,CWTeamJoinData> teamJoinData = rlt.first;
			Map<String,List<String>> powers = rlt.second;
			Map<String,List<String>> rankHistorys = rlt.third;
			HawkTuple2<List<List<CWRoomData>> ,List<CWTeamJoinData>>  tuple = doMatch(9999, 0,teamJoinData);
			List<List<CWRoomData>> roomList = tuple.first;
			List<CWTeamJoinData> failList = tuple.second;
			List<String> roomInfos = getRoomistTextInfo(roomList,teamJoinData);
			List<String> playerPowers = getPlayerPowerTextInfo(powers, 9999, 0);
			List<String> historys = getHistoryTextInfo(rankHistorys, 9999, 0);
			List<String> fails = getFialsTextInfo(failList);
			String output = System.getProperty("user.dir") + "/logs/CWNewMatch.txt";
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
						
						
			return successResponse("succese");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	

	public List<String> getRoomistTextInfo(List<List<CWRoomData>> roomList,Map<String,CWTeamJoinData> teamJoinData){
		List<String> teamList = new ArrayList<>();
		int i = 1;
		for(List<CWRoomData> list : roomList){
			teamList.add("********************************************房间列表>>>"+i+"<<<****************************************************");
			teamList.add("\r\n");
			teamList.add("房间id\t"+"房间服务器id\t"+"战队id列表\t");
			teamList.add("\r\n");
			for(CWRoomData roomData : list){
				StringBuilder sb = new StringBuilder();
				sb.append(roomData.getId()).append("\t")
				.append(roomData.getRoomServerId()).append("\t");
				
				String str = "";
				for(String teamId : roomData.gtMaps.values()){
					if(str.length() > 0){
						str += ",";
					}
					str += (teamId+"_"+teamJoinData.get(teamId).getMatchPower()+"_"+teamJoinData.get(teamId).getServerOpenDays());
				}
				str ="["+str+ "]";
				sb.append(str);
				teamList.add(sb.toString());
				teamList.add("\r\n");
			}
			i++;
		}
		
		return teamList;
	}
	
	public List<String> getPlayerPowerTextInfo(Map<String,List<String>> powers,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************玩家战斗力*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("playerId");
		sb.append("\t").append("serverId");
		sb.append("\t").append("guildId");
		sb.append("\t").append("power");
		sb.append("\t").append("rank");
		sb.append("\t").append("powerWeight");
		sb.append("\t").append("(power * powerWeight)");
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
	
	
	public List<String> getFialsTextInfo(List<CWTeamJoinData> list){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************匹配失败队伍*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("tamId");
		sb.append("\t").append("serverId");
		sb.append("\t").append("guildId");
		sb.append("\t").append("matchPower");
		teamList.add(sb.toString());
		teamList.add("\r\n");
		for(CWTeamJoinData team : list){
			teamList.add(team.getId()+"\t"+team.getServerId()+"\t"+team.getGuildId()+"\t"+team.getMatchPower()+"\t"+team.getServerOpenDays());
			teamList.add("\r\n");
		}
		return teamList;
	}
	
	public List<String> getHistoryTextInfo(Map<String,List<String>> historys,int termId,int timeIndex){
		List<String> teamList = new ArrayList<>();
		teamList.add("********************************************历史战绩*****************************************************");
		teamList.add("\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("teamId");
		sb.append("\t").append("serverId");
		sb.append("\t").append("guildId");
		sb.append("\t").append("termId");
		sb.append("\t").append("rank");
		sb.append("\t").append("rankParam");
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
	
	
	
	private HawkTuple3<Map<String,CWTeamJoinData>,Map<String,List<String>>,Map<String,List<String>>> flushSignerInfo(int serverCount,int guildCount,int teamCount,int termId,int timeIndex){
		
		Map<String,CWTeamJoinData> joinDataList = new HashMap<>();
		//玩家战力
		Map<String,List<String>> powerMap = new HashMap<>();
		//历史战绩参数
		Map<String,List<String>> historyMap = new HashMap<>();
		for(int s=0;s<serverCount;s++){
			int serverOpenDays = HawkRand.randInt(1, 1000);
			for(int g=0;g<guildCount;g++){
				for(int t=0;t<teamCount;t++){
					//玩家数据
					List<HawkTuple2<String, Long>> powerList = new ArrayList<>();
					int memberCount = HawkRand.randInt(10, 20);
					for(int p=0;p<memberCount;p++){
						String playerId = "server"+s+"-guild"+g+"-team"+t+"-p"+p;
						long power = HawkRand.randInt(1000000, 200000000);
						powerList.add(new HawkTuple2<String, Long>(playerId, power));
					}
					//历史战绩
					List<HawkTuple2<Integer, Integer>> rankList = new ArrayList<>();
					int rankCount = 10;
					for(int h=0;h<rankCount;h++){
						int rank = HawkRand.randInt(1, 4);
						rankList.add(new HawkTuple2<Integer, Integer>(h, rank));
					}
					
					String teamId = "server"+s+"-guild"+g+"-team"+t;
					String guildId = "server"+s+"-guild"+g;
					String serverId = "server"+s;
					CWTeamJoinData joinData = new CWTeamJoinData();
					joinData.setId(teamId);
					joinData.setGuildId(guildId);
					joinData.setServerId(serverId);
					joinData.setServerOpenDays(serverOpenDays);
					long matchPower = getMatchPower(serverId, guildId, teamId, powerList,rankList, powerMap, historyMap);
					joinData.setMatchPower(matchPower);
					joinData.setMemberCnt(memberCount);
					joinDataList.put(teamId, joinData);
				}
			}
		}
		
		return new HawkTuple3<Map<String,CWTeamJoinData>, Map<String,List<String>>, Map<String,List<String>>>
			(joinDataList, powerMap, historyMap);
	}

	/**
	 * 新得匹配战力
	 * @param teamId
	 * @param powerList
	 * @return
	 */
	public long getMatchPower(String serverId,String guildId,String teamId,List<HawkTuple2<String, Long>> powerList,
			List<HawkTuple2<Integer, Integer>> historyRank,Map<String,List<String>> powerMap,
			Map<String,List<String>> historyMap){
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
			powerMap.put(teamId, list);
			double memberPower = 0;
			for(int i =0;i<powerList.size();i++){
				HawkTuple2<String, Long> tuple = powerList.get(i);
				int rank = i+1;
				long power = tuple.second;
				double powerWeight = this.getPowerWeight(rank);
				memberPower += (power * powerWeight);
				
				StringBuilder sb = new StringBuilder();
				sb.append(tuple.first);
				sb.append("\t").append(serverId);
				sb.append("\t").append(guildId);
				sb.append("\t").append(power);
				sb.append("\t").append(rank);
				sb.append("\t").append(powerWeight);
				sb.append("\t").append((power * powerWeight));
				list.add(sb.toString());
				
			}
			double teamParam = this.getTeamMatchParam(serverId,guildId,teamId,historyRank,historyMap);
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
	private double getTeamMatchParam(String serverId,String guildId,String teamId,
			List<HawkTuple2<Integer, Integer>> historyRank,Map<String,List<String>> historyMap){
		double param = 0;
		
		List<String> list =new ArrayList<>();
		historyMap.put(teamId, list);
		for(HawkTuple2<Integer, Integer> tuple : historyRank){
			int rank = tuple.second;
			double rankParam = CyborgConstCfg.getInstance().getCyborgMatchBattleResultValue(rank);
			param += rankParam;
			
			StringBuilder sb = new StringBuilder();
			sb.append(teamId);
			sb.append("\t").append(serverId);
			sb.append("\t").append(guildId);
			sb.append("\t").append(tuple.first);
			sb.append("\t").append(rank);
			sb.append("\t").append(rankParam);
			list.add(sb.toString());
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
	public HawkTuple2<List<List<CWRoomData>> ,List<CWTeamJoinData>> doMatch(int termId,int timeIndex,Map<String,CWTeamJoinData> dataList) {
		
		List<CWTeamJoinData> matchList = new ArrayList<>(dataList.values());

		List<List<CWRoomData>> roomList = new ArrayList<>();
		
			Map<Integer,List<CWTeamJoinData>> matchMap = new HashMap<>();
			List<CWTeamJoinData> extList = new ArrayList<>();
			//填充匹配池
			this.fillMatchPool(matchList, matchMap, extList);
			//匹配池数量
			int poolSize =  CyborgConstCfg.getInstance().getMatchOpenDaysPoolSize();
			//没有匹配上的队伍
			List<CWTeamJoinData> lastList = new ArrayList<>();
			for(int i=0;i<poolSize;i++){
				List<CWTeamJoinData> pool = matchMap.get(i);
				if(Objects.isNull(pool)){
					continue;
				}
				//上次剩余的添加进来
				pool.addAll(lastList);
				lastList = this.doMatchPool(termId,timeIndex,pool,roomList);
			}
			//额外池匹配
			if(extList.size() > 0){
				extList.addAll(lastList);
				lastList = this.doMatchPool(termId,timeIndex,extList,roomList);
			}
			// 未匹配成功的战队及剩余不足4队的战队
			if (!lastList.isEmpty()) {
				for (CWTeamJoinData guildData : lastList) {
					guildData.setMatchFailed(true);
					guildData.setRoomId("");
				}
			}
		 return new HawkTuple2<List<List<CWRoomData>>, List<CWTeamJoinData>>(roomList, lastList);
	}

	/**
	 * 分配匹配池
	 * @param dataList
	 * @param matchMap
	 * @param extList
	 */
	public void fillMatchPool(List<CWTeamJoinData> dataList,Map<Integer,List<CWTeamJoinData>> matchMap,List<CWTeamJoinData> extList){
		for(CWTeamJoinData data : dataList){
			int poolIndex = CyborgConstCfg.getInstance().getMatchOpenDaysPoolIndex(data.getServerOpenDays());
			if(poolIndex < 0){
				extList.add(data);
				continue;
			}
			List<CWTeamJoinData> matchList = matchMap.get(poolIndex);
			if(Objects.isNull(matchList)){
				matchList = new ArrayList<>();
				matchMap.put(poolIndex, matchList);
			}
			matchList.add(data);
		}
	}
	
	/**
	 * 匹配
	 * @param termId
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public List<CWTeamJoinData> doMatchPool(int termId,int timeIndex,List<CWTeamJoinData> dataList,List<List<CWRoomData>> roomList){
		List<CWRoomData> rlt = new ArrayList<>();
		roomList.add(rlt);
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
		
		List<CWTeamJoinData> updateList = new ArrayList<>();
		Map<String, Integer> roomServerMap = new HashMap<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData> tuple : resultList) {
			CWRoomData roomData = new CWRoomData();
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
			rlt.add(roomData);
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

			updateList.add(tuple.first);
			updateList.add(tuple.second);
			updateList.add(tuple.third);
			updateList.add(tuple.fourth);
			
		}
		
		
		List<CWTeamJoinData> lastList = new ArrayList<>();
		//剩下不足的
		lastList.addAll(matchList);
		//没找到合适队伍的
		lastList.addAll(noMatchList);
		return lastList;
	}
	
	

	/**
	 * 计算战力
	 */
	public long calcStrength(Player player) {
		long startTime = HawkTime.getMillisecond();
		JSONObject soldierStrength = new JSONObject();
		long strength = 0;
		int stype = 0;
		for (int soldierType : GsConst.calcStrengthSoldierType) {
			try {
				long typeStrength = PlayerStrengthFactory.getInstance().calcStrengtehBySolderType(player, SoldierType.valueOf(soldierType),true);
				if(typeStrength > strength){
					stype = soldierType;
				}
				strength = Math.max(typeStrength, strength);
				soldierStrength.put(String.valueOf(soldierType), typeStrength);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		strength = Math.max(strength, 0);
		PlayerStrengthFactory.logger.info("calc player strength, playerId:{}, soldierType:{}, strength:{}, costTime:{}", player.getId(), stype,strength, HawkTime.getMillisecond() - startTime);
		LogUtil.logStrength(player, soldierStrength.toJSONString(), strength);
		return strength;
	}
}
