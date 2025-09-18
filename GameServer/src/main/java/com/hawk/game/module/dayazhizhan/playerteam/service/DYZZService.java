package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZBattleRoomFameHallMember;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZExtraParam;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZGamer;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZBilingInformationMsg;
import com.hawk.game.module.dayazhizhan.marchserver.service.DYZZMatchService;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZShopRefreshTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;
import com.hawk.game.protocol.DYZZ.PBGuildInfo;
import com.hawk.game.protocol.DYZZ.PBPlayerInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZCancelTeamMatchData;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamNotify;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamRoomStateData;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamState;
import com.hawk.game.protocol.DYZZWar.PBDYZZTime;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarOpenInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZWarState;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.YQZZ.PBYQZZFirstControlBuildMail;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.LogUtil;

public class DYZZService  extends HawkAppObj {
	
	private DYZZActivityInfo activityInfo;
	
	private Map<String,DYZZTeamRoom> teams = new ConcurrentHashMap<>();
	
	private boolean activityOpen;
	
	private long lastTime;
	
	
	public DYZZService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	private static DYZZService instance = null;
	public static DYZZService getInstance() {
		return instance;
	}
	
	
	public boolean init(){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		this.activityInfo = DYZZRedisData.getInstance().loadDYZZServiceInfo();
		this.activityOpen = cfg.isOpen();
		return true;
	}
	
