package com.hawk.game.warcollege;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.hawk.xid.HawkXID;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WarCollegeInstanceCfg;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.entity.PlayerWarCollegeEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.msg.LMJYJoinRoomMsg;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.WarCollege.DismissInfo;
import com.hawk.game.protocol.WarCollege.DismissType;
import com.hawk.game.protocol.WarCollege.SelfInfo;
import com.hawk.game.protocol.WarCollege.SelfTeamInfo;
import com.hawk.game.protocol.WarCollege.TeamPlayerMsg;
import com.hawk.game.protocol.WarCollege.TeamPlayerOper;
import com.hawk.game.protocol.WarCollege.TeamState;
import com.hawk.game.protocol.WarCollege.WarCollegeMiniTeamResp;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamDetailResp;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamDissolveResp;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamPlayerUpdate;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.warcollege.model.WarCollegeTeam;
import com.hawk.game.warcollege.model.WarCollegeTeamPlayer;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldPlayerService;

/** 仿虎牢关 曾用名战争学院副本 现用名联盟军演 方法名带on的都是对外接口
 * 
 * @author jm */
public class WarCollegeInstanceService extends HawkAppObj {
	/** teamId 递增 */
	private AtomicInteger autoTeamId = null;
	/** {playerId, teamId} */
	private Map<String, Integer> playerIdTeamIdMap = null;
	/** {teamId, team} */
	private Map<Integer, WarCollegeTeam> teamMap = null;
	/**
	 * 
	 */
	private boolean isOpen = false;
	/** {guildId, teamIdList} */
	private Map<String, Set<Integer>> guildIdTeamIdListMap = null;

	// 分享CD间隔
	private Map<String, Integer> shareMap = null;

	private static WarCollegeInstanceService instance = null;

	public static WarCollegeInstanceService getInstance() {
		return instance;
	}

	public WarCollegeInstanceService(HawkXID xid) {
		super(xid);
		init();
		instance = this;
	}

	private boolean init() {
		this.autoTeamId = new AtomicInteger(0);
		this.playerIdTeamIdMap = Maps.newConcurrentMap();
		this.teamMap = Maps.newConcurrentMap();
		this.guildIdTeamIdListMap = Maps.newConcurrentMap();
		this.shareMap = Maps.newConcurrentMap();
		this.isOpen = isOpen();
		this.addTickable(new HawkPeriodTickable(3000) {
			@Override
			public void onPeriodTick() {
				checkState();
			}
		});
		return true;
	}

	/** 分享cd时间是否够
	 * 
	 * @param playerId
	 * @return */
	public boolean onPass(String playerId) {
		if (!shareMap.containsKey(playerId)) {
			return shareMap.put(playerId, HawkTime.getSeconds()) == null;
		} else {
			int last = shareMap.get(playerId);
			int interval = ConstProperty.getInstance().getCdTime();
			if (HawkTime.getSeconds() - last >= interval) {
				return shareMap.put(playerId, HawkTime.getSeconds()) != null;
			}
			return false;
		}
	}

	public void checkState() {
		checkAcvitityState();
		checkTeam();
	}

	private void checkTeam() {
		ArrayList<WarCollegeTeam> teams = new ArrayList<>(teamMap.values());
		for (WarCollegeTeam team : teams) {
			if (team.createout()) {
				LogUtil.logWarCollegeTeam(team.getTeamId(), team.getLeaderId(), team.getGuildId(), team.getInstanceId(), WarCollegeTeamOP.OVERTIME);
				dissovleWarCollegeTeam(team, DismissType.CREATE_TOO_LONG_DISMISS);
			}
		}
	}

	private void checkAcvitityState() {
		// 如果是连开的你可以打死策划.
		boolean _isOpen = isOpen();
		if (_isOpen == isOpen) {
			return;
		}
		isOpen = _isOpen;
		// 开
		if (!_isOpen) {
			doEnd();
		}
	}

