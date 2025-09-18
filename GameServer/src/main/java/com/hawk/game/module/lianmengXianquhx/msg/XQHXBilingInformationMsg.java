package com.hawk.game.module.lianmengXianquhx.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.google.common.base.Objects;
import com.hawk.game.protocol.XQHX.PBGuildInfo;
import com.hawk.game.protocol.XQHX.PBXQHXGameInfoSync;

/***
 * 结算信息
 */
public class XQHXBilingInformationMsg extends HawkMsg {
	private PBXQHXGameInfoSync lastSyncpb;
	private String roomId;
	private String winGuild;

	public PBGuildInfo getGuildInfo(String guildid) {
		List<PBGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBGuildInfo ginfo : guildList) {
			if (Objects.equal(guildid, ginfo.getGuildId())) {
				return ginfo;
			}
		}
		return null;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public PBXQHXGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBXQHXGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public String getWinGuild() {
		return winGuild;
	}

	public void setWinGuild(String winGuild) {
		this.winGuild = winGuild;
	}

}
