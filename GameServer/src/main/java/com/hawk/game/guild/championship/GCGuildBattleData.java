package com.hawk.game.guild.championship;

import com.hawk.game.guild.championship.GCConst.GCBattleStage;

public class GCGuildBattleData {
	// 联盟对战标识
	public String gBattleId;

	// 对战阶段
	public GCBattleStage battleStage;

	// 对战顺序角标
	public int posIndex;

	public String guildA;

	public String guildB;

	public String winnerGuild;

	public String getgBattleId() {
		return gBattleId;
	}

	public void setgBattleId(String gBattleId) {
		this.gBattleId = gBattleId;
	}

	public GCBattleStage getBattleStage() {
		return battleStage;
	}

	public void setBattleStage(GCBattleStage battleStage) {
		this.battleStage = battleStage;
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
}