	/** 活动结束 */
	private void doEnd() {
		// 原本的设计是以team为主体,战斗中也只是team的一个状态,不过现在以战斗那边为主.
		List<WarCollegeTeam> teams = new ArrayList<WarCollegeTeam>(teamMap.values());
		for (WarCollegeTeam team : teams) {
			if (!team.inInstance()) {
				// 没有进入副本的才需要解散否则等他们战斗完
				LogUtil.logWarCollegeTeam(team.getTeamId(), team.getLeaderId(), team.getGuildId(), team.getInstanceId(), WarCollegeTeamOP.ACT_END);
				this.dissovleWarCollegeTeam(team, DismissType.ACT_END_DISMISS);
			}
		}
	}

	/** 获取本队伍的详细信息
	 * 
	 * @param playerId */
	public int onTeamReq(Player player, WarCollegeTeam team) {
		WarCollegeTeamDetailResp.Builder resp = WarCollegeTeamDetailResp.newBuilder();
		resp.setInstanceId(team.getInstanceId());
		resp.setState(team.getState());
		resp.setCreateTime(team.getCreateTime());
		resp.setTeamId(team.getTeamId());
		team.getMembers().forEach(p -> {
			boolean isleader = team.getLeaderId().equals(p.getPlayerId());
			TeamPlayerMsg.Builder teamPlayerBuilder = p.buildTeamPlayerBuilder(isleader);
			resp.addTeamPlayers(teamPlayerBuilder);
		});
		resp.setSelfTeamInfo(buildSelf(player));
		// System.err.println("onTeamReq:" + resp.build().toString());
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.WAR_COLLEGE_TEAM_DETAIL_RESP_VALUE, resp);
		player.sendProtocol(hawkProtocol);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 请求队伍列表信息
	 * 
	 * @param playerId */
	public int onMiniTeamReq(Player player) {
		String guildId = player.getGuildId();
		List<WarCollegeTeam> teams = this.getGuildTeams(guildId);
		WarCollegeMiniTeamResp.Builder miniBuilder = WarCollegeMiniTeamResp.newBuilder();
		for (WarCollegeTeam team  : teams) {
			if (!team.inInstance()) {// 沒有進入副本的隊伍才可見
				miniBuilder.addMiniTeamInfos(team.buildMiniTeamInfoBuilder());
			}
		}
		miniBuilder.setSelfTeamInfo(buildSelf(player));
		// System.err.println("onMiniTeamReq:" + miniBuilder.build().toString());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WAR_COLLEGE_MINI_TEAM_RESP_VALUE, miniBuilder);
		player.sendProtocol(protocol);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	public List<WarCollegeTeam> getGuildTeams(String guildId){
		List<WarCollegeTeam> rlist = new ArrayList<>();
		if(HawkOSOperator.isEmptyString(guildId)){
			return rlist;
		}
		Set<Integer> teamIdList = this.getTeamIdList(guildId);
		for (Integer teamId : teamIdList) {
			WarCollegeTeam team = this.getWarCollegeTeam(teamId);
			if (!team.inInstance()) {// 沒有進入副本的隊伍才可見
				rlist.add(team);
			}
		}
		return rlist;
	}
	
	
	/** 快速加入隊伍
	 * 
	 * @param id
	 * @return */
	public int onTeamQuickJoin(Player player) {
		Set<Integer> list = this.getTeamIdList(player.getGuildId());
		for (int teamId : list) {
			WarCollegeTeam team = this.getWarCollegeTeam(teamId);
			if (team.getTeamPlayer(player.getId()) == null) {
				int code = onTeamJoin(player, teamId);
				if (code == Status.SysError.SUCCESS_OK_VALUE) {
					return code;
				}
			}
		}
		return Status.Error.WAR_COLLEGE_JOIN_QUICK_FAIL_VALUE;
	}

	/** 加入队伍
	 * 
	 * @param playerId
	 * @param teamId
	 * @return */
	public int onTeamJoin(Player player, Integer teamId) {

		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		if (entity == null) {
			throw new NullPointerException("PlayerWarCollegeEntity is null,playerId=" + player.getId());
		}
		entity.checkOrReset();

		if (!WarCollegeInstanceService.getInstance().isOpen()) {
			return Status.Error.WAR_COLLEGE_INSTANCE_NOT_OPEN_VALUE;
		}
		if (!player.hasGuild()) {
			return Status.Error.WAR_COLLEGE_NOT_HAVE_GUILD_VALUE;
		}
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			return Status.Error.LMJY_HAS_PLYAER_MARCH_VALUE;
		}
		if (getTeamId(player.getId()) != null) {
			return Status.Error.WAR_COLLEGE_ALEARDY_IN_TEAM_VALUE;
		}
		WarCollegeTeam team = this.getWarCollegeTeam(teamId);
		if (team == null) {
			return Status.Error.WAR_COLLEGE_TEAM_NOT_EXIST_VALUE;
		}
		if (team.inInstance()) {
			return Status.Error.WAR_COLLEGE_TEAM_STATE_NOT_WAIT_VALUE;
		}

		Player leader = GlobalData.getInstance().makesurePlayer(team.getLeaderId());
		Player joinPlayer = GlobalData.getInstance().makesurePlayer(player.getId());
		if (!leader.getGuildId().equals(joinPlayer.getGuildId())) {
			return Status.Error.WAR_COLLEGE_NOT_SAME_VALUE;
		}

		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, team.getInstanceId());
		if (cfg == null) {
			throw new NullPointerException("WarCollegeInstanceCfg is null,instanceId=" + team.getInstanceId());
		}
		if (player.getCityLevel() < cfg.getCityLevel()) {
			return Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE;
		}
		if (cfg.getNeedOpenInstanceId() > 0 && !entity.hitInstance(cfg.getNeedOpenInstanceId())) {
			return Status.Error.WAR_COLLEGE_NOT_PASS_BEFORE_VALUE;
		}

