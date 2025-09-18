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

import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBattleCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayer;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSON;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.dayazhizhan.marchserver.service.DYZZMatchCamp;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonPlayerData;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGameRoomData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZMatchData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMatchData;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMember;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 打印玩家作用号属性 localhost:8080/script/dymatch?teamCnt=50
 * 玩家名字
 * 
 * @author Jesse
 *
 */
public class DYZZMatchTestHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String getKda = params.get("getKda");
			String playerName = params.get("playerName");
			if(!HawkOSOperator.isEmptyString(getKda) && !HawkOSOperator.isEmptyString(playerName) ){
				DYZZBattleCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				DYZZPlayer tp = (DYZZPlayer) DYZZRoomManager.getInstance().makesurePlayer(player.getId());
				HawkTuple3<Double, Double, Double> sp3 = cfg.getScoreparameter3();
//			return (int) (killCount * sp3.first + collectHonor * sp3.second + hurtTankCount * sp3.third);
				String str = "kad :  "+ tp.getKillCount() +"*"+ sp3.first+" + " + tp.getCollectHonor() +"*"+ sp3.second +" + " + tp.getHurtTankCount() +" * " + sp3.third +" = " + tp.getKda();
				return successResponse(str);
			}

			String param1 = params.get("addSeasonScore");
			String param2 = params.get("playerName");
			if(!HawkOSOperator.isEmptyString(param1) &&
					!HawkOSOperator.isEmptyString(param2)){
				//添加积分
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
				}
				int addScore =  NumberUtils.toInt(param1);
				String playerId = player.getId();
				int threadIndex = Math.abs(playerId.hashCode()) % HawkTaskManager.getInstance().getThreadNum();
				HawkTaskManager.getInstance().postTask(new HawkTask(){
					@Override
					public Object run() {
						DYZZSeasonPlayerData seasonData = DYZZSeasonService.getInstance().getDYZZSeasonPlayerData(playerId);
						if(seasonData == null){
							return null;
						}
						seasonData.addScore(addScore);
						DYZZSeasonRedisData.getInstance().updateDYZZSeasonPlayerData(seasonData);
						//更改榜积分
						DYZZSeasonService.getInstance().updateScoreRankPlayer(seasonData.getScore(),seasonData.getServerId(),
								seasonData.getPlatform(),seasonData.getOpenId(),seasonData.getPlayerId());
						DYZZSeasonService.getInstance().checkDYZZSeasonDataSync(player);
						return null;
					}		
				},threadIndex);
				
				return successResponse("succese");
			}
			
			String param3 = params.get("resetFirstWin");
			if(!HawkOSOperator.isEmptyString(param3)){
				Map<String, String> restParams = new HashMap<>();
				restParams.put("playerName", param3);
				Player player = GlobalData.getInstance().scriptMakesurePlayer(restParams);
				if (player == null) {
					return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, restParams.toString());
				}
				DYZZRedisData.getInstance().removeDYZZWincountToday(player.getId());
				DYZZService.getInstance().syncStateInfo(player);
				return successResponse("succese");
			}
			
			String teamCnt =params.get("teamCnt");
			StringBuilder sbuilder = new StringBuilder();
			Map<String,DYZZMatchData>  maps = randomJoinData(teamCnt,sbuilder);
			this.doMatch(maps, sbuilder);
			
			String output = System.getProperty("user.dir") + "/logs/DYZZMatch.txt";
			File file = new File(output);
			if (file.exists()) {
				file.delete();
			}
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(output, true);
				fileWriter.write(sbuilder.toString());
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

	public Map<String,DYZZMatchData> randomJoinData(String teamCnt,StringBuilder matchInfo) {
		String[] arr = teamCnt.split("_");
		List<PBDYZZTeamMatchData> joinList = new ArrayList<>();
		for(int i=0;i<arr.length;i++){
			int tCnt = Integer.parseInt(arr[i]);
			int memberCnt = i+1;
			// 5个战队
			for (int tid = 1; tid <= tCnt; tid++) {
				String teamId = "team"+(i+1)+"-" + tid;
				PBDYZZTeamMatchData.Builder matchData = PBDYZZTeamMatchData.newBuilder(); 
				matchData.setServerId("60017");
				matchData.setTeamId(teamId);
				matchData.setLeader(teamId+"-member1");
				
				int memberCount = memberCnt;
				for(int m = 1;m<= memberCount;m++){
					PBDYZZTeamMember.Builder member =  PBDYZZTeamMember.newBuilder();
					String memberId = teamId+"-member"+m;
					member.setPlayerId(memberId);
					member.setPlayerName(memberId);
					member.setIcon(1);
					member.setPfIcon("pfIcon");
					member.setBattlePoint(20);
					member.setServerId("60017");
					member.setSeasonScore(HawkRand.randInt(700, 1500));
					matchData.addMembers(member);
				}
				joinList.add(matchData.build());
			}
		}
		Map<String,DYZZMatchData> teams = new HashMap<>();
		
		matchInfo.append("\r\n*********************原始队伍列表*****************************");
		matchInfo.append("\r\n队伍id\t"+"队伍人数\t"+"队伍匹配值\t"+"进入匹配池次序\t");
		for(int i=0;i<joinList.size();i++){
			PBDYZZTeamMatchData data = joinList.get(i);
			DYZZMatchData matchData = new DYZZMatchData();
			matchData.mergeFromDYZZTeamMatchData(data);
			matchData.setMatchPoolTime((long) i+1);
			teams.put(matchData.getTeamId(), matchData);
			matchInfo.append("\r\n")
			.append(matchData.getTeamId()).append("\t")
			.append(matchData.getMemberCount()).append("\t")
			.append(matchData.getTotalMatchScore()).append("\t")
			.append(matchData.getMatchPoolTime()).append("\t");
		}
		return teams;
	}
	
	

	/**
	 * 匹配
	 * 
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public void doMatch(Map<String,DYZZMatchData>  teams,StringBuilder matchInfo) {
		int termId = 1;
		//正式匹配对战房间
		List<DYZZGameRoomData> gameSucesslist = new ArrayList<>();
		List<DYZZMatchCamp> campSucesslist = new ArrayList<>();
		Map<Integer,List<DYZZMatchData>> matchList = new HashMap<>();
		//分组并排序
		this.doMatchGroup(matchList,teams);
		//开始匹配
		int size = teams.size();
		for(int i=0;i<size;i++){
			DYZZMatchData data = this.doMatchHeader(matchList);
			if(data == null){
				break;
			}
			DYZZMatchCamp camp = this.doMatchCamp(data,matchList);
			if(camp!= null){
				campSucesslist.add(camp);
			}
		}
		//阵营排序
		this.sortMatchCamp(campSucesslist);
		int campSize = campSucesslist.size();
		int roomSize = campSize / 2;
		int last = campSize % 2;
		if(last > 0){
			//移除最后一个
			campSucesslist.remove(campSize -1);
		}
		for(int i=0;i< roomSize;i++){
			if(campSucesslist.size() < 2){
				break;
			}
			int index1 = campSucesslist.size();
			DYZZMatchCamp campA = campSucesslist.remove(index1 -1);
			int index2 = campSucesslist.size();
			DYZZMatchCamp campB = campSucesslist.remove(index2 -1);
			
			String battleServer = campA.getCampTeams().get(0).getServerId();
			DYZZGameRoomData room = new DYZZGameRoomData();
			room.setTermId(termId);
			room.setServerId(battleServer);
			room.setGameId(HawkUUIDGenerator.genUUID());
			room.setCampATeams(campA.getCampTeams());
			room.setCampBTeams(campB.getCampTeams());
			room.setState(PBDYZZGameRoomState.DYZZ_GAME_INIT);
			gameSucesslist.add(room);
		}
		
		if(gameSucesslist.size() > 0){
			DYZZRedisData.getInstance().saveDYZZGameData(termId, gameSucesslist);
			//删除已经匹配上的队伍
			for(DYZZGameRoomData game : gameSucesslist){
				List<DYZZMatchData> teamAList = game.getCampATeams();
				for(DYZZMatchData teamData : teamAList){
					teams.remove(teamData.getTeamId());
				}
				List<DYZZMatchData> teamBList = game.getCampBTeams();
				for(DYZZMatchData teamData : teamBList){
					teams.remove(teamData.getTeamId());
				}
			}
		}
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		if(DYZZSeasonService.getInstance().getDYZZSeasonTerm() > 0){
			for(DYZZMatchData data : teams.values()){
				data.addMatchScore(cfg.getMatchScoreAdd());
			}
		}
	
		
		matchInfo.append("\r\n*********************匹配战局队伍列表*****************************");
		matchInfo.append("\r\n游戏id\t"+"A阵营队伍\t"+"B阵营队伍\t");
		for(int i=0;i<gameSucesslist.size();i++){
			DYZZGameRoomData data = gameSucesslist.get(i);
			List<String> campA = new ArrayList<>();
			for(DYZZMatchData matchData : data.getCampATeams()){
				String str = matchData.getTeamId()+"("+matchData.getMemberCount()+"_"+matchData.getTotalMatchScore()+")";
				campA.add(str);
			}
			List<String> campB = new ArrayList<>();
			for(DYZZMatchData matchData : data.getCampBTeams()){
				String str = matchData.getTeamId()+"("+matchData.getMemberCount()+"_"+matchData.getTotalMatchScore()+")";
				campB.add(str);
			}
			
			String campAStr = JSON.toJSONString(campA);
			String campBStr = JSON.toJSONString(campB);
			matchInfo.append("\r\n")
			.append(data.getGameId()).append("\t")
			.append(campAStr).append("\t")
			.append(campBStr).append("\t");
		}
		
		matchInfo.append("\r\n*********************剩余队伍列表*****************************");
		matchInfo.append("\r\n队伍id\t"+"队伍人数\t"+"进入匹配池次序\t"+"匹配分数\t");
		for(DYZZMatchData team : teams.values()){
			matchInfo.append("\r\n")
			.append(team.getTeamId()).append("\t")
			.append(team.getMemberCount()).append("\t")
			.append(team.getMatchPoolTime()).append("\t")
			.append(team.getTotalMatchScore()).append("\t");
		}
	}
	
	private void doMatchGroup(Map<Integer,List<DYZZMatchData>> matchList,Map<String,DYZZMatchData> teams){
		for(DYZZMatchData data : teams.values()){
			int memberCount = data.getMembers().size();
			List<DYZZMatchData> list = matchList.get(memberCount);
			if(list == null ){
				list = new ArrayList<DYZZMatchData>();
				matchList.put(memberCount, list);
			}
			list.add(data);
		}
		//排序
		for(List<DYZZMatchData> list : matchList.values()){
			this.sortMatchList(list);
		}
	}
	
	public void sortMatchList(List<DYZZMatchData> matchList){
		Collections.sort(matchList, new Comparator<DYZZMatchData>() {
			@Override
			public int compare(DYZZMatchData o1, DYZZMatchData o2) {
				int matchScore1 = o1.getTotalMatchScore();
				int matchScore2 = o2.getTotalMatchScore();
				long matchTime1 = o1.getMatchPoolTime();
				long matchTime2 = o2.getMatchPoolTime();
				if(matchScore1 != matchScore2){
					return matchScore2 - matchScore1;
				}
				if(matchTime1 != matchTime2){
					if(matchTime1 > matchTime2){
						return 1;
					}else{
						return -1;
					}
				}
				return 0;
			}
		});
	}
	
	private DYZZMatchData doMatchHeader(Map<Integer,List<DYZZMatchData>> matchList){
		DYZZMatchData headerData = null;
		for(List<DYZZMatchData> list : matchList.values()){
			if(!list.isEmpty()){
				DYZZMatchData temp = list.get(0);
				if(headerData == null){
					headerData = temp;
				}
				if(temp.getTotalMatchScore() > headerData.getTotalMatchScore()){
					headerData = temp;
				}
			}
		}
		if(headerData != null){
			int size = headerData.getMemberCount();
			matchList.get(size).remove(0);
		}
		return headerData;
	}
	
	private DYZZMatchCamp doMatchCamp(DYZZMatchData data,Map<Integer,List<DYZZMatchData>> matchList){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		DYZZMatchCamp camp = new DYZZMatchCamp();
		camp.getCampTeams().add(data);
		//人数直接够了
		if(camp.matchCampFinish()){
			return camp;
		}
		//取其他队伍
		int teamMemberCount = cfg.getTeamMemberCount();
		for(int i=teamMemberCount;i>=1;i--){
			List<DYZZMatchData> chooseDataList = matchList.get(i);
			if(chooseDataList == null){
				continue;
			}
			for(DYZZMatchData dataChoose : chooseDataList){
				boolean add = camp.addToCamp(dataChoose);
				if(!add){
					break;
				}
			}
			//如果完成了匹配
			if(camp.matchCampFinish()){
				List<DYZZMatchData> teamList = camp.getCampTeams();
				for(DYZZMatchData teamData : teamList){
					matchList.get(teamData.getMemberCount()).remove(teamData);
				}
				return camp;
			}
		}
		return null;
	}
	
	public void sortMatchCamp(List<DYZZMatchCamp> matchCampList){
		Collections.sort(matchCampList, new Comparator<DYZZMatchCamp>() {
			@Override
			public int compare(DYZZMatchCamp o1, DYZZMatchCamp o2) {
				int matchScore1 = o1.getMartchTotalScore();
				int matchScore2 = o2.getMartchTotalScore();
				return matchScore2 - matchScore1;
			}
		});
	}
}
