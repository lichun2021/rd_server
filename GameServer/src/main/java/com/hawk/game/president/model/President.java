package com.hawk.game.president.model;

import java.io.Serializable;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;

/**
 * 国王
 * @author zhenyu.shang
 * @since 2017年12月4日
 */
public class President implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String playerName;
	
	private String playerId;
	
	private String playerGuildId;
	
	private String playerGuildName;
	
	private String playerGuildTag;
	
	private int playerGuildFlag;
	
	private String pfIcon;
	
	private int icon;
	/**
	 * 上一任国王的ID
	 */
	private String lastPresidentPlayerId;
	/**
	 * 任职开始时间
	 */
	private long tenure;
	
	/**
	 * 任职届数
	 */
	private int tenureCount;
	/**
	 * 国王的区服
	 */
	private String serverId = "";

	public String getPlayerName() {
		return playerName;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getPlayerGuildId() {
		return playerGuildId;
	}

	public String getPlayerGuildName() {
		return playerGuildName;
	}

	public long getTenure() {
		return tenure;
	}

	public void setTenure(long tenure) {
		this.tenure = tenure;
	}

	public int getTenureCount() {
		return tenureCount;
	}

	public void setTenureCount(int tenureCount) {
		this.tenureCount = tenureCount;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setPlayerGuildId(String playerGuildId) {
		this.playerGuildId = playerGuildId;
	}

	public void setPlayerGuildName(String playerGuildName) {
		this.playerGuildName = playerGuildName;
	}

	public String getPfIcon() {
		return pfIcon;
	}

	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getPlayerGuildTag() {
		return playerGuildTag;
	}

	public void setPlayerGuildTag(String playerGuildTag) {
		this.playerGuildTag = playerGuildTag;
	}

	public int getPlayerGuildFlag() {
		return playerGuildFlag;
	}

	public void setPlayerGuildFlag(int playerGuildFlag) {
		this.playerGuildFlag = playerGuildFlag;
	}

	public String getLastPresidentPlayerId() {
		return lastPresidentPlayerId;
	}

	public void setLastPresidentPlayerId(String lastPresidentPlayerId) {
		this.lastPresidentPlayerId = lastPresidentPlayerId;
	}
	
	@JSONField(serialize = false)
	public String getServerIdByAssemble() {
		if (HawkOSOperator.isEmptyString(this.serverId)) {
			return GsConfig.getInstance().getServerId();
		} else {
			return GlobalData.getInstance().getMainServerId(this.serverId);  
		}
	}
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
