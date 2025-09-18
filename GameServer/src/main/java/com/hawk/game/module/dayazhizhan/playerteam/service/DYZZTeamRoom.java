package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamState;

public class DYZZTeamRoom {
	
	private int termId;
	
	private String serverId;
	
	private String teamId;
		
	private String gameRoomId;
	
	private String leader;
	
	private List<DYZZMember> members = new CopyOnWriteArrayList<>();
	
	private long createTime;
	
	private PBDYZZTeamState state;

	private long matchStartTime;
	
	private long gameStartTime;
	
	private long cancelStartTime;
	
	private long lastTickTime;
	
	private long inviteTime;
	
	
	public DYZZTeamRoom() {
	
	}
	
	public DYZZTeamRoom(Player creater,int termId) {
		this.termId = termId;
		this.serverId = GsConfig.getInstance().getServerId();
		this.teamId = HawkUUIDGenerator.genUUID();
		this.leader = creater.getId();
		this.members.add(DYZZMember.valueOf(creater));
		this.createTime = HawkTime.getMillisecond();
		this.state = PBDYZZTeamState.DYZZ_TEAM_FREE;
	}
	
	
	
	public void ontick(){
		long curTime = HawkTime.getMillisecond();
		if(curTime < this.lastTickTime + 10 * 1000){
			return;
		}
		this.lastTickTime = curTime;
		//如果一直处于取消匹配状态
		if(this.state == PBDYZZTeamState.DYZZ_TEAM_MATCH_CANCEL){
			if(this.cancelStartTime + HawkTime.MINUTE_MILLI_SECONDS * 5 < curTime){
				this.state = PBDYZZTeamState.DYZZ_TEAM_FREE;
				DYZZService.getInstance().syncTeamRoomInfo(this);
				HawkLog.logPrintln("DYZZTeamRoom ontick cancelmatchGame,teamId: {},state:{},cancelTime:{}",
						this.getTeamId(),this.getState().getNumber(),this.cancelStartTime);
			}
		}
		//如果一直处于游戏状态
		if(this.state == PBDYZZTeamState.DYZZ_TEAM_GAMING){
			//如果房间一直处于对战状态，超过一定时间转换状态
			if(this.gameStartTime + HawkTime.MINUTE_MILLI_SECONDS * 30 < curTime){
				DYZZGameRoomData data = DYZZRedisData.getInstance().getDYZZGameData(this.termId, this.gameRoomId);
				if(data == null || (data.getLastActiveTime() + HawkTime.MINUTE_MILLI_SECONDS < HawkTime.getMillisecond())){
					this.state = PBDYZZTeamState.DYZZ_TEAM_FREE;
					DYZZService.getInstance().syncTeamRoomInfo(this);
					HawkLog.logPrintln("DYZZTeamRoom ontick DYZZ_TEAM_GAMING err,teamId: {},state:{},gameStartTime:{}",
							this.getTeamId(),this.getState().getNumber(),this.gameStartTime);
				}
			}
		}
	}
	
	
	public boolean joinTeamRoom(Player player){
		if(!this.inTeamRoom(player.getId())){
			this.members.add(DYZZMember.valueOf(player));
		}
		return true;
	}
	
	public boolean removePlayer(String playerId){
		DYZZMember member = this.getMember(playerId);
		if(member != null){
			this.members.remove(member);
			if(member.getPlayerId().equals(this.leader) && this.members.size() > 0){
				this.leader = this.members.get(0).getPlayerId();
			}
		}
		return true;
	}
	
	
	public boolean inTeamRoom(String playerId){
		return this.getMember(playerId) != null;
	}
	
	
	public boolean isLeader(String playerId){
		return this.leader.equals(playerId);
	}
	
	
	
	public DYZZMatchData createDYZZMatchData(){
		return new DYZZMatchData(this);
	}
	
	public DYZZMember getMember(String playerId){
		for(DYZZMember member : this.members){
			if(member.getPlayerId().equals(playerId)){
				return member;
			}
		}
		return null;
	}
	
	public boolean invitInCD(){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		if(HawkTime.getMillisecond() < this.inviteTime + cfg.getInviteCD() * 1000){
			return true;
		}
		return false;
	}
	
	
	
	public PBDYZZTeamInfo.Builder createPBDYZZTeamInfoBuilder(){
		PBDYZZTeamInfo.Builder builder = PBDYZZTeamInfo.newBuilder();
		builder.setTeamId(this.teamId);
		builder.setLeader(this.leader);
		for(DYZZMember member : this.members){
			builder.addMembers(member.genDYZZTeamMemberBuilder());
		}
		builder.setState(this.state);
		builder.setMatchTime(this.matchStartTime);
		builder.setCreateTime(this.createTime);
		return builder;
		
	}
	
	
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public List<DYZZMember> getMembers() {
		return members;
	}


	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public PBDYZZTeamState getState() {
		return state;
	}

	public void setState(PBDYZZTeamState state) {
		this.state = state;
	}


	public String getGameRoomId() {
		return gameRoomId;
	}


	public void setGameRoomId(String gameRoomId) {
		this.gameRoomId = gameRoomId;
	}

	public long getMatchStartTime() {
		return matchStartTime;
	}

	public void setMatchStartTime(long matchStartTime) {
		this.matchStartTime = matchStartTime;
	}

	public long getGameStartTime() {
		return gameStartTime;
	}

	public void setGameStartTime(long gameStartTime) {
		this.gameStartTime = gameStartTime;
	}

	public long getCancelStartTime() {
		return cancelStartTime;
	}

	public void setCancelStartTime(long cancelStartTime) {
		this.cancelStartTime = cancelStartTime;
	}


	public void setInviteTime(long inviteTime) {
		this.inviteTime = inviteTime;
	}


	
	
	
	
	
	
	
	

}
