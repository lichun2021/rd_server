package com.hawk.game.lianmengxzq.worldpoint.data;

import java.io.Serializable;

/**
 * 司令实体
 * @author zhenyu.shang
 * @since 2018年4月24日
 */
public class XZQCommander implements Serializable{

	private static final long serialVersionUID = 1L;

	private String playerName;

	private String playerId;

	private String playerGuildId;

	private String playerGuildName;

	private String pfIcon;

	private int icon;

	private long time;

	private int termId;

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

	
	

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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
