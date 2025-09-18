package com.hawk.game.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 密友（微信或QQ）邀请信息
 * 
 * @author lating
 *
 */
public class FriendInviteInfo {
	/**
	 * 邀请成功的好友信息<openid， 任务属性数据>
	 */
	private Map<String, TaskAttrInfo> inviteSuccFriends;
	/**
	 * 已领取过奖励的任务ID
	 */
	private Set<Integer> hasRewardTaskIds;
	/**
	 * 已发出过邀请但还未进入游戏的好友<openid, 邀请时间>
	 */
	private Map<String, Integer> inviteNotSuccFriends;
	/**
	 * 已完成的任务ID
	 */
	private Set<Integer> finishedTaskIds;

	/**
	 * 邀请好友任务属性数据
	 */
	public static class TaskAttrInfo {
		int cityLevel;
		String serverId;
		String openid;
		String sopenid;
		String platform;
		long inviteTime;
		
		public TaskAttrInfo() {}
		
		public TaskAttrInfo(int cityLevel, String serverId, String openid, String sopenid) {
			this.cityLevel = cityLevel;
			this.serverId = serverId;
			this.openid = openid;
			this.sopenid = sopenid;
		}
		
		public int getCityLevel() {
			return cityLevel;
		}

		public void setCityLevel(int cityLevel) {
			this.cityLevel = cityLevel;
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}
		
		public String getPlatform() {
			return platform;
		}

		public void setPlatform(String platform) {
			this.platform = platform;
		}
		
		public String getSopenid() {
			return sopenid;
		}

		public void setSopenid(String sopenid) {
			this.sopenid = sopenid;
		}
		
		public long getInviteTime() {
			return inviteTime;
		}

		public void setInviteTime(long inviteTime) {
			this.inviteTime = inviteTime;
		}
	}
	
	public FriendInviteInfo() {
		inviteSuccFriends = new HashMap<String, TaskAttrInfo>();
		hasRewardTaskIds = new HashSet<Integer>();
		inviteNotSuccFriends = new HashMap<String, Integer>();
		finishedTaskIds = new HashSet<Integer>();
	}

	public Map<String, TaskAttrInfo> getInviteSuccFriends() {
		return inviteSuccFriends;
	}

	public Set<Integer> getHasRewardTaskIds() {
		return hasRewardTaskIds;
	}
	
	public Map<String, Integer> getInviteNotSuccFriends() {
		return inviteNotSuccFriends;
	}
	
	public Set<String> getInvitedFriendsSOpenid() {
		if (inviteSuccFriends.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<String> sopenids = new HashSet<>();
		for (TaskAttrInfo attrInfo : inviteSuccFriends.values()) {
			sopenids.add(attrInfo.sopenid);
		}
		
		return sopenids;
	}

	public Set<Integer> getFinishedTaskIds() {
		return finishedTaskIds;
	}

}
