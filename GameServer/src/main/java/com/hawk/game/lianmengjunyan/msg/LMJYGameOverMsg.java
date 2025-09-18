package com.hawk.game.lianmengjunyan.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.warcollege.LMJYExtraParam;

public class LMJYGameOverMsg extends HawkMsg {
	private boolean win;
	private int battleCfgId;
	private LMJYExtraParam extParm;
	private String winAward;

	private LMJYGameOverMsg() {
	}

	public static LMJYGameOverMsg valueOf(int battleCfgId, boolean win) {
		LMJYGameOverMsg msg = new LMJYGameOverMsg();
		msg.win = win;
		msg.battleCfgId = battleCfgId;
		return msg;
	}

	public LMJYExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(LMJYExtraParam extParm) {
		this.extParm = extParm;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public int getBattleCfgId() {
		return battleCfgId;
	}

	public void setBattleCfgId(int battleCfgId) {
		this.battleCfgId = battleCfgId;
	}

	public String getWinAward() {
		return winAward;
	}

	public void setWinAward(String winAward) {
		this.winAward = winAward;
	}

}
