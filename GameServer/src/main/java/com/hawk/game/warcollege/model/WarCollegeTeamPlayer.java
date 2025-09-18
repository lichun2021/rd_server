package com.hawk.game.warcollege.model;

import java.io.Serializable;
import java.util.Objects;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.WarCollege.MiniTeamPlayerMsg;
import com.hawk.game.protocol.WarCollege.TeamPlayerMsg;
import com.hawk.game.protocol.WarCollege.TeamState;
import com.hawk.game.util.BuilderUtil;

/** 组队玩家
 * 
 * @author jm */
public class WarCollegeTeamPlayer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 玩家ID */
	private String playerId;

	private TeamState state = TeamState.TEAM_WAIT;
	/** 创建teamPlayer的时间(可用于排序) */
	private int createTime;
	
	private int passMax;
	
	private int helpRewardCount;
	
	public WarCollegeTeamPlayer() {
	}

	public WarCollegeTeamPlayer(String playerId,int passMax,int helpRewardCount) {
		this.playerId = playerId;
		this.passMax = passMax;
		this.helpRewardCount = helpRewardCount;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	
	public int getPassMax() {
		return passMax;
	}
	
	public int getHelpRewardCount() {
		return helpRewardCount;
	}
	
	
	
	public MiniTeamPlayerMsg.Builder buildMiniTeamPlayerBuilder(boolean isLeader) {
		MiniTeamPlayerMsg.Builder miniPlayerBuilder = MiniTeamPlayerMsg.newBuilder();
		Player player = GlobalData.getInstance().makesurePlayer(this.getPlayerId());
		miniPlayerBuilder.setIcon(player.getIcon());
		miniPlayerBuilder.setPfIcon(player.getPfIcon());
		miniPlayerBuilder.setState(this.getState());
		miniPlayerBuilder.setMaxInstanceId(this.passMax);
		miniPlayerBuilder.setHelpRewardCount(this.helpRewardCount);
		miniPlayerBuilder.setIsLeader(isLeader);
		miniPlayerBuilder.setPlayerId(this.getPlayerId());
		return miniPlayerBuilder;
	}

	public TeamPlayerMsg.Builder buildTeamPlayerBuilder(boolean isLeader) {
		TeamPlayerMsg.Builder teamPlayerBuilder = TeamPlayerMsg.newBuilder();
		Player player = this.toPlayer();
		teamPlayerBuilder.setState(this.getState());
		teamPlayerBuilder.setBattleValue(player.getPower());
		teamPlayerBuilder.setPlayerId(this.getPlayerId());
		teamPlayerBuilder.setPlayerName(player.getName());
		teamPlayerBuilder.setIcon(player.getIcon());
		teamPlayerBuilder.setPfIcon(player.getPfIcon());
		teamPlayerBuilder.setPlayerCommon(BuilderUtil.genPlayerCommonBuilder(player));
		teamPlayerBuilder.setIsLeader(isLeader);
		teamPlayerBuilder.setMaxInstanceId(this.passMax);
		teamPlayerBuilder.setHelpRewardCount(this.helpRewardCount);
		return teamPlayerBuilder;
	}

	@Override
	public int hashCode() {
		return playerId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (Objects.isNull(obj)) {
			return false;
		}

		if (!(obj instanceof WarCollegeTeamPlayer)) {
			return false;
		}

		return this.playerId.equals(((WarCollegeTeamPlayer) obj).playerId);
	}

	public Player toPlayer() {
		return GlobalData.getInstance().makesurePlayer(playerId);
	}

	public boolean inInstance() {
		return state == TeamState.TEAM_START;
	}

	public void setState(TeamState state) {
		this.state = state;
	}

	public TeamState getState() {
		return state;
	}

}
