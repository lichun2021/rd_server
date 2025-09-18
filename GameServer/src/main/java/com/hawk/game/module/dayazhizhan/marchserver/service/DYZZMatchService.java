package com.hawk.game.module.dayazhizhan.marchserver.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZMapBlock;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGameRoomData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZMatchData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZMember;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.DYZZWar.PBDYZZCancelTeamMatchData;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMatchData;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamNotify;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamRoomStateData;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarState;

public class DYZZMatchService extends HawkAppObj {

	public enum CAMP{A,B}
	private Map<String,DYZZMatchData> matchTeams = new HashMap<>();
	private Map<String,PBDYZZCancelTeamMatchData> cancels = new HashMap<>();
	private long lastTime;

	
	
	public DYZZMatchService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	private static DYZZMatchService instance = null;
	public static DYZZMatchService getInstance() {
		return instance;
	}
	
	
	public boolean init(){
		this.lastTime = HawkTime.getMillisecond();
		// 加载地图的阻挡信息
		if (!DYZZMapBlock.getInstance().init()) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public boolean onTick() {
		//处理匹配清除
		this.doMatchCannel();
		//匹配
		long curTime = HawkTime.getMillisecond();
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		int matchInterval = Math.max(5000, cfg.getMatchInterval());
		if(curTime - this.lastTime < matchInterval){
			return true;
		}
		this.lastTime = curTime;
		PBDYZZWarState state = DYZZService.getInstance().getDYZZWarState();
		if(state == PBDYZZWarState.DYZZ_HIDDEN){
			this.clearMatchInfo();
			return true;
		}
		this.doMatch();
		return true;
	}
	
	
	public boolean doMatch(){
		int termId = DYZZService.getInstance().getDYZZWarTerm();
		String serverId = GsConfig.getInstance().getServerId();
		int serverType = GsConfig.getInstance().getServerType();
		//如果不是正常服务器，不参与抢夺计算锁
		if(serverType != ServerType.NORMAL){
			return false;
		}
		String matchServer = DYZZRedisData.getInstance().getDYZZMatchServer(termId);
		if(HawkOSOperator.isEmptyString(matchServer)){
			//抢锁
			DYZZRedisData.getInstance().achiveMatchServer(termId, serverId);
			return false;
		}
		if(!matchServer.equals(serverId)){
			return false;
		}
		DYZZSeasonCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
		//延申匹配计算时间
		int extendTime = Math.max(10, (cfg.getMatchInterval() *2/1000));
		DYZZRedisData.getInstance().extendMatchServerTime(termId,extendTime);
		//正式匹配对战房间
		List<DYZZGameRoomData> gameSucesslist = new ArrayList<>();
		List<DYZZMatchCamp> campSucesslist = new ArrayList<>();
		Map<Integer,List<DYZZMatchData>> matchList = new HashMap<>();
		//分组并排序
		this.doMatchGroup(matchList);
		//开始匹配
		int size = this.matchTeams.size();
		for(int i=0;i<size;i++){
			DYZZMatchData data = this.doMatchHeader(matchList);
			if(data == null){
				break;
			}
			HawkLog.logPrintln("DYZZMatchService doMatch doMatchHeader,teamId:{},teamMemberCount:{},teamMatchScore:{}", data.getTeamId(),data.getMemberCount(),data.getMatchScore());
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
					this.matchTeams.remove(teamData.getTeamId());
				}
				List<DYZZMatchData> teamBList = game.getCampBTeams();
				for(DYZZMatchData teamData : teamBList){
					this.matchTeams.remove(teamData.getTeamId());
				}
				//发送RPC通知匹配成功
				this.notifyMatchGameSucess(game);
			}
		}
		if(DYZZSeasonService.getInstance().getDYZZSeasonTerm() > 0){
			for(DYZZMatchData data : this.matchTeams.values()){
				data.addMatchScore(cfg.getMatchScoreAdd());
			}
		}
		return true;
	}
	
	/**
	 * 组织一个阵营
	 * @param data
	 * @param matchList
	 * @return
	 */
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
				HawkLog.logPrintln("DYZZMatchService doMatch doMatchCamp,teamId:{},teamMemberCount:{},teamMatchScore:{}", 
						dataChoose.getTeamId(),dataChoose.getMemberCount(),dataChoose.getMatchScore());
			}
			//如果完成了匹配
			if(camp.matchCampFinish()){
				List<DYZZMatchData> teamList = camp.getCampTeams();
				for(DYZZMatchData teamData : teamList){
					matchList.get(teamData.getMemberCount()).remove(teamData);
				}
				HawkLog.logPrintln("DYZZMatchService doMatch sucess,teamId:{},teamMemberCount:{},teamMatchScore:{}", 
						data.getTeamId(),data.getMemberCount(),data.getMatchScore());
				return camp;
			}
		}
		return null;
	}
	
	
	
	/**
	 * 获取第一个匹配的队伍
	 * @param matchList
	 * @return
	 */
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
			int size = headerData.getMembers().size();
			matchList.get(size).remove(0);
		}
		return headerData;
	}
	
	/**
	 * 分组
	 * @param matchList
	 */
	private void doMatchGroup(Map<Integer,List<DYZZMatchData>> matchList){
		for(DYZZMatchData data : this.matchTeams.values()){
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
			for(DYZZMatchData data : list){
				HawkLog.logPrintln("DYZZMatchService doMatchMember,teamId:{},teamMemberCount:{},teamMatchScore:{}", 
						data.getTeamId(),data.getMemberCount(),data.getMatchScore());
			}
		}
	}
	
	/**
	 * 处理取消匹配
	 */
	private void doMatchCannel(){
		//取消匹配
		if(this.cancels.isEmpty()){
			return;
		}
		for(Map.Entry<String, PBDYZZCancelTeamMatchData> entry : this.cancels.entrySet()){
			String teamId = entry.getKey();
			PBDYZZCancelTeamMatchData cancelData = entry.getValue();
			DYZZMatchData matchData = this.matchTeams.get(teamId);
			if(matchData!= null){
				if(cancelData.getServerId().equals(matchData.getServerId())){
					this.matchTeams.remove(teamId);
				}
			}
			this.notifyMatchCancelSucess(cancelData);
		}
		//清除数据
		this.cancels.clear();
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
	
	/**
	 * 通知取消匹配完成
	 * @param data
	 */
	public void notifyMatchCancelSucess(PBDYZZCancelTeamMatchData data){
		PBDYZZTeamRoomStateData.Builder builder = PBDYZZTeamRoomStateData.newBuilder();
		builder.setTeamId(data.getTeamId());
		builder.setState(PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_CANCEL_SUCESS);
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE, builder), 
				data.getServerId(), "");
		HawkLog.logPrintln("DYZZMatchService notifyMatchCancelSucess,serverId:{},teamId: {}",data.getServerId(),data.getTeamId());
	}
	
	/**
	 * 通知匹配完成
	 * @param game
	 */
	public void notifyMatchGameSucess(DYZZGameRoomData game){
		List<DYZZMatchData> teamList = new ArrayList<>();
		teamList.addAll(game.getCampATeams());
		teamList.addAll(game.getCampBTeams());
		for(DYZZMatchData teamData : teamList){
			PBDYZZTeamRoomStateData.Builder builder = PBDYZZTeamRoomStateData.newBuilder();
			builder.setTeamId(teamData.getTeamId());
			builder.setState(PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_GAME_START);
			builder.setGameId(game.getGameId());
			CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE, builder), 
					teamData.getServerId(), "");
			HawkLog.logPrintln("DYZZMatchService notifyMatchGameSucess,serverId:{},teamId: {}",teamData.getServerId(),teamData.getTeamId());
		}
	}
	
	
	
	
	
	
	

	
	

	
	
	public void clearMatchInfo(){
		if(this.matchTeams.size() > 0){
			this.matchTeams.clear();
		}
		if(this.cancels.size() > 0){
			this.cancels.clear();
		}
	}
	
	
	public String getMatchServerId(int termId){
		String matchServer = DYZZRedisData.getInstance().getDYZZMatchServer(termId);
		return matchServer;
	}
	
	@ProtocolHandler(code = CHP.code.CROSS_DDZY_TEAM_MATCHING_REQ_VALUE)
	public void onTeamMatchGame(HawkProtocol protocol){
		PBDYZZTeamMatchData matchData = protocol.parseProtocol(PBDYZZTeamMatchData.getDefaultInstance());
		long matchTime = HawkTime.getMillisecond();
		DYZZMatchData data = new DYZZMatchData();
		data.mergeFromDYZZTeamMatchData(matchData);
		data.setMatchPoolTime(matchTime);
		this.matchTeams.put(data.getTeamId(), data);
		PBDYZZTeamRoomStateData.Builder builder = PBDYZZTeamRoomStateData.newBuilder();
		builder.setTeamId(matchData.getTeamId());
		builder.setState(PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_MATCHING);
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE, builder), 
				matchData.getServerId(), "");
		HawkLog.logPrintln("DYZZMatchService onTeamMatchGame,serverId:{},teamId: {}",matchData.getServerId(),matchData.getTeamId());
		
	}
	
	
	@ProtocolHandler(code = CHP.code.CROSS_DDZY_CANCEL_TEAM_MATCHING_REQ_VALUE)
	public void onCancelTeamMatchGame(HawkProtocol protocol){
		PBDYZZCancelTeamMatchData cancelData = protocol.parseProtocol(PBDYZZCancelTeamMatchData.getDefaultInstance());
		this.cancels.put(cancelData.getTeamId(), cancelData);
		PBDYZZTeamRoomStateData.Builder builder = PBDYZZTeamRoomStateData.newBuilder();
		builder.setTeamId(cancelData.getTeamId());
		builder.setState(PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_CANCEL_MATCHING);
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE, builder), 
				cancelData.getServerId(), "");
		HawkLog.logPrintln("DYZZMatchService onCancelTeamMatchGame,serverId:{},teamId: {}",cancelData.getServerId(),cancelData.getTeamId());
	}
	
	public String getMatchInfo(){
		StringBuilder info = new StringBuilder();
		int termId = DYZZService.getInstance().getDYZZWarTerm();
		String matchServer = DYZZRedisData.getInstance().getDYZZMatchServer(termId);
		info.append("匹配服务器ID："+matchServer+"，期数："+termId+"<br><br>");
		
		List<DYZZMatchData> matchList = new ArrayList<>();
		matchList.addAll(this.matchTeams.values());
		this.sortMatchList(matchList);
		
		for(DYZZMatchData data : matchList){
			info.append("********************************"+data.getTeamId()+"("+data.getServerId()+")********************************<br>\r\n");
			for(DYZZMember member :data.getMembers()){
				String soldiers = "";
				for(ArmyInfo soldier : member.getArmys()){
					String soldierStr = "["+soldier.getArmyId()+","+soldier.getTotalCount()+"]";
					soldiers += soldierStr;
				}
				String heros = "";
				for(int hero : member.getHeros()){
					String herStr = "["+hero+"]";
					heros += herStr;
				}
				info.append("玩家ID:"+member.getPlayerId()+","+member.getPlayerName()+"<br>\r\n");
				info.append("士兵信息:"+soldiers+"<br>\r\n");
				info.append("英雄信息:"+heros+"<br>\r\n");
				info.append("<br>\r\n");
			}
		}
		
		return info.toString();
	}
}
