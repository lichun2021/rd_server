package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.alibaba.fastjson.JSONObject;

/**
 * 账号信息, 主要用来处理登陆校验
 *
 * @author hawk
 *
 */
@Entity
public class AccountInfo {
	@Id
	@Column(name = "playerId")
	protected String playerId;

	@Column(name = "puid")
	protected String puid;
	
	@Column(name = "serverId")
	protected String serverId;

	@Column(name = "forbidenTime")
	protected long forbidenTime = 0;
	
	@Column(name = "logoutTime")
	protected long logoutTime = 0;
	
	@Column(name = "playerName")
	protected String playerName = null;
	
	@Column(name = "updateTime")
	protected long updateTime = 0;
	
	@Column(name = "isActive")
	private boolean isActive = true;
	
	/**
	 * 是否是新玩家, 仅仅用来做数据初始化的优化, 别用作其他逻辑使用。
	 * 在assemble过程中有效，login消息时已设置为false
	 */
	@Transient
	private boolean newly;
	
	/**
	 * 是否是在出生中
	 */
	@Transient
	private boolean inBorn;
	
	/**
	 * 士兵是否被修复
	 */
	@Transient
	private boolean armyFixed;
	
	/**
	 * 登录时间标记
	 */
	@Transient
	private long loginTime;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public long getForbidenTime() {
		return forbidenTime;
	}

	public void setForbidenTime(long forbidenTime) {
		this.forbidenTime = forbidenTime;
	}
	
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public long getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(long logoutTime) {
		this.logoutTime = logoutTime;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("playerId", playerId);
		json.put("puid", puid);
		json.put("playerName", playerName);
		json.put("active", isActive);
		return json;
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@Override
	public String toString() {
		return toJSON().toJSONString();
	}
	
	public boolean isNewly() {
		return newly;
	}

	public void setNewly(boolean newly) {
		this.newly = newly;
	}
	
	public boolean isInBorn() {
		return inBorn;
	}

	public void setInBorn(boolean inBorn) {
		this.inBorn = inBorn;
	}

	public boolean isArmyFixed() {
		return armyFixed;
	}

	public void setArmyFixed(boolean armyFixed) {
		this.armyFixed = armyFixed;
	}
}
