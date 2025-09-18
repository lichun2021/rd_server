package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.List;

/**
 * 战队上次出战列表
 * @author Jesse
 *
 */
public class CWTeamLastJoins {
	public String id;

	public int termId;

	public List<String> joinList = new ArrayList<String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public List<String> getJoinList() {
		return joinList;
	}

	public void setJoinList(List<String> joinList) {
		this.joinList = joinList;
	}

	@Override
	public String toString() {
		return "CWTeamLastJoins [id=" + id + ", termId=" + termId + ", joinList=" + joinList + "]";
	}
}
