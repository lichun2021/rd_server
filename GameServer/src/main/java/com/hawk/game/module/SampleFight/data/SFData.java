package com.hawk.game.module.SampleFight.data;

import java.util.List;

public class SFData {
	private List<SFUnit> atks;
	private List<SFUnit> defs;
	private int atkWin;

	public List<SFUnit> getAtks() {
		return atks;
	}

	public void setAtks(List<SFUnit> atks) {
		this.atks = atks;
	}

	public List<SFUnit> getDefs() {
		return defs;
	}

	public void setDefs(List<SFUnit> defs) {
		this.defs = defs;
	}

	public int getAtkWin() {
		return atkWin;
	}

	public void setAtkWin(int atkWin) {
		this.atkWin = atkWin;
	}

}
