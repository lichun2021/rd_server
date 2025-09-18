package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 排行榜联盟信息 （服务器名称/联盟简称/联盟全称/盟主角色名）
 */
public class RankGuildInfo implements SplitEntity {

	private String guildId;
	
	private String guildName;
	
	private String guildTag;
	
	private String leaderId;
	
	private String leaderName;
	
	private String serverId;
	
	private int icon;
	
	private String pfIcon;
	
	@Override
	public SplitEntity newInstance() {
		return new RankGuildInfo();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(guildId);
		dataList.add(guildName);
		dataList.add(guildTag);
		dataList.add(leaderId);
		dataList.add(leaderName);
		dataList.add(serverId);
		dataList.add(icon);
		dataList.add(pfIcon);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(8);
		guildId = dataArray.getString();
		guildName = dataArray.getString();
		guildTag = dataArray.getString();
		leaderId = dataArray.getString();
		leaderName = dataArray.getString();
		serverId = dataArray.getString();
		icon = dataArray.getInt();
		pfIcon = dataArray.getString();
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
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

	public String toString() {
		return SerializeHelper.toSerializeString(this, SerializeHelper.COLON_ITEMS);
	}

	public static RankGuildInfo parseObj(String str) {
		return SerializeHelper.getValue(RankGuildInfo.class, str, SerializeHelper.COLON_ITEMS);
	}
}
