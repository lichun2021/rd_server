package com.hawk.game.service.tiberium;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.service.tiberium.TiberiumConst.TLWBattleType;

public class TLWBattleData {
	
	//期数
	public int termId;
	// 联盟对战标识
	public String roomId;
	// 对战序号
	public int posIndex;

	public String guildA;

	public String guildB;
	
	public String winnerGuild;
	
	public TLWBattleType battleType;

	public int serverType;
	
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getPosIndex() {
		return posIndex;
	}

	public void setPosIndex(int posIndex) {
		this.posIndex = posIndex;
	}

	public String getGuildA() {
		return guildA;
	}

	public void setGuildA(String guildA) {
		this.guildA = guildA;
	}

	public String getGuildB() {
		return guildB;
	}

	public void setGuildB(String guildB) {
		this.guildB = guildB;
	}

	public String getWinnerGuild() {
		return winnerGuild;
	}

	public void setWinnerGuild(String winnerGuild) {
		this.winnerGuild = winnerGuild;
	}
	
	public TLWBattleType getBattleType() {
		return battleType;
	}
	
	public void setBattleType(TLWBattleType battleType) {
		this.battleType = battleType;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	@JSONField(serialize = false)
	public String getLossGuild(){
		if(HawkOSOperator.isEmptyString(this.winnerGuild)){
			return null;
		}
		if(this.guildA.equals(this.winnerGuild)){
			return this.guildB;
		}
		return this.guildA;
	}
}
