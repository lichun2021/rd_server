package com.hawk.game.superweapon.weapon.model;

import java.io.Serializable;

/**
 * 司令实体
 * @author zhenyu.shang
 * @since 2018年4月24日
 */
public class Commander implements Serializable{

	private static final long serialVersionUID = 1L;

	private String playerName;
	
	private String playerId;
	
	private String playerGuildId;
	
	private String playerGuildName;
	
	private String pfIcon;
	
	private int icon;
	
	/**
	 * 任职开始时间
	 */
	private long tenure;
	
	/**
	 * 任职届数
	 */
	private int tenureCount;

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
}
