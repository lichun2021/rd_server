package com.hawk.game.module.lianmengfgyl.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.FGYL.PBFGYLGameInfoSync;

/***
 * 结算信息
 */
public class FGYLBilingInformationMsg extends HawkMsg {
	private PBFGYLGameInfoSync lastSyncpb;
	private String roomId;

	public PBFGYLGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBFGYLGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
