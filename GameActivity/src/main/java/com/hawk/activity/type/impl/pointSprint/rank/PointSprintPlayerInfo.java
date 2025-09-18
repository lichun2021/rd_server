package com.hawk.activity.type.impl.pointSprint.rank;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 巅峰荣耀玩家信息
 * @author Golden
 *
 */
public class PointSprintPlayerInfo implements SplitEntity {

	private String playerId;

	private String playerName;

	private String guildTag;

	private int icon;

	private String pfIcon;

	private String serverId;
	private int officerId;

	@Override
	public SplitEntity newInstance() {
		return new PointSprintPlayerInfo();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(playerId);
		dataList.add(playerName);
		dataList.add(guildTag);
		dataList.add(icon);
		dataList.add(pfIcon);
		dataList.add(serverId);
		dataList.add(officerId);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(8);
		playerId = dataArray.getString();
		playerName = dataArray.getString();
		guildTag = dataArray.getString();
		icon = dataArray.getInt();
		pfIcon = dataArray.getString();
		serverId = dataArray.getString();
		officerId = dataArray.getInt();
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

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
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

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getOfficerId() {
		return officerId;
	}

	public void setOfficerId(int officerId) {
		this.officerId = officerId;
	}

}
