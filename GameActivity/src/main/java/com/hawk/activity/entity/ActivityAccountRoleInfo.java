package com.hawk.activity.entity;

/***
 * 活动工程的帐号角色信息，对应GameServer工程的AccountRoleInfo
 * 反序列化只需要玩家回归判定所用到的参数，不必和AccountRoleInfo属性一一对应
 * @author yang.rao
 *
 */
public class ActivityAccountRoleInfo implements Comparable<ActivityAccountRoleInfo>{
	
	private String openId;
	private String playerId;
	private String playerName;
	private int playerLevel;
	private int vipLevel;
	private int cityLevel;
	private long battlePoint;
	private String serverId;
	private String activeServer;
	private String platform;
	private int icon;
	private String pfIcon;
	private int qqSVIPLevel;
	private int loginWay;
	private long registerTime;
	private long loginTime;
	private long logoutTime;
	
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getPlayerLevel() {
		return playerLevel;
	}
	public void setPlayerLevel(int playerLevel) {
		this.playerLevel = playerLevel;
	}
	public int getVipLevel() {
		return vipLevel;
	}
	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}
	public int getCityLevel() {
		return cityLevel;
	}
	public void setCityLevel(int cityLevel) {
		this.cityLevel = cityLevel;
	}
	public long getBattlePoint() {
		return battlePoint;
	}
	public void setBattlePoint(long battlePoint) {
		this.battlePoint = battlePoint;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getActiveServer() {
		return activeServer;
	}
	public void setActiveServer(String activeServer) {
		this.activeServer = activeServer;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public String getPfIcon() {
		return pfIcon;
	}
	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}
	public int getQqSVIPLevel() {
		return qqSVIPLevel;
	}
	public void setQqSVIPLevel(int qqSVIPLevel) {
		this.qqSVIPLevel = qqSVIPLevel;
	}
	public int getLoginWay() {
		return loginWay;
	}
	public void setLoginWay(int loginWay) {
		this.loginWay = loginWay;
	}
	public long getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(long registerTime) {
		this.registerTime = registerTime;
	}
	public long getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	public long getLogoutTime() {
		return logoutTime;
	}
	public void setLogoutTime(long logoutTime) {
		this.logoutTime = logoutTime;
	}
	@Override
	public int compareTo(ActivityAccountRoleInfo targetRole) {
		if (cityLevel == targetRole.cityLevel) {
			if (battlePoint == targetRole.battlePoint) {
				if (vipLevel == targetRole.vipLevel) {
					if (playerLevel == targetRole.playerLevel) {
						return targetRole.loginTime - loginTime > 0 ? 1 : -1;
					} else {
						return targetRole.playerLevel - playerLevel;
					}
				} else {
					return targetRole.vipLevel - vipLevel;
				}
			} else {
				return targetRole.battlePoint - battlePoint > 0 ? 1 : -1;
			}
		} else {
			return targetRole.cityLevel - cityLevel;
		}
	}
	@Override
	public String toString() {
		return "ActivityAccountRoleInfo [openId=" + openId + ", playerId=" + playerId + ", playerName=" + playerName
				+ ", playerLevel=" + playerLevel + ", vipLevel=" + vipLevel + ", cityLevel=" + cityLevel
				+ ", battlePoint=" + battlePoint + ", serverId=" + serverId + ", activeServer=" + activeServer
				+ ", platform=" + platform + ", icon=" + icon + ", pfIcon=" + pfIcon + ", qqSVIPLevel=" + qqSVIPLevel
				+ ", loginWay=" + loginWay + ", registerTime=" + registerTime + ", loginTime=" + loginTime
				+ ", logoutTime=" + logoutTime + "]";
	}
}
