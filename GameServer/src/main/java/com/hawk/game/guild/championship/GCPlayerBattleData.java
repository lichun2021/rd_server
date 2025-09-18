package com.hawk.game.guild.championship;

public class GCPlayerBattleData {
	// 玩家对战标识
	public String pBattleId;

	// 对战阶段
	public int battleStage;

	// 对战顺序角标
	public int posIndex;

	public String playerA;

	public String playerB;

	public String winnerPlayer;

	public String getpBattleId() {
		return pBattleId;
	}

	public void setpBattleId(String pBattleId) {
		this.pBattleId = pBattleId;
	}

	public int getBattleStage() {
		return battleStage;
	}

	public void setBattleStage(int battleStage) {
		this.battleStage = battleStage;
	}

	public int getPosIndex() {
		return posIndex;
	}

	public void setPosIndex(int posIndex) {
		this.posIndex = posIndex;
	}

	public String getPlayerA() {
		return playerA;
	}

	public void setPlayerA(String playerA) {
		this.playerA = playerA;
	}

	public String getPlayerB() {
		return playerB;
	}

	public void setPlayerB(String playerB) {
		this.playerB = playerB;
	}

	public String getWinnerPlayer() {
		return winnerPlayer;
	}

	public void setWinnerPlayer(String winnerPlayer) {
		this.winnerPlayer = winnerPlayer;
	}

}
