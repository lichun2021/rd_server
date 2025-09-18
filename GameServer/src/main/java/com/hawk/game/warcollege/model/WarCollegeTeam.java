package com.hawk.game.warcollege.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.google.common.collect.Lists;
import com.hawk.game.config.WarCollegeInstanceCfg;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.WarCollege.MiniTeamInfoMsg;
import com.hawk.game.protocol.WarCollege.TeamInfoMsg;
import com.hawk.game.protocol.WarCollege.TeamPlayerMsg;
import com.hawk.game.protocol.WarCollege.TeamState;

public class WarCollegeTeam {
	/** teamId唯一 */
	private int teamId;
	/** 队友列表 */
	private Map<String, WarCollegeTeamPlayer> members;

	/** 创建时间 */
	private int createTime;

	/** 进入副本的时间 */
	private int joinTime;
	/** 副本ID */
	private int instanceId;
	/** 戰鬥配置id */
	private int battleId;
	/** 哪个公会的 */
	private String guildId;

	private TeamState state = TeamState.TEAM_WAIT;

	//带新人的老玩家
	private Set<String> helpers;
	/** 进入副本后所在的房间 */
	private LMJYBattleRoom room;

	public WarCollegeTeam(int instanceId, int teamId, String guildId, int battleId) {
		this.teamId = teamId;
		this.guildId = guildId;
		this.instanceId = instanceId;
		this.battleId = battleId;
		this.members = Collections.synchronizedMap(new LinkedHashMap<>());
		this.createTime = HawkTime.getSeconds();
		this.helpers = new HashSet<>();
	}

	public MiniTeamInfoMsg.Builder buildMiniTeamInfoBuilder() {
		MiniTeamInfoMsg.Builder teamBuilder = MiniTeamInfoMsg.newBuilder();
		teamBuilder.setInstanceId(getInstanceId());
		teamBuilder.setTeamId(getTeamId());
		teamBuilder.setState(getState());
		teamBuilder.setLeaderName(GlobalData.getInstance().getPlayerNameById(getLeaderId()));
		teamBuilder.setCreateTime(getCreateTime());
		for (WarCollegeTeamPlayer teamPlayer : getMembers()) {
			boolean isleader = teamPlayer.getPlayerId().equals(getLeaderId());
			teamBuilder.addMiniPlayers(teamPlayer.buildMiniTeamPlayerBuilder(isleader));
		}

		return teamBuilder;
	}

	public TeamInfoMsg.Builder buildTeamInfoBuilder() {
		TeamInfoMsg.Builder teamInfoBuilder = TeamInfoMsg.newBuilder();
		teamInfoBuilder.setTeamId(getTeamId());
		for (WarCollegeTeamPlayer teamPlayer : getMembers()) {
			boolean isleader = teamPlayer.getPlayerId().equals(getLeaderId());
			TeamPlayerMsg.Builder builder = teamPlayer.buildTeamPlayerBuilder(isleader);
			teamInfoBuilder.addTeamPlayers(builder);
		}

		return teamInfoBuilder;
	}

	public int getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(int joinTime) {
		this.joinTime = joinTime;
	}

	public int getBattleId() {
		return battleId;
	}

	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public String getLeaderId() {
		Iterator<Entry<String, WarCollegeTeamPlayer>> iterator = members.entrySet().iterator();
		return iterator.hasNext() ? iterator.next().getKey() : "";
	}

	/** 转换成player对象
	 * 
	 * @return */
	public List<Player> toPlayerList() {
		ArrayList<Player> players = Lists.newArrayList();
		members.values().forEach(p -> {
			players.add(p.toPlayer());
		});
		return players;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public WarCollegeTeamPlayer removeTeamPlayer(String playerId) {
		return members.remove(playerId);
	}

	public WarCollegeTeamPlayer getTeamPlayer(String playerId) {
		return members.get(playerId);
	}

	public boolean addTeamPlayer(WarCollegeTeamPlayer teamPlayer) {
		return members.put(teamPlayer.getPlayerId(), teamPlayer) == null;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String membersString() {
		return Arrays.toString(members.keySet().toArray(new String[members.size()]));
	}

	public TeamState getState() {
		return state;
	}

	public void setState(TeamState state) {
		this.state = state;
	}

	/** 是否有人進入了副本
	 * 
	 * @return */
	public boolean inInstance() {
		return getState() == TeamState.TEAM_START;
	}

	/** 创建队伍后长时间不进房间开始副本 */
	public boolean createout() {
		if (inInstance()) {
			return false;
		}
		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, this.instanceId);
		return cfg == null ? true : (HawkTime.getSeconds() - createTime > (cfg.getCutTime()));
	}

	public Collection<WarCollegeTeamPlayer> getMembers() {
		return members.values();
	}

	public void setRoom(LMJYBattleRoom lmjyBattleRoom) {
		this.room = lmjyBattleRoom;
	}

	public LMJYBattleRoom getRoom() {
		return this.room;
	}
	
	public void calHelpReward(){
		String leaderId = this.getLeaderId();
		WarCollegeTeamPlayer leader = this.getTeamPlayer(leaderId);
		if(Objects.isNull(leader)){
			return;
		}
		//房主已经通过此关
		if(leader.getPassMax() >= this.instanceId){
			return;
		}
		
		Set<String> helpSet = new HashSet<>();
		for(WarCollegeTeamPlayer member : this.members.values()){
			if(member.getPlayerId().equals(leaderId)){
				continue;
			}
			//这个队员没通过此关
			if(member.getPassMax() < this.instanceId){
				continue;
			}
			//带新奖励次数是否已经上限
			WarCollegeTimeControlCfg config = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
			if (config == null) {
				continue;
			}
			if(member.getHelpRewardCount() >= config.getDailyTeacherReward()){
				continue;
			}
			helpSet.add(member.getPlayerId());
		}
		this.helpers = helpSet;
	}
	
	public Set<String> getHelpers() {
		return helpers;
	}

}
