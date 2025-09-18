package com.hawk.game.service.starwars;

public class SWMatchState {
	public boolean hasInit;
	
	public int termId = 0;

	public boolean finish = false;
	
	public boolean firstWarFinsh = false;
	
	public boolean secondWarFinsh = false;
	
	public boolean thirdWarFinsh = false;
	
	
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

	public boolean isFirstWarFinsh() {
		return firstWarFinsh;
	}

	public void setFirstWarFinsh(boolean firstWarFinsh) {
		this.firstWarFinsh = firstWarFinsh;
	}

	public boolean isSecondWarFinsh() {
		return secondWarFinsh;
	}

	public void setSecondWarFinsh(boolean secondWarFinsh) {
		this.secondWarFinsh = secondWarFinsh;
	}

	public boolean isThirdWarFinsh() {
		return thirdWarFinsh;
	}

	public void setThirdWarFinsh(boolean thirdWarFinsh) {
		this.thirdWarFinsh = thirdWarFinsh;
	}
	
	
}