	@Override
	public boolean onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curTime - this.lastTime < 2000){
			return true;
		}
		this.lastTime = curTime;
		this.checkActivityOpen();
		this.checkStateChange();
		this.checkTeamRoom();
		return true;
	}
	
	
	public void checkActivityOpen(){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		boolean state = cfg.isOpen();
		if(state != this.activityOpen){
			this.activityOpen = state;
			this.boardcastInfo();
		}
	}
	
	
	public void checkTeamRoom(){
		for(DYZZTeamRoom room : this.teams.values()){
			room.ontick();
		}
	}
	

	

	private void checkStateChange() {
		DYZZActivityInfo newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			DYZZRedisData.getInstance().updateXZQServiceInfo(activityInfo);
		}
		PBDYZZWarState old_state = activityInfo.getState();
		PBDYZZWarState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从隐藏阶段开始轮询
		if (new_term != old_term) {
			old_state = PBDYZZWarState.DYZZ_HIDDEN;
			activityInfo.setTermId(new_term);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == PBDYZZWarState.DYZZ_HIDDEN) {
				old_state = PBDYZZWarState.DYZZ_SHOW;
				activityInfo.setState(old_state);
				onShow();
			} else if (old_state == PBDYZZWarState.DYZZ_SHOW) {
				old_state = PBDYZZWarState.DYZZ_OPEN;
				activityInfo.setState(old_state);
				onOpen();
			} else if (old_state == PBDYZZWarState.DYZZ_OPEN) {
				old_state = PBDYZZWarState.DYZZ_CLOSE;
				activityInfo.setState(old_state);
				onEnd();
			} else if (old_state == PBDYZZWarState.DYZZ_CLOSE) {
				old_state = PBDYZZWarState.DYZZ_HIDDEN;
				activityInfo.setState(old_state);
				onHidden();
			}
		}

		if (needUpdate) {
			activityInfo = newInfo;
			DYZZRedisData.getInstance().updateXZQServiceInfo(activityInfo);
			boardcastInfo();
		}

	}
	
	
	
	

	private DYZZActivityInfo calcInfo() {
		ConfigIterator<DYZZTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(DYZZTimeCfg.class);
		long now = HawkTime.getMillisecond();
		DYZZTimeCfg cfg = null;
		for (DYZZTimeCfg timeCfg : its) {
			if (now > timeCfg.getShowTimeValue()) {
				cfg = timeCfg;
			}
		}
		// 没有可供开启的配置
		if (cfg == null) {
			return new DYZZActivityInfo();
		}
		int termId = 0;
		PBDYZZWarState state = PBDYZZWarState.DYZZ_HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showTime = cfg.getShowTimeValue();
			long startTime = cfg.getStartTimeValue();
			long endTime = cfg.getEndTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showTime) {
				state = PBDYZZWarState.DYZZ_HIDDEN;
			}
			if (now >= showTime && now < startTime) {
				state = PBDYZZWarState.DYZZ_SHOW;
			}
			if (now >= startTime && now < endTime) {
				state = PBDYZZWarState.DYZZ_OPEN;
			}
			if (now >= endTime && now < hiddenTime) {
				state = PBDYZZWarState.DYZZ_CLOSE;
			}
			if (now >= hiddenTime) {
				state = PBDYZZWarState.DYZZ_HIDDEN;
			}
		}
		DYZZActivityInfo info = new DYZZActivityInfo();
		info.setTermId(termId);
		info.setState(state);
		return info;
	}
	
	
	public void clear(){
		this.teams.clear();
	}
	
	public void onShow(){
		this.teams.clear();
	}
	
	public void onOpen(){
		this.teams.clear();
	}
	
	public void onEnd(){
		
	}
	
	public void onHidden(){
		this.teams.clear();
	}
	
	
	
	
	
	public void syncTeamRoomInfo(DYZZTeamRoom team){
		List<DYZZMember> members = team.getMembers();
		for(DYZZMember member: members){
			String playerId = member.getPlayerId();
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if(player!= null){
				this.syncStateInfo(player);
			}
		}
	}
	
	
	
	public void boardcastInfo(){
		Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : players) {
			syncOpenInfo(player);
			syncStateInfo(player);
		}
	}
	
	
	public void syncOpenInfo(Player player){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		PBDYZZWarOpenInfo.Builder builder = PBDYZZWarOpenInfo.newBuilder();
		if(cfg.isOpen()){
			builder.setOpen(1);
		}else{
			builder.setOpen(0);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_OPEN_INFO_RESP_VALUE, builder));
	}
	
	public void syncStateInfo(Player player){
		PBDYZZWarInfo.Builder builder = this.createPBDYZZWarInfoBuilder(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_INFO_RESP_VALUE, builder));
	}
	

	public PBDYZZWarInfo.Builder createPBDYZZWarInfoBuilder(Player player){
		PBDYZZWarInfo.Builder builder = PBDYZZWarInfo.newBuilder();
		int termId = this.getDYZZWarTerm();
		PBDYZZWarState state = this.getDYZZWarState();
		builder.setTermId(termId);
		builder.setState(state);
		if(termId != 0){
			int winCount = DYZZRedisData.getInstance().getDYZZWincountToday(player.getId());
			DYZZTimeCfg timecfg = HawkConfigManager.getInstance().getConfigByKey(DYZZTimeCfg.class, termId);
			builder.setTime(this.createPBDYZZTimeBuilder(timecfg));
			builder.setFistReward(winCount);
		}
		DYZZTeamRoom team = this.getPlayerTeamRoom(player.getId());
		if(team != null){
			builder.setTeam(team.createPBDYZZTeamInfoBuilder());
		}
		return builder;
	}
	
	public PBDYZZTime.Builder createPBDYZZTimeBuilder(DYZZTimeCfg cfg){
		PBDYZZTime.Builder builder = PBDYZZTime.newBuilder();
		builder.setShowTime(cfg.getShowTimeValue());
		builder.setOpenTime(cfg.getStartTimeValue());
		builder.setCloseTime(cfg.getEndTimeValue());
		builder.setHiddenTime(cfg.getHiddenTimeValue());
		return builder;
	}
	
	
	public int getDYZZWarTerm(){
		return this.activityInfo.getTermId();
	}
	public PBDYZZWarState getDYZZWarState(){
		return this.activityInfo.getState();
	}
	
	
	/**
	 * 创建房间
	 * @param player
	 * @return
	 */
	public void playerCreateTeamRoom(Player player){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		boolean open = cfg.isOpen();
		if(!open){
			return;
		}
		if(player.isCsPlayer()){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_IN_CROSS_VALUE, 0);
			return;
		}
		if(player.isInDungeonMap()){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.Error.PLAYER_IN_INSTANCE_VALUE, 0);
			return;
		}
		if(this.getDYZZWarState() != PBDYZZWarState.DYZZ_OPEN){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_IN_MATCH_TIME_VALUE, 0);
			return;
		}
		if(player.getCityLevel() < cfg.getFortLevel()){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_LEVEL_LIMIT_VALUE, 0);
			return;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_IN_WAR_FEVER_VALUE, 0);
			return;
		}
		DYZZTeamRoom team = this.getPlayerTeamRoom(player.getId());
		if(team != null){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_HAS_IN_TEAM_VALUE, 0);
			HawkLog.logPrintln("DYZZ playerCreateTeamRoom error,playerId: {},teamId: {}",player.getId(),team.getTeamId());
			return;
		}
		int termId = this.activityInfo.getTermId();
		DYZZTeamRoom newTeam = new DYZZTeamRoom(player,termId);
		this.teams.put(newTeam.getTeamId(), newTeam);
		PBDYZZWarInfo.Builder builder = this.createPBDYZZWarInfoBuilder(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_CREATE_TEAM_RESP_VALUE, builder));
		DYZZMember creater = newTeam.getMember(player.getId());
		HawkLog.logPrintln("DYZZ playerCreateTeamRoom sucess,playerId: {},teamId: {},creater:{}",player.getId(),newTeam.getTeamId(),creater.serializ());
		
		DungeonRedisLog.log(player.getId(), "{}", newTeam.getTeamId());
	}
	
	
	/**
	 * 发送联盟邀请
	 * @param player
	 */
	public void inviteTeam(Player player){
		if(this.getDYZZWarState() != PBDYZZWarState.DYZZ_OPEN){
			player.sendError(HP.code2.DYZZ_CREATE_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_IN_MATCH_TIME_VALUE, 0);
			return;
		}
		DYZZTeamRoom room = this.getPlayerTeamRoom(player.getId());
		if(room == null){
			player.sendError(HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(!player.hasGuild()){
			player.sendError(HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_INIT_NO_GUILD_VALUE, 0);
			return;
		}
		if(room.getState() != PBDYZZTeamState.DYZZ_TEAM_FREE){
			HawkLog.logPrintln("DYZZ inviteTeam error,playerId: {},teamId: {},state:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber());
			return;
		}
		if(room.invitInCD()){
			player.sendError(HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_INIT_IN_CD_VALUE, 0);
			return;
		}
		room.setInviteTime(HawkTime.getMillisecond());
		ChatParames chatParames = ChatParames.newBuilder()
				.setChatType(ChatType.GUILD_HREF)
				.setKey(Const.NoticeCfgId.DYZZ_GUILD_INVIT)
				.setPlayer(player)
				.addParms(room.getTeamId())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParames);
		player.responseSuccess(HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE);
	}
	
	
	/**
	 * 加入房间
	 * @param player
	 * @param roomId
	 * @return
	 */
	public void joinTeamRoom(Player player,String roomId){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		boolean open = cfg.isOpen();
		if(!open){
			return;
		}
		if(player.isCsPlayer()){
			player.sendError(HP.code2.DYZZ_GUILD_JOIN_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_IN_CROSS_VALUE, 0);
			return;
		}
		if(player.isInDungeonMap()){
			player.sendError(HP.code2.DYZZ_GUILD_JOIN_TEAM_REQ_VALUE,
					Status.Error.PLAYER_IN_INSTANCE_VALUE, 0);
			return;
		}
		if(this.getDYZZWarState() != PBDYZZWarState.DYZZ_OPEN){
			player.sendError(HP.code2.DYZZ_GUILD_JOIN_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_IN_MATCH_TIME_VALUE, 0);
			return;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			player.sendError(HP.code2.DYZZ_GUILD_JOIN_TEAM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_IN_WAR_FEVER_VALUE, 0);
			return;
		}
		DYZZTeamRoom team = this.getPlayerTeamRoom(player.getId());
		if(team != null){
			player.sendError(HP.code2.DYZZ_JOIN_ROOM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_HAS_IN_TEAM_VALUE, 0);
			return;
		}
		DYZZTeamRoom targetTeam = this.getDYZZTeamRoom(roomId);
		if(targetTeam == null){
			player.sendError(HP.code2.DYZZ_GUILD_INVITE_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(targetTeam.getState() != PBDYZZTeamState.DYZZ_TEAM_FREE){
			if(targetTeam.getState() == PBDYZZTeamState.DYZZ_TEAM_MATCHING){
				player.sendError(HP.code2.DYZZ_JOIN_ROOM_REQ_VALUE,
						Status.DYZZError.DYZZ_WAR_JOIN_TEAM_IN_MATCHING_VALUE, 0);
			}
			if(targetTeam.getState() == PBDYZZTeamState.DYZZ_TEAM_GAMING){
				player.sendError(HP.code2.DYZZ_JOIN_ROOM_REQ_VALUE,
						Status.DYZZError.DYZZ_WAR_JOIN_TEAM_IN_GAMING_VALUE, 0);
			}
			return;
		}
		if(targetTeam.getMembers().size() >= cfg.getTeamMemberCount()){
			player.sendError(HP.code2.DYZZ_JOIN_ROOM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_JOIN_TEAM_FULL_VALUE, 0);
			return;
		}
		if(player.getCityLevel() < cfg.getFortLevel()){
			player.sendError(HP.code2.DYZZ_JOIN_ROOM_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_LEVEL_LIMIT_VALUE, 0);
			return;
		}
		targetTeam.joinTeamRoom(player);
		//加入成功
		PBDYZZWarInfo.Builder builder = this.createPBDYZZWarInfoBuilder(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_GUILD_JOIN_TEAM_RESP_VALUE, builder));
		DYZZMember selfMember = targetTeam.getMember(player.getId());
		HawkLog.logPrintln("DYZZ joinTeamRoom sucess,playerId: {},teamId: {},memeber:{}",player.getId(),targetTeam.getTeamId(),selfMember.serializ());
		//同步房间其他人
		for(DYZZMember member: targetTeam.getMembers()){
			String playerId = member.getPlayerId();
			if(playerId.equals(player.getId())){
				continue;
			}
			Player teamPlayer = GlobalData.getInstance().getActivePlayer(playerId);
			if(teamPlayer!= null){
				this.syncStateInfo(teamPlayer);
			}
		}
		DungeonRedisLog.log(player.getId(), "teamId {}", targetTeam.getTeamId());
		return;
	}
	
	
	
	/**
	 * 移除房间成员
	 * @param player
	 * @param roomId
	 * @param memberId
	 * @return
	 */
	public void removeTeamMember(Player player,String memberId){
		DYZZTeamRoom team = this.getDYZZTeamRoomByLeader(player.getId());
		if(Objects.isNull(team)){
			player.sendError(HP.code2.DYZZ_KICK_OUT_MEMBER_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(!team.getLeader().equals(player.getId())){
			player.sendError(HP.code2.DYZZ_KICK_OUT_MEMBER_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_LEADER_VALUE, 0);
			return;
		}
		boolean rlt = team.removePlayer(memberId);
		if(rlt){
			Player delPlayer = GlobalData.getInstance().getActivePlayer(memberId);
			if(Objects.nonNull(delPlayer)){
				PBDYZZWarInfo.Builder builder = this.createPBDYZZWarInfoBuilder(delPlayer);
				delPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_KICKED_OUT_RESP_VALUE, builder));
			}
			
			this.syncTeamRoomInfo(team);
			HawkLog.logPrintln("DYZZ removeTeamMember sucess,playerId: {},teamId: {},memberId:{}",player.getId(),team.getTeamId(),memberId);
			DungeonRedisLog.log(memberId, " {}", team.getTeamId());
		}
	}
	
	/**
	 * 退出房间
	 * @param player
	 */
	public void quitTeam(Player player){
		DYZZTeamRoom team = this.getPlayerTeamRoom(player.getId());
		if(Objects.isNull(team)){
			player.sendError(HP.code2.DYZZ_KICK_OUT_MEMBER_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(team.getState() != PBDYZZTeamState.DYZZ_TEAM_FREE){
			HawkLog.logPrintln("DYZZ quitTeam error,playerId: {},teamId: {},state:{}",
					player.getId(),team.getTeamId(),team.getState().getNumber());
			return;
		}
		boolean rlt = team.removePlayer(player.getId());
		if(rlt){
			Player delPlayer = GlobalData.getInstance().getActivePlayer(player.getId());
			if(Objects.nonNull(delPlayer)){
				this.syncStateInfo(player);
			}
			HawkLog.logPrintln("DYZZ quitTeam sucess,playerId: {},teamId: {},state:{}",
					player.getId(),team.getTeamId(),team.getState().getNumber());
			this.syncTeamRoomInfo(team);
			if(team.getMembers().size() <= 0){
				this.teams.remove(team.getTeamId());
				HawkLog.logPrintln("DYZZ quitTeam sucess,team delete,playerId: {},teamId: {},state:{}",
						player.getId(),team.getTeamId(),team.getState().getNumber());
			}
			DungeonRedisLog.log(player.getId(), " {}", team.getTeamId());
		}
	}
	
	
	/**
	 * 开始匹配
	 * @param player
	 * @return
	 */
	public void matchGame(Player player){
		DYZZTeamRoom room = this.getDYZZTeamRoomByLeader(player.getId());
		if(Objects.isNull(room)){
			player.sendError(HP.code2.DYZZ_MATCH_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(this.getDYZZWarState() != PBDYZZWarState.DYZZ_OPEN){
			player.sendError(HP.code2.DYZZ_MATCH_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NOT_IN_MATCH_TIME_VALUE, 0);
			HawkLog.logPrintln("DYZZ matchGame warstate error,playerId: {},teamId: {},state:{},activitystate:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber(),this.getDYZZWarState().getNumber());
			return;
		}
		if(room.getState() != PBDYZZTeamState.DYZZ_TEAM_FREE){
			HawkLog.logPrintln("DYZZ matchGame error,playerId: {},teamId: {},state:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber());
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime < room.getMatchStartTime() + 20000){
			HawkLog.logPrintln("DYZZ matchGame error match time limit,playerId: {},teamId: {},state:{},matchTime:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber(),room.getMatchStartTime());
			return;
		}
		//设置20秒的冷却
		room.setMatchStartTime(curTime);
		DYZZMatchData matchData = room.createDYZZMatchData();
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_REQ_VALUE, 
				matchData.genDYZZTeamMatchData());
		String toServerId = DYZZMatchService.getInstance().getMatchServerId(room.getTermId());
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, null);
		
		HawkLog.logPrintln("DYZZ matchGame match ,playerId: {},teamId: {},state:{},matchTime:{},matchServer:{}",
				player.getId(),room.getTeamId(),room.getState().getNumber(),room.getMatchStartTime(),toServerId);
		//记录行为日志
		for(DYZZMember member : room.getMembers()){
			DungeonRedisLog.log(member.getPlayerId(), " {}, leader：{}", room.getTeamId(),room.getLeader());
		}
	}
	
	
	/**
	 * 取消匹配
	 * @param player
	 * @return
	 */
	public void cancelmatchGame(Player player){
		DYZZTeamRoom room = this.getPlayerTeamRoom(player.getId());
		if(Objects.isNull(room)){
			player.sendError(HP.code2.DYZZ_CANCEL_MATCH_REQ_VALUE,
					Status.DYZZError.DYZZ_WAR_NO_TEAM_VALUE, 0);
			return;
		}
		if(room.getState() != PBDYZZTeamState.DYZZ_TEAM_MATCHING){
			HawkLog.logPrintln("DYZZ cancelmatchGame state error,playerId: {},teamId: {},state:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber());
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime < room.getCancelStartTime() + 20000){
			HawkLog.logPrintln("DYZZ cancelmatchGame error time limit ,playerId: {},teamId: {},state:{},cancelTime:{}",
					player.getId(),room.getTeamId(),room.getState().getNumber(),room.getCancelStartTime());
			return;
		}
		//设置20秒的冷却
		room.setCancelStartTime(curTime);
		PBDYZZCancelTeamMatchData.Builder cancelData = PBDYZZCancelTeamMatchData.newBuilder();
		cancelData.setServerId(room.getServerId());
		cancelData.setTeamId(room.getTeamId());
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(CHP.code.CROSS_DDZY_CANCEL_TEAM_MATCHING_REQ_VALUE, cancelData);
		String toServerId = DYZZMatchService.getInstance().getMatchServerId(room.getTermId());
		CrossProxy.getInstance().sendNotify(hawkProtocol, toServerId, null);
	}
	
	
	
	
	
	/**
	 * 玩家是否在队伍中
	 * @param playerId
	 * @return
	 */
	public DYZZTeamRoom getPlayerTeamRoom(String playerId){
		for(DYZZTeamRoom room : this.teams.values()){
			if(room.inTeamRoom(playerId)){
				return room;
			}
		}
		return null;
	}
	
	@ProtocolHandler(code = CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE)
	public void onmatchGameCallBack(HawkProtocol hawkProtocol){
		PBDYZZTeamRoomStateData data = hawkProtocol.parseProtocol(PBDYZZTeamRoomStateData.getDefaultInstance());
		HawkLog.logPrintln("DYZZ onmatchGameCallBack message,teamId: {},state:{},gameId:{}",
				data.getTeamId(),data.getState().getNumber(),data.getGameId());
		DYZZTeamRoom team = this.getDYZZTeamRoom(data.getTeamId());
		if(team == null){
			HawkLog.logPrintln("DYZZ onmatchGameCallBack message,team null,teamId: {},state:{},gameId:{}",
					data.getTeamId(),data.getState().getNumber(),data.getGameId());
			return;
		}
		long curTime = HawkTime.getMillisecond();
		PBDYZZTeamState state = team.getState();
		HawkLog.logPrintln("DYZZ onmatchGameCallBack message,team current state,teamId:{},state:{}",
				team.getTeamId(),team.getState().getNumber());
		switch (state) {
		case DYZZ_TEAM_FREE:
			if(data.getState() == PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_MATCHING){
				team.setState(PBDYZZTeamState.DYZZ_TEAM_MATCHING);
				HawkLog.logPrintln("DYZZ onmatchGameCallBack message DYZZ_TEAM_NOTIFY_MATCHING,"
						+ "team goto matching state,teamId:{},state:{}",team.getTeamId(),team.getState().getNumber());
			}
			break;
		case DYZZ_TEAM_MATCHING:
			if(data.getState() == PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_GAME_START){
				//匹配完成，进场开打
				String gameId = data.getGameId();
				DYZZGameRoomData game = DYZZRedisData.getInstance().getDYZZGameData(team.getTermId(), gameId);
				if(game == null){
					HawkLog.logPrintln("DYZZ onmatchGameCallBack err,game null,gameId:{}",gameId);
					return;
				}
				team.setState(PBDYZZTeamState.DYZZ_TEAM_GAMING);
				team.setGameRoomId(gameId);
				team.setGameStartTime(curTime);
				HawkLog.logPrintln("DYZZ onmatchGameCallBack message DYZZ_TEAM_NOTIFY_GAME_START,"
						+ "team goto matching state,teamId:{},state:{},gameId:{}",team.getTeamId(),team.getState().getNumber(),team.getGameRoomId());
				//创建房间
				this.createGame(team.getTermId(),gameId);
				//匹配Tlog
				long matchTime = curTime - team.getMatchStartTime();
				LogUtil.dyzzMatchResult(team.getTermId(), team.getTeamId(), gameId, matchTime);
				for(DYZZMember member : team.getMembers()){
					DungeonRedisLog.log(member.getPlayerId(), " {},leader：{}, gameRoomId:{}, gameServerID:{}", 
							team.getTeamId(),team.getLeader(),team.getGameRoomId(),game.getServerId());
				}
			}
			if(data.getState() == PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_CANCEL_MATCHING){
				team.setState(PBDYZZTeamState.DYZZ_TEAM_MATCH_CANCEL);
				HawkLog.logPrintln("DYZZ onmatchGameCallBack message DYZZ_TEAM_NOTIFY_CANCEL_MATCHING,"
						+ "team goto matching state,teamId:{},state:{}",team.getTeamId(),team.getState().getNumber());
			}
			break;
		case DYZZ_TEAM_MATCH_CANCEL:
			if(data.getState() == PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_CANCEL_SUCESS){
				team.setState(PBDYZZTeamState.DYZZ_TEAM_FREE);
				team.setMatchStartTime(0);
				team.setCancelStartTime(0);
				team.setGameStartTime(0);
				team.setGameRoomId("");
				HawkLog.logPrintln("DYZZ onmatchGameCallBack message DYZZ_TEAM_NOTIFY_CANCEL_SUCESS,"
						+ "team goto matching state,teamId:{},state:{}",team.getTeamId(),team.getState().getNumber());
			}
			break;
		case DYZZ_TEAM_GAMING:
			//打完了
			if(data.getState() == PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_GAME_OVER && 
					data.hasGameId() && data.getGameId().equals(team.getGameRoomId())){
				//获取战斗数据
				PBDYZZGameInfoSync.Builder battleInfo = DYZZRedisData.getInstance()
						.getDYZZBilingInformationData(team.getTermId(),team.getGameRoomId());
				//计算战后
				int termId = team.getTermId();
				String gameRoom =  team.getGameRoomId();
				this.calBattleInfo(termId, gameRoom, battleInfo.build());
				team.setState(PBDYZZTeamState.DYZZ_TEAM_FREE);
				team.setMatchStartTime(0);
				team.setCancelStartTime(0);
				team.setGameStartTime(0);
				team.setGameRoomId("");
				//打完删除房间
				this.teams.remove(team.getTeamId());
				HawkLog.logPrintln("DYZZ onmatchGameCallBack message DYZZ_TEAM_NOTIFY_GAME_OVER,"
						+ "team goto matching state,teamId:{},state:{},gameId:{}",team.getTeamId(),team.getState().getNumber(),data.getGameId());
			}
			break;
		default:
			HawkLog.logPrintln("DYZZ onmatchGameCallBack message workout,teamId:{},state:{},gameId:{}",team.getTeamId(),team.getState().getNumber(),team.getGameRoomId());
			break;
		}
		this.syncTeamRoomInfo(team);
	}
	
	
	private void createGame(int termId,String gameId){
		String serverId = GsConfig.getInstance().getServerId();
		HawkLog.logPrintln("DYZZ createGame,gameId:{}",gameId);
		DYZZGameRoomData game = DYZZRedisData.getInstance().getDYZZGameData(termId, gameId);
		if(game == null){
			HawkLog.logPrintln("DYZZ createGame err,game null,gameId:{}",gameId);
			return;
		}
		HawkLog.logPrintln("DYZZ createGame,gameId:{},serverId:{},state:{}",gameId,game.getServerId(),game.getState().getNumber());
		//所有匹配队伍
		List<DYZZMatchData> teams = new ArrayList<>();
		teams.addAll(game.getCampATeams());
		teams.addAll(game.getCampBTeams());
		//创建房间
		if(game.getServerId().equals(serverId) &&
				game.getState() == PBDYZZGameRoomState.DYZZ_GAME_INIT){
			HawkLog.logPrintln("DYZZ createGame, local server,gameId:{},serverId:{},state:{}",
					gameId,game.getServerId(),game.getState().getNumber());
			//创建玩家数据
			List<DYZZPlayerData> playerDatas = new ArrayList<>();
			Set<String> playerIds = new HashSet<>();
			for(DYZZMatchData team : teams){
				for(DYZZMember member : team.getMembers()){
					DYZZPlayerData playerData = new DYZZPlayerData(termId,gameId,member);
					playerDatas.add(playerData);
					playerIds.add(playerData.getPlayerId());
					HawkLog.logPrintln("DYZZ DYZZPlayerData sucess,playerId:{},serverId:{}",
							playerData.getPlayerId(),playerData.getServerId());
				}
			}
			//创建房间
			long curTime = HawkTime.getMillisecond();
			int seasonTerm = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
			List<DYZZBattleRoomFameHallMember> fameHallMembers = DYZZSeasonService.getInstance()
					.getFameHallMemeber();
			DYZZExtraParam param = new DYZZExtraParam();
			param.setBattleId(game.getGameId());
			//赛季ID
			param.setSeasonTerm(seasonTerm);
			//赛季名人堂
			param.setFameHallMembers(fameHallMembers);
			for(DYZZMatchData team : game.getCampATeams()){
				for(DYZZMember member : team.getMembers()){
					DYZZGamer gamer = member.getDYZZGamer();
					gamer.setCamp(DYZZCAMP.A);
					param.addGamer(gamer);
				}
			}
			for(DYZZMatchData team : game.getCampBTeams()){
				for(DYZZMember member : team.getMembers()){
					DYZZGamer gamer = member.getDYZZGamer();
					gamer.setCamp(DYZZCAMP.B);
					param.addGamer(gamer);
				}
			}
			
			boolean rlt = DYZZRoomManager.getInstance().creatNewBattle(curTime, param);
			if(rlt){
				//修改状态数据
				game.setState(PBDYZZGameRoomState.DYZZ_GAME_GAMING);
				game.setStartTime(curTime);
				game.setLastActiveTime(curTime);
				DYZZRedisData.getInstance().saveDYZZGameData(termId, game);
				HawkLog.logPrintln("DYZZ createGame sucess, local server,gameId:{},serverId:{},state:{}",
						gameId,game.getServerId(),game.getState().getNumber());
				//保存玩家数据
				DYZZRedisData.getInstance().updateDYZZPlayerData(playerDatas);
			}
			
		}
		//添加跨服联盟数据
		Map<String,DYZZGuildData> dyzzGuildDatas = new HashMap<>();
		for(DYZZMatchData team : teams){
			if(serverId.equals(team.getServerId())){
				String leader = team.getLeaderId();
				String guildId = GuildService.getInstance().getPlayerGuildId(leader);
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					continue;
				}
				if(dyzzGuildDatas.containsKey(guildId)){
					continue;
				}
				long power = GuildService.getInstance().getGuildBattlePoint(guildId);
				DYZZGuildData guildData = new DYZZGuildData();
				guildData.setTermId(termId);
				guildData.setId(guildId);
				guildData.setName(guildObj.getName());
				guildData.setTag(guildObj.getTag());
				guildData.setLeaderId(guildObj.getLeaderId());
				guildData.setLeaderName(guildObj.getLeaderName());
				guildData.setFlag(guildObj.getFlagId());
				guildData.setServerId(serverId);
				guildData.setPower(power);
				dyzzGuildDatas.put(guildId,guildData);
				HawkLog.logPrintln("DYZZ createGame save guildData,gameId:{},serverId:{},state:{}",
						gameId,game.getServerId(),game.getState().getNumber());
			}
		}
		DYZZRedisData.getInstance().updateDYZZGuildData(termId, dyzzGuildDatas.values());
	}
	
	
	
	@MessageHandler
	private void onBattleFinish(DYZZBilingInformationMsg msg) {
		String gameId = msg.getRoomId();
		int termId = this.getDYZZWarTerm();
		DYZZGameRoomData roomData = DYZZRedisData.getInstance().getDYZZGameData(termId, gameId);
		if(roomData == null){
			HawkLog.logPrintln("DYZZ onBattleFinish msg,game null,gameId:{},",gameId);
			return;
		}
		HawkLog.logPrintln("DYZZ onBattleFinish msg,gameId:{},",gameId,roomData.getState().getNumber());
		roomData.setState(PBDYZZGameRoomState.DYZZ_GAME_OVER);
		DYZZRedisData.getInstance().saveDYZZGameData(termId, roomData);
		DYZZRedisData.getInstance().saveDYZZBilingInformationData(termId, gameId, msg.getLastSyncpb());
		PBDYZZGameInfoSync info = msg.getLastSyncpb();
		//玩家数据
		Map<String,PBPlayerInfo> playerMap = this.getPBPlayerInfoMap(info);
		//结束时基地血量
		Map<Integer,Integer> baseHpMap = this.getCampBaseHpMap(info);
		//添加记录
		Set<String> playerSet = new HashSet<>();
		playerSet.addAll(playerMap.keySet());
		Map<String,DYZZPlayerScore> scoreMap = DYZZRedisData.getInstance().getDYZZPlayerScore(playerSet);
		//记录数据
		this.calRecordData(info, playerMap, scoreMap);
		//添加战斗记录
		DYZZRedisData.getInstance().addPlayerBattleHistory(playerSet, info);
		//更新记录
		DYZZRedisData.getInstance().updateDYZZPlayerScore(scoreMap.values());
		//通知游戏结束
		List<DYZZMatchData> matchList = new ArrayList<>();
		matchList.addAll(roomData.getCampATeams());
		matchList.addAll(roomData.getCampBTeams());
		for(DYZZMatchData teamData : matchList){
			PBDYZZTeamRoomStateData.Builder builder = PBDYZZTeamRoomStateData.newBuilder();
			builder.setTeamId(teamData.getTeamId());
			builder.setState(PBDYZZTeamNotify.DYZZ_TEAM_NOTIFY_GAME_OVER);
			builder.setGameId(gameId);
			CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.CROSS_DDZY_TEAM_MATCHING_STATE_RESP_VALUE, builder), 
					teamData.getServerId(), "");
		}
		//玩家结算Tlog
		long curTime = HawkTime.getMillisecond();
		long battleTime = curTime - roomData.getStartTime();
		for(PBPlayerInfo pinfo : playerMap.values()){
			int camp = pinfo.getCamp();
			int baseHp = baseHpMap.getOrDefault(camp,-1);
			int winCount = 0;
			int lossCount = 0;
			DYZZPlayerScore score = scoreMap.get(pinfo.getPlayerId());
			if(score != null){
				winCount = score.getWinCount();
				lossCount = score.getLossCount();
			}
			LogUtil.dyzzBattleResult(pinfo.getPlayerId(),termId, gameId, battleTime, pinfo.getKillCount(), pinfo.getHurtCount(), 
					pinfo.getCollectOrder(),camp,baseHp,winCount,lossCount);
		}
	}
	
	/**
	 * 添加记录数据
	 * @param info
	 * @param playerMap
	 * @param scoreMap
	 */
	private void calRecordData(PBDYZZGameInfoSync info,Map<String,PBPlayerInfo> playerMap,Map<String,DYZZPlayerScore> scoreMap){
		for(DYZZPlayerScore score : scoreMap.values()){
			PBPlayerInfo playerInfo = playerMap.get(score.getPlayerId());
			if(playerInfo == null){
				continue;
			}
			boolean win = playerInfo.getCamp() == info.getWinCamp();
			boolean mvp = playerInfo.getMvp() > 0;
			if(win){
				int winCount = score.getWinCount() + 1;
				score.setWinCount(winCount);
				if(mvp){
					int mvpCount = score.getMvpCount() + 1;
					score.setMvpCount(mvpCount);
				}
			}else{
				int lossCount = score.getLossCount() + 1;
				score.setLossCount(lossCount);
				if(mvp){
					int smvpCount = score.getSmvpCount() + 1;
					score.setSmvpCount(smvpCount);
				}
			}
		}
	}
	
	
	/**
	 * 计算战后
	 */
	private void calBattleInfo(int termId,String gameId,PBDYZZGameInfoSync battleInfo){
		//赛季计算
		try {
			String serverId = GsConfig.getInstance().getServerId();
			long deals = DYZZRedisData.getInstance().getDYZZResultDeal(serverId, gameId);
			if(deals >1){
				return;
			}
			DYZZSeasonService.getInstance().onBattleFinish(termId,gameId,battleInfo);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	private Map<String,PBPlayerInfo> getPBPlayerInfoMap(PBDYZZGameInfoSync info){
		Map<String,PBPlayerInfo> map = new HashMap<>();
		for(PBPlayerInfo playerInfo :info.getPlayerInfoList()){
			map.put(playerInfo.getPlayerId(), playerInfo);
		}
		return map;
	}
	
	private Map<Integer,Integer> getCampBaseHpMap(PBDYZZGameInfoSync info){
		Map<Integer,Integer> map = new HashMap<>();
		for(PBGuildInfo guildInfo :info.getGuildInfoList()){
			map.put(guildInfo.getCamp(), guildInfo.getBaseHP());
		}
		return map;
	}
	
	
	/**
	 * 删除房间
	 * @param roomId
	 * @return
	 */
	public DYZZTeamRoom delTeamRoom(String roomId){
		return this.teams.remove(roomId);
	}
	
	
	
	/**
	 * 获取房间
	 * @param roomId
	 * @return
	 */
	public DYZZTeamRoom getDYZZTeamRoom(String roomId){
		return this.teams.get(roomId);
	}
	
	
	/**
	 * 获取房间
	 * @param leaderId
	 * @return
	 */
	public DYZZTeamRoom getDYZZTeamRoomByLeader(String leaderId){
		for(DYZZTeamRoom room : this.teams.values()){
			if(room.isLeader(leaderId)){
				return room;
			}
		}
		return null;
	}
	
	
	
	
	/**
	 * 获取赛博商店redisKey的赛季参数
	 * @return
	 */
	public int getDYZZShopTerm() {
		DYZZShopRefreshTimeCfg cfg = getNearbyCfg();
		if (cfg != null) {
			return cfg.getId();
		}
		return 0;
	}
	

	/**
	 * 获取当前最近一期的商店刷新时间配置
	 * @return
	 */
	public DYZZShopRefreshTimeCfg getNearbyCfg() {
		ConfigIterator<DYZZShopRefreshTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(DYZZShopRefreshTimeCfg.class);
		long now = HawkTime.getMillisecond();
		DYZZShopRefreshTimeCfg cfg = null;
		for (DYZZShopRefreshTimeCfg timeCfg : its) {
			if (now > timeCfg.getRefreshTimeValue()) {
				cfg = timeCfg;
			}
		}
		return cfg;
	}
	
	
	/**
	 * 获取下一次商店兑换重置时间
	 * @return
	 */
	public DYZZShopRefreshTimeCfg getNextCfg(){
		ConfigIterator<DYZZShopRefreshTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(DYZZShopRefreshTimeCfg.class);
		long now = HawkTime.getMillisecond();
		for (DYZZShopRefreshTimeCfg timeCfg : its) {
			if (now < timeCfg.getRefreshTimeValue()) {
				return timeCfg;
			}
		}
		return null;
	}
	
	
	public boolean joinRoom(Player player){
		int termId = this.getDYZZWarTerm();
		if(this.getDYZZWarState() != PBDYZZWarState.DYZZ_OPEN && 
				this.getDYZZWarState() != PBDYZZWarState.DYZZ_CLOSE){
			return false;
		}
		HawkTuple2<DYZZGameRoomData, Integer> tuple = getPlayerRoomData(player);
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			HawkLog.logPrintln("DYZZWarService enter room error, errorCode: {}, playerId: {}, termId: {}", status, player.getId(), termId);
			return false;
		}
		DYZZGameRoomData roomData = tuple.first;
		if (roomData == null) {
			HawkLog.logPrintln("DYZZWarService enter room error, room not esixt, playerId: {}, termId: {}", player.getId(), termId);
			return false;
		}
		String roomId = roomData.getGameId();
		if (!DYZZRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("DYZZWarService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getGameId());
			return false;
		}
		DYZZPlayerData dyzzPlayer = DYZZRedisData.getInstance().getDYZZPlayerData(termId, player.getId());
		if (dyzzPlayer == null) {
			HawkLog.logPrintln("DYZZWarService enter room error, dyzzPlayerData is null, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getServerId(), player.getGuildId());
			return false;
		}
		if(!dyzzPlayer.getGameId().equals(roomId)){
			HawkLog.logPrintln("DYZZWarService enter room error, dyzzPlayerData gameId error, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {},dyzzPlayerGameId:{}", player.getId(),
					termId, roomId, roomData.getServerId(), player.getGuildId(),dyzzPlayer.getGameId());
			return false;
		}

		if (dyzzPlayer.getQuitTime()> 0) {
			HawkLog.logPrintln("DYZZWarService enter room error, has entered, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getServerId(), player.getGuildId());
			return false;
		}
		if (!DYZZRoomManager.getInstance().joinGame(roomData.getGameId(), player)) {
			HawkLog.logPrintln("DYZZWarService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					termId, roomId, roomData.getServerId(), player.getGuildId());
			return false;
		}
		dyzzPlayer.setEnterTime(HawkTime.getMillisecond());
		DYZZRedisData.getInstance().updateDYZZPlayerData(dyzzPlayer);
		DungeonRedisLog.log(player.getId(), "{}", roomId);
		return true;
	}
	
	
	
	/**
	 * 根据玩家获取匹配房间信息
	 * @param player
	 * @return
	 */
	public HawkTuple2<DYZZGameRoomData, Integer> getPlayerRoomData(Player player) {
		String playerId = player.getId();
		int termId = this.getDYZZWarTerm();
		DYZZPlayerData dyzzPlayer = DYZZRedisData.getInstance().getDYZZPlayerData(termId, playerId);
		if (dyzzPlayer == null) {
			return new HawkTuple2<DYZZGameRoomData, Integer>(null, Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		String gameId = dyzzPlayer.getGameId();
		if (HawkOSOperator.isEmptyString(gameId)) {
			return new HawkTuple2<DYZZGameRoomData, Integer>(null, Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		DYZZGameRoomData gameData = DYZZRedisData.getInstance().getDYZZGameData(termId, gameId);
		if(gameData == null){
			return new HawkTuple2<DYZZGameRoomData, Integer>(null, Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		if(gameData.getState() != PBDYZZGameRoomState.DYZZ_GAME_GAMING){
			return new HawkTuple2<DYZZGameRoomData, Integer>(null, Status.DYZZError.DYZZ_WAR_NO_MATCH_INFO_VALUE);
		}
		return new HawkTuple2<DYZZGameRoomData, Integer>(gameData, Status.SysError.SUCCESS_OK_VALUE);
	}
	
	
	public boolean quitRoom(Player player,boolean midWayQuit){
		int termId = this.getDYZZWarTerm();
		String playerId = player.getId();
		DYZZPlayerData dyzzPlayer = DYZZRedisData.getInstance().getDYZZPlayerData(termId, playerId);
		if (dyzzPlayer == null) {
			HawkLog.logPrintln("DYZZWarService quitRoom error, dyzzPlayerData is null, playerId: {}, isMidwayQuit: {}", player.getId(), midWayQuit);
			return false;
		}
		if (dyzzPlayer.getEnterTime() == 0) {
			HawkLog.logPrintln("DYZZWarService quitRoom error, has not entered, playerId: {},  isMidwayQuit: {}", dyzzPlayer.getPlayerId(),midWayQuit);
			return false;
		}
		if (midWayQuit) {
			dyzzPlayer.setMidwayQuit(midWayQuit);
			dyzzPlayer.setQuitTime(HawkTime.getMillisecond());
			DYZZRedisData.getInstance().updateDYZZPlayerData(dyzzPlayer);
		}
		return true;
	}
	
	/** 本服玩家跨服取得建筑首占, 给留在本服的盟友发奖励*/
	@ProtocolHandler(code = CHP.code.YQZZ_FIRST_CONTROL_MAIL_VALUE)
	public void onYQZZCrossFirsBuildControl(HawkProtocol hawkProtocol) {
		PBYQZZFirstControlBuildMail req = hawkProtocol.parseProtocol(PBYQZZFirstControlBuildMail.getDefaultInstance());
		MailParames.Builder mailParames = MailParames.newBuilder()
				.addTitles(req.getX(),req.getY())
				.addSubTitles(req.getX(),req.getY())
				.setMailId(MailId.ATTACK_YQZZ_BUILD_FIRST_CONTROL)
				.addContents(req.getX())
				.addContents(req.getY())
				.setRewards(req.getReward())
				.setAwardStatus(MailRewardStatus.NOT_GET);
		for (String playerId : GuildService.getInstance().getGuildMembers(req.getGuildId())) {
			if (req.getExcludeList().contains(playerId)) {
				continue;
			}
			mailParames.setPlayerId(playerId);
			MailService.getInstance().sendMail(mailParames.build());
		}
	}
	
	/** 放弃建筑*/
	@ProtocolHandler(code = CHP.code.YQZZ_GIVEUP_BUILD_MAIL_REQ_VALUE)
	public void onYQZZGiveupBuild(HawkProtocol hawkProtocol) {
		PBYQZZFirstControlBuildMail req = hawkProtocol.parseProtocol(PBYQZZFirstControlBuildMail.getDefaultInstance());
		String guildName = GuildService.getInstance().getGuildName(req.getGuildId());
		MailParames.Builder mailParames = MailParames.newBuilder()
				.addTitles(req.getX(),req.getY())
				.addSubTitles(req.getX(),req.getY())
				.setMailId(MailId.YQZZ_GIVEUP_BUILD_MAIL)
				.addContents(req.getReward())
				.addContents(guildName)
				.addContents(req.getX())
				.addContents(req.getY())
				.setAwardStatus(MailRewardStatus.NOT_GET);
		for (String playerId : GuildService.getInstance().getGuildMembers(req.getGuildId())) {
			if (req.getExcludeList().contains(playerId)) {
				continue;
			}
			mailParames.setPlayerId(playerId);
			MailService.getInstance().sendMail(mailParames.build());
		}
	}
	
}
