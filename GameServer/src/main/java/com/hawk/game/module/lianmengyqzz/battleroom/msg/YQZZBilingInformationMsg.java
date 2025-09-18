package com.hawk.game.module.lianmengyqzz.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;

/***
 * 结算信息
 */
public class YQZZBilingInformationMsg extends HawkMsg {
	private String roomId;
	private PBYQZZGameInfoSync lastSyncpb;

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public PBYQZZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBYQZZGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

}
