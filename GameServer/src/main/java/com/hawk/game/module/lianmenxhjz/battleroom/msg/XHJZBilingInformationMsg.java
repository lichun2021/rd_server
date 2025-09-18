package com.hawk.game.module.lianmenxhjz.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.XHJZ.PBXHJZGameInfoSync;

/***
 * 结算信息
 */
public class XHJZBilingInformationMsg extends HawkMsg {
	private PBXHJZGameInfoSync lastSyncpb;
	private String roomId;

	public PBXHJZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBXHJZGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
