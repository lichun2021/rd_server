package com.hawk.game.guild.championship;

public class GCMatchState {
	public boolean hasInit;
	
	public int termId = 0;

	public boolean finish = false;
	
	public boolean isHasInit() {
		return hasInit;
	}

	public void setHasInit(boolean hasInit) {
		this.hasInit = hasInit;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public final boolean isFinish() {
		return finish;
	}

	public final void setFinish(boolean finish) {
		this.finish = finish;
	}
}
