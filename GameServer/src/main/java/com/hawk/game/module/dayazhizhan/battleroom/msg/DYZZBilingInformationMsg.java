package com.hawk.game.module.dayazhizhan.battleroom.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;

/***
 * 结算信息
 */
public class DYZZBilingInformationMsg extends HawkMsg {
	private String roomId;
	private PBDYZZGameInfoSync lastSyncpb;

	private int baseHPA = 0;
	private int baseHPB = 0;
	
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public PBDYZZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBDYZZGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
		this.baseHPA = lastSyncpb.getGuildInfo(0).getBaseHP();
		this.baseHPB = lastSyncpb.getGuildInfo(1).getBaseHP();
	}

	public int getBaseHPA() {
		return baseHPA;
	}

	public void setBaseHPA(int baseHPA) {
		this.baseHPA = baseHPA;
	}

	public int getBaseHPB() {
		return baseHPB;
	}

	public void setBaseHPB(int baseHPB) {
		this.baseHPB = baseHPB;
	}
	
}
