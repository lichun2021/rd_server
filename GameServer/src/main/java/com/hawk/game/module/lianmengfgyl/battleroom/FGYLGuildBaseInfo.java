package com.hawk.game.module.lianmengfgyl.battleroom;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.protocol.Const.EffType;

public class FGYLGuildBaseInfo {
	private FGYL_CAMP camp = FGYL_CAMP.YURI;
	private String guildId = "QXM";
	private String guildName = "七玄门";
	private String guildTag = "";
	private String serverId = "";
	private int guildFlag;
	public ImmutableMap<EffType, Integer> battleEffVal = ImmutableMap.of();

	public final static FGYLGuildBaseInfo defaultInstance = new FGYLGuildBaseInfo();

	public static FGYLGuildBaseInfo getDefaultinstance() {
		return defaultInstance;
	}

	public FGYL_CAMP getCamp() {
		return camp;
	}

	public void setCamp(FGYL_CAMP camp) {
		this.camp = camp;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String campGuild) {
		this.guildId = campGuild;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String campGuildName) {
		this.guildName = campGuildName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String campGuildTag) {
		this.guildTag = campGuildTag;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String campServerId) {
		this.serverId = campServerId;
	}

	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int campguildFlag) {
		this.guildFlag = campguildFlag;
	}

}
