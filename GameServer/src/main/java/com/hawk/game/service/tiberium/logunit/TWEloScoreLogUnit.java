package com.hawk.game.service.tiberium.logunit;

import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.tiberium.TiberiumConst.EloReason;

/**
 * 泰伯利亚elo积分变更流水
 * 
 * @author z
 *
 */
public class TWEloScoreLogUnit {
	private String guildId;

	private int termId;

	private int scoreBef;

	private int scoreAft;

	private int changeNum;

	private TiberiumConst.EloReason reason;

	public TWEloScoreLogUnit(String guildId, int termId, int scoreBef, int scoreAft, int changeNum, EloReason reason) {
		super();
		this.guildId = guildId;
		this.termId = termId;
		this.scoreBef = scoreBef;
		this.scoreAft = scoreAft;
		this.changeNum = changeNum;
		this.reason = reason;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getScoreBef() {
		return scoreBef;
	}

	public void setScoreBef(int scoreBef) {
		this.scoreBef = scoreBef;
	}

	public int getScoreAft() {
		return scoreAft;
	}

	public void setScoreAft(int scoreAft) {
		this.scoreAft = scoreAft;
	}

	public int getChangeNum() {
		return changeNum;
	}

	public void setChangeNum(int changeNum) {
		this.changeNum = changeNum;
	}

	public TiberiumConst.EloReason getReason() {
		return reason;
	}

	public void setReason(TiberiumConst.EloReason reason) {
		this.reason = reason;
	}
}
