package com.hawk.activity.event.impl;

import java.util.List;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.type.impl.playerteamback.entity.MemberInfo;

/**
 * 玩家回流H5活动H5页面信息变更事件
 * 
 * @author lating
 *
 */
public class PlayerTeamBackH5Event extends ActivityEvent {
	
	/** 鉴定星数量（0 ：未鉴定； 其他：鉴定星数量）*/
	private int starNum;
	
	/**  组队ID（0 未组队； 其他：组队ID- 代表有组队） */
	private int teamId;
	
	private List<MemberInfo> memberList;
	
	private List<Integer> rewardList;

	public PlayerTeamBackH5Event(){ super(null);}
	public PlayerTeamBackH5Event(String playerId) {
		super(playerId);
	}

	public int getStarNum() {
		return starNum;
	}

	public void setStarNum(int starNum) {
		this.starNum = starNum;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public List<MemberInfo> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<MemberInfo> memberList) {
		this.memberList = memberList;
	}

	public List<Integer> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<Integer> rewardList) {
		this.rewardList = rewardList;
	}
	
}