		if (cfg.getMaxMemberNum() <= team.getMembers().size()) {
			return Status.Error.OUT_OF_MEMBER_LIMIT_VALUE;
		}

		if (entity.getHitCount(team.getInstanceId()) >= cfg.getDailyTimesTotal()) {
			return Status.Error.WAR_COLLEGE_TIMES_NOT_ENOUGH_VALUE;
		}
		WarCollegeTeamPlayer _joinPlayer = new WarCollegeTeamPlayer(player.getId(),entity.getMaxInstanceId(),entity.getHelpRwardCount());
		team.addTeamPlayer(_joinPlayer);
		this.buildTeam(player.getId(), teamId);

		this.notifyTeamPlayerUpdate(team, _joinPlayer, TeamPlayerOper.TEAM_PLAYER_ADD);
		this.notifyTeamInfo(player.getId(), team);
		refresh(leader.getGuildId());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "joinTeam");
		jsonObject.put("playerId", player.getId());
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 创建队伍
	 * 
	 * @param playerId
	 * @param instanceId
	 * @return */
	public int onTeamCreate(Player player, Integer instanceId) {

		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		if (entity == null) {
			throw new NullPointerException("PlayerWarCollegeEntity is null,playerid=" + player.getId());
		}
		entity.checkOrReset(instanceId);

		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, instanceId);
		if (cfg == null) {
			throw new NullPointerException("WarCollegeInstanceCfg is null,instanceId=" + instanceId);
		}

		if (!WarCollegeInstanceService.getInstance().isOpen()) {
			return Status.Error.WAR_COLLEGE_INSTANCE_NOT_OPEN_VALUE;
		}

		if (!player.hasGuild()) {
			return Status.Error.WAR_COLLEGE_NOT_HAVE_GUILD_VALUE;
		}

		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			return Status.Error.LMJY_HAS_PLYAER_MARCH_VALUE;
		}

		if (player.getCityLevel() < cfg.getCityLevel()) {
			return Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE;
		}

		if (cfg.getNeedOpenInstanceId() > 0 && !entity.hitInstance(cfg.getNeedOpenInstanceId())) {
			return Status.Error.WAR_COLLEGE_NOT_PASS_BEFORE_VALUE;
		}

		// 打过了當前副本并且次数达到上线
		if (entity.getHitCount(instanceId) >= cfg.getDailyTimesTotal()) {
			return Status.Error.WAR_COLLEGE_TIMES_NOT_ENOUGH_VALUE;
		}
		if (getTeamId(player.getId()) != null) {
			return Status.Error.WAR_COLLEGE_ALEARDY_IN_TEAM_VALUE;
		}

		int battleId = cfg.getBattleId();
		WarCollegeTeam team = this.createWarCollegeTeam(player.getGuildId(), entity, instanceId, battleId);
		buildTeam(player.getId(), team.getTeamId());
		notifyTeamInfo(player.getId(), team);
		refresh(player.getGuildId());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "onTeamCreate");
		jsonObject.put("playerId", player.getId());
		jsonObject.put("instanceId", instanceId);
		jsonObject.put("battleId", battleId);
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);

		LogUtil.logWarCollegeTeam(team.getTeamId(), team.getLeaderId(), team.getGuildId(), instanceId, WarCollegeTeamOP.CREATE);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 解散队伍
	 * 
	 * @param playerId
	 * @return */
	public int onTeamDissolve(String playerId) {
		WarCollegeTeam team = this.getWarCollegeTeamByPlayerId(playerId);
		if (team == null) {
			return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
		}
		if (!team.getLeaderId().equals(playerId)) {
			return Status.Error.WAR_COLLEGE_NOT_LEADER_VALUE;
		}
		if (team.inInstance()) {
			return Status.Error.WAR_COLLEGE_TEAM_STATE_NOT_WAIT_VALUE;
		}
		LogUtil.logWarCollegeTeam(team.getTeamId(), team.getLeaderId(), team.getGuildId(), team.getInstanceId(), WarCollegeTeamOP.LEADER_DISMISS);
		dissovleWarCollegeTeam(team, DismissType.LEADER_DISMISS);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "onTeamDissolve");
		jsonObject.put("playerId", playerId);
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 进入副本
	 * 
	 * @param playerId
	 * @return */
	public int onInstanceEnter(Player player) {
		WarCollegeTeam team = this.getWarCollegeTeamByPlayerId(player.getId());
		if (team == null) {
			return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
		}
		WarCollegeTeamPlayer enterPlayer = team.getTeamPlayer(player.getId());
		if (enterPlayer == null) {
			return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
		}
		if (enterPlayer.inInstance()) {
			return Status.Error.WAR_COLLEGE_ALREARDY_IN_INSTANCE_VALUE;
		}

		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			return Status.Error.LMJY_HAS_PLYAER_MARCH_VALUE;
		}
		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			return Status.Error.TIBERIUM_HAS_ASSISTANCE_MARCH_VALUE;
		}

		// 有被动行军
		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if (!CollectionUtils.isEmpty(passiveMarchs)) {
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			for (IWorldMarch march : passiveMarchs) {
				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
					continue;
				}
				if (march.getTerminalId() != playerPos) {
					continue;
				}
				return Status.Error.TIBERIUM_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		if (IReportPushMarch.hasRedAlarm(player.getId())) {// 有行军不能加入
			return Status.Error.LMJY_HAS_PLYAER_BEATK_VALUE;
		}

		boolean isleader = enterPlayer.getPlayerId().equals(team.getLeaderId());
		if (isleader) {
			if (!WarCollegeInstanceService.getInstance().isOpen()) {
				return Status.Error.WAR_COLLEGE_INSTANCE_NOT_OPEN_VALUE;
			}
		}
		if (CityManager.getInstance().cityIsFired(player.getId())) {
			return Status.Error.WAR_COLLEGE_CITY_FIRE_VALUE;
		}
		boolean isLeader = player.getId().equals(team.getLeaderId());
		if (isLeader && team.getRoom() == null) {// 是队长并且第一次进房间才可以
			WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, team.getInstanceId());
			if (cfg == null) {
				throw new NullPointerException("WarCollegeInstanceCfg is null,instanceId=" + team.getInstanceId());
			}
			if (team.getMembers().size() < cfg.getOpenNum()) {
				return Status.Error.WAR_COLLEGE_TEAM_NUM_NOT_ENOUGH_VALUE;
			}
			LMJYExtraParam extparam = new LMJYExtraParam();
			extparam.setInstanceId(team.getInstanceId());
			extparam.setBattleId(team.getBattleId());
			extparam.setTeamId(team.getTeamId());
			extparam.setLeaderId(player.getId());
			extparam.setMembers(team.membersString());
			for (Player teamp : team.toPlayerList()) {
				PlayerWarCollegeEntity entity = teamp.getData().getPlayerWarCollegeEntity();
				entity.checkOrReset(team.getInstanceId());
				String reward = entity.getRewardCount(team.getInstanceId()) < cfg.getDailyRewardTimes() ? cfg.getReward() : "";
				extparam.putWinReward(teamp.getId(), reward);
			}
			LMJYBattleRoom lmjyBattleRoom = LMJYRoomManager.creatNewBattle(team.toPlayerList(), team.getBattleId(), extparam);
			team.setRoom(lmjyBattleRoom);
			team.setState(TeamState.TEAM_START);
			team.calHelpReward();
		}
		ILMJYPlayer gamer = LMJYRoomManager.getInstance().makesurePlayer(player.getId());
		LMJYJoinRoomMsg msg = LMJYJoinRoomMsg.valueOf(gamer.getParent(), gamer);
		HawkApp.getInstance().postMsg(player.getXid(), msg);

		enterPlayer.setState(TeamState.TEAM_START);
		if (isLeader) {
			team.setJoinTime(HawkTime.getSeconds());
		}
		notifyTeamPlayerUpdate(team, enterPlayer, TeamPlayerOper.TEAM_PLAYER_JOIN_INSTANCE);
		refresh(player.getGuildId());

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "onInstanceEnter");
		jsonObject.put("playerId", player.getId());
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 其实可以用DailyActivityService 来管控所有的daily_activity里面的表.
	 * 
	 * @return */
	public boolean isOpen() {
		WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
		if (config == null) {
			return true;
		}
		if(!config.openWarCollege()){
			return false;
		}
		
		long time = HawkTime.getMillisecond();
		long specialTermOverTime = this.getSpecialTermOverTime();
		if(time <= specialTermOverTime){
			//开服x天内特殊排期
			return this.isOpenSpecialTerm(time);
		}else{
			//正常排期
			return this.isopenNormal(time);
		}
			
	}
	
	/**
	 * 是否在正常开放时间内
	 * @param time
	 * @return
	 */
	public boolean isopenNormal(long time){
		WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
		int curDayOfWeek = localDateTime.getDayOfWeek().getValue();
		boolean isToday = false;
		for (int day : config.getDaysArray()) {
			if (day == curDayOfWeek) {
				isToday = true;
				break;
			}
		}

		if (!isToday) {
			return false;
		}
		// 16:00:00 15:00:00 字符串默认的ascii字典比较的方法刚好符合时间比较.
		String curTime = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		if (config.getStartTime().compareTo(curTime) > 0 || config.getEndTime().compareTo(curTime) < 0) {
			return false;
		}

		return true;
	}
	
	/**
	 * 是否在开服时间特殊限定时间内开放
	 * @param time
	 * @return
	 */
	public boolean isOpenSpecialTerm(long time){
		List<HawkTuple2<Long, Long>> spList = this.getSpecialTerm();
		for(HawkTuple2<Long, Long> tuple : spList){
			if(tuple.first > time || time >= tuple.second){
				continue;
			}
			WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
			LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId());
			String curTime = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			if (config.getStartTime().compareTo(curTime) > 0 || config.getEndTime().compareTo(curTime) < 0) {
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 特殊排期结束时间
	 * @return
	 */
	public long getSpecialTermOverTime(){
		WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
		long time = GameUtil.getServerOpenTime();
		long beginTime = HawkTime.getAM0Date(new Date(time)).getTime();
		long endTime = beginTime + HawkTime.DAY_MILLI_SECONDS * (config.getNormalDays() -1);
		return endTime;
	}
	
	
	/**
	 * 获取开服特定时间 
	 * @return
	 */
	public List<HawkTuple2<Long, Long>> getSpecialTerm(){
		List<HawkTuple2<Long, Long>> list = new ArrayList<>();
		WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
		List<Integer> spList = config.getServerDaysOpenList();
		long time = GameUtil.getServerOpenTime();
		for(int day : spList){
			long beginTime = HawkTime.getAM0Date(new Date(time)).getTime();
			long endTime = beginTime + HawkTime.DAY_MILLI_SECONDS * day;
			long startTime = endTime - HawkTime.DAY_MILLI_SECONDS;
			list.add(HawkTuples.tuple(startTime, endTime));
		}
		return list;
	}
	
	

	/** 解散队伍 */
	public void dissovleWarCollegeTeam(WarCollegeTeam team, DismissType dismissType) {
		final List<Player> players = team.toPlayerList();
		team.getMembers().forEach(p -> {
			clearPlayerTeam(p.getPlayerId());
		});
		if (!players.isEmpty()) {
			WarCollegeTeamDissolveResp.Builder resp = WarCollegeTeamDissolveResp.newBuilder();
			resp.setDismiss(DismissInfo.newBuilder().setDtype(dismissType));
			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.WAR_COLLEGE_TEAM_DISSOLVE_RESP_VALUE, resp);
			players.forEach(p -> {
				p.sendProtocol(hawkProtocol);
			});
		}
		teamMap.remove(team.getTeamId());
		deleteFromTeamIdList(team.getGuildId(), team.getTeamId());
		refresh(team.getGuildId());
	}

	/** 创建队伍
	 * 
	 * @param guildId
	 * @param playerId
	 * @param instanceId
	 * @return */
	private WarCollegeTeam createWarCollegeTeam(String guildId, PlayerWarCollegeEntity playerCollegeEntity, int instanceId, int battleId) {
		Integer newTeamId = autoTeamId.incrementAndGet();
		WarCollegeTeam warCollegeTeam = new WarCollegeTeam(instanceId, newTeamId, guildId, battleId);
		warCollegeTeam.addTeamPlayer(new WarCollegeTeamPlayer(playerCollegeEntity.getPlayerId(),playerCollegeEntity.getMaxInstanceId(),playerCollegeEntity.getHelpRwardCount()));
		this.buildTeam(playerCollegeEntity.getPlayerId(), newTeamId);
		this.addToTeamIdList(guildId, newTeamId);
		this.teamMap.put(newTeamId, warCollegeTeam);
		return warCollegeTeam;
	}

	/** 绑定玩家和队伍的关系
	 * 
	 * @param playerId
	 * @param teamId
	 * @return */
	public boolean buildTeam(String playerId, int teamId) {
		return playerIdTeamIdMap.put(playerId, teamId) == null;
	}

	/** 清除玩家和队伍的绑定关系
	 * 
	 * @param playerId
	 * @return */
	public boolean clearPlayerTeam(String playerId) {
		shareMap.remove(playerId);
		return playerIdTeamIdMap.remove(playerId) != null;
	}

	/** get team id by playerId
	 * 
	 * @param playerId
	 * @return */
	public Integer getTeamId(String playerId) {
		Integer teamId = playerIdTeamIdMap.get(playerId);
		if (teamId != null) {
			WarCollegeTeam team = getWarCollegeTeam(teamId);
			if (team == null || team.getTeamPlayer(playerId) == null) {
				clearPlayerTeam(playerId);
				teamId = playerIdTeamIdMap.get(playerId);
			}
		}
		return teamId;
	}

	/** 是否是在房间里面
	 * 
	 * @param playerId
	 * @return */
	public boolean isInTeam(String playerId) {
		return playerIdTeamIdMap.containsKey(playerId);
	}

	/** get team by team id
	 * 
	 * @param teamId
	 * @return */
	public WarCollegeTeam getWarCollegeTeam(Integer teamId) {
		return teamMap.get(teamId);
	}

	/** get team by player id
	 * 
	 * @param playerId
	 * @return */
	public WarCollegeTeam getWarCollegeTeamByPlayerId(String playerId) {
		Integer teamId = this.getTeamId(playerId);
		if (teamId == null) {
			return null;
		} else {
			return this.getWarCollegeTeam(teamId);
		}
	}

	/** 没有的时候插入一个空的列表进去
	 * 
	 * @param guildId
	 * @return */
	public Set<Integer> getTeamIdList(String guildId) {
		if (!guildIdTeamIdListMap.containsKey(guildId)) {
			synchronized (guildIdTeamIdListMap) {
				if (!guildIdTeamIdListMap.containsKey(guildId)) {
					guildIdTeamIdListMap.put(guildId, Sets.newConcurrentHashSet());
				}
			}
		}
		return guildIdTeamIdListMap.get(guildId);
	}

	/** 公会组队列表里面删除某个队伍
	 * 
	 * @param guildId
	 * @param teamId */
	private boolean deleteFromTeamIdList(String guildId, Integer teamId) {
		return getTeamIdList(guildId).remove(teamId);
	}

	/** 队伍加到公会组队列表
	 * 
	 * @param guildId
	 * @param teamId */
	private boolean addToTeamIdList(String guildId, Integer teamId) {
		return getTeamIdList(guildId).add(teamId);
	}

	public void notifyTeamPlayerUpdate(WarCollegeTeam team, WarCollegeTeamPlayer player, TeamPlayerOper oper) {
		WarCollegeTeamPlayerUpdate.Builder updateBuilder = WarCollegeTeamPlayerUpdate.newBuilder();
		boolean isLeader = team.getLeaderId().equals(player.getPlayerId());
		TeamPlayerMsg.Builder teamPlayerBuilder = player.buildTeamPlayerBuilder(isLeader);
		updateBuilder.setTeamPlayer(teamPlayerBuilder);
		updateBuilder.setOper(oper);
		updateBuilder.setState(team.getState());
		updateBuilder.setLeader(team.getLeaderId());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WAR_COLLEGE_TEAM_PLAYER_UPDATE_VALUE, updateBuilder);
		for (WarCollegeTeamPlayer teamPlayer : team.getMembers()) {
			if (!teamPlayer.getPlayerId().equals(player.getPlayerId()))
				teamPlayer.toPlayer().sendProtocol(protocol);
		}
		// 当离开队伍时，离开的玩家也需要收到消息，这里补发一条消息
		player.toPlayer().sendProtocol(protocol);
	}

	public void notifyTeamInfo(String playerId, WarCollegeTeam team) {
		WarCollegeTeamDetailResp.Builder resp = WarCollegeTeamDetailResp.newBuilder();
		resp.setInstanceId(team.getInstanceId());
		team.getMembers().forEach(p -> {
			boolean leader = team.getLeaderId().equals(p.getPlayerId());
			TeamPlayerMsg.Builder teamPlayerBuilder = p.buildTeamPlayerBuilder(leader);
			resp.addTeamPlayers(teamPlayerBuilder);
		});
		resp.setSelfTeamInfo(buildSelf(GlobalData.getInstance().makesurePlayer(playerId)));
		// System.err.println("notifyTeamInfo:" + resp.build().toString());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WAR_COLLEGE_TEAM_DETAIL_RESP_VALUE, resp);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		player.sendProtocol(protocol);
	}

	private SelfTeamInfo.Builder buildSelf(Player player) {
		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		// entity.checkOrReset();
		SelfTeamInfo.Builder builder = SelfTeamInfo.newBuilder();
		builder.setMaxInstanceId(entity.getMaxInstanceId());
		builder.setHelpRewardCount(entity.getHelpRwardCount());
		entity.getInstanceInfoMap().forEach((k, v) -> {
			SelfInfo.Builder self = SelfInfo.newBuilder();
			self.setInstanceId(k);
			self.setHitCount(entity.getHitCount(k));
			self.setLastTime(entity.getLastTime(k));
			self.setRewardCount(entity.getRewardCount(k));
			self.setFristRewardTime(entity.getFirstRewardTime(k));
			builder.addInfos(self);
		});
		return builder;
	}

	/** 退出队伍
	 * 
	 * @param playerId
	 * @return */
	public int onTeamQuit(Player player, TeamPlayerOper oper) {
		WarCollegeTeam team = this.getWarCollegeTeamByPlayerId(player.getId());
		if (team == null) {
			return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
		}
		try {

			if (team.inInstance() && oper.getNumber() <= TeamPlayerOper.TEAM_PLAYER_KICK_VALUE) {// 不是副本內的動作，不能退出，只能是被打飛，投降等退出
				return Status.Error.WAR_COLLEGE_TEAM_STATE_NOT_WAIT_VALUE;
			}
			WarCollegeTeamPlayer quitPlayer = team.removeTeamPlayer(player.getId());
			if (quitPlayer == null) {
				return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
			}
			WarCollegeInstanceService.getInstance().clearPlayerTeam(quitPlayer.getPlayerId());
			if (team.getMembers().isEmpty()) {
				try {
					String dismissType = team.getRoom().isCampAwin() ? WarCollegeTeamOP.BATTLE_WIN : WarCollegeTeamOP.DEFEAT;
					LogUtil.logWarCollegeTeam(team.getTeamId(), team.getLeaderId(), team.getGuildId(), team.getInstanceId(), dismissType);
				} catch (Exception e) {
//					HawkException.catchException(e);
				}

				team = teamMap.remove(team.getTeamId());
				deleteFromTeamIdList(team.getGuildId(), team.getTeamId());
			}
			notifyTeamPlayerUpdate(team, quitPlayer, oper);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		refresh(team.getGuildId());

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", oper);
		jsonObject.put("playerId", player.getId());
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 踢出队友
	 * 
	 * @param playerId
	 * @param kickedPlayerId
	 * @return */
	public int onTeamKickPlayer(String playerId, String kickedPlayerId) {
		WarCollegeTeam team = this.getWarCollegeTeamByPlayerId(playerId);
		if (team == null) {
			return Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE;
		}
		if (!team.getLeaderId().equals(playerId)) {
			return Status.Error.WAR_COLLEGE_NOT_LEADER_VALUE;
		}
		if (team.inInstance()) {
			return Status.Error.WAR_COLLEGE_TEAM_STATE_NOT_WAIT_VALUE;
		}
		WarCollegeTeamPlayer quitPlayer = team.removeTeamPlayer(kickedPlayerId);
		WarCollegeInstanceService.getInstance().clearPlayerTeam(kickedPlayerId);
		notifyTeamPlayerUpdate(team, quitPlayer, TeamPlayerOper.TEAM_PLAYER_KICK);
		refresh(quitPlayer.toPlayer().getGuildId());

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "onTeamKickPlayer");
		jsonObject.put("playerId", playerId);
		jsonObject.put("kickoutPlayerId", kickedPlayerId);
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/** 当联盟队伍发生变更时推送
	 * 
	 * @param guildId */
	public void refresh(String guildId) {
		List<WarCollegeTeam> teams = this.getGuildTeams(guildId);
		WarCollegeMiniTeamResp.Builder miniBuilder = WarCollegeMiniTeamResp.newBuilder();
		for (WarCollegeTeam team  : teams) {
			if (!team.inInstance()) {// 沒有進入副本的隊伍才可見
				miniBuilder.addMiniTeamInfos(team.buildMiniTeamInfoBuilder());
			}
		}
		WarCollegeMiniTeamResp builder = miniBuilder.build();
		GuildService.getInstance().getOnlineMembers(guildId).forEach(id -> {
			GlobalData.getInstance().makesurePlayer(id).sendProtocol(HawkProtocol.valueOf(HP.code.WAR_COLLEGE_TEAM_REFRESH_RESP_VALUE,builder.toByteArray()));
		});
		
	}
	

	
	/**
	 * 是否有带新人奖励
	 * @return
	 */
	public boolean hasHelpReward(Player player){
		WarCollegeTeam team = this.getWarCollegeTeamByPlayerId(player.getId());
		if(Objects.isNull(team)){
			return false;
		}
		Set<String> set = team.getHelpers();
		if(Objects.isNull(set)){
			return false;
		}
		if(!set.contains(player.getId())){
			return false;
		}
		return true;
	}
	
	

}
