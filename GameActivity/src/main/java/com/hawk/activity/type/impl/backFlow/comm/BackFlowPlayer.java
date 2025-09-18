package com.hawk.activity.type.impl.backFlow.comm;

import java.util.Date;

import org.hawk.os.HawkTime;

public class BackFlowPlayer{
	
	//openId
	private String openId;
	//回流角色ID
	private String playerId;
	//角色等级
	private int playerLevel;
	//角色VIP等级
	private int vipLevel;
	//主城等级
	private int cityLevel;
	//战斗力
	private long battlePoint;
	//角色所在serverID
	private String serverId;
	//角色登录平台
	private String platform;
	//登录时间
	private long loginTime;
	//离线时间
	private long logoutTime;
	//回流次数(针对openId)
	private int backCount;
	//回流时间点
	private long backTimeStamp;
	//账号推送回归消时间点
	private long pushBackMessageTime;
	
	
	
	
	
	/**
	 * 获取流失天数
	 * @return
	 */
	public int getLossDays(){
		long lossBeginTime = this.logoutTime + HawkTime.DAY_MILLI_SECONDS;
		Date lossBegin = new Date(lossBeginTime);
		Date lossOver = new Date(this.backTimeStamp);
		return HawkTime.calcBetweenDays(lossBegin, lossOver);
	}
	
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
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
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
	
	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
	}

	public long getBackTimeStamp() {
		return backTimeStamp;
	}
	public void setBackTimeStamp(long backTimeStamp) {
		this.backTimeStamp = backTimeStamp;
	}

	public long getPushBackMessageTime() {
		return pushBackMessageTime;
	}

	public void setPushBackMessageTime(long pushBackMessageTime) {
		this.pushBackMessageTime = pushBackMessageTime;
	}

	
	
	
	
	
	
	
	
	
	
}
