package com.hawk.game.service.warFlag;

/**
 * 战旗状态：已解锁
 * @author golden
 *
 */
public class FlagUnlock implements FlagState {

	public IFlag flag;
	
	public FlagUnlock(IFlag flag) {
		this.flag = flag;
	}
	
	@Override
	public void stateTick() {
		
	}

	@Override
	public void resTick() {
		
	}

	@Override
	public boolean hasManor() {
		return false;
	}

	@Override
	public int getCurrBuildLife() {
		return 0;
	}

	@Override
	public int getCurrOccupyLife() {
		return 0;
	}

	@Override
	public void marchReturn() {
		
	}

	@Override
	public void marchReach(String guildId) {
		
	}

	@Override
	public boolean isBuildComplete() {
		return false;
	}

	@Override
	public boolean isPlaced() {
		return false;
	}

	@Override
	public boolean canTakeBack() {
		return false;
	}

	@Override
	public boolean canPlace() {
		return true;
	}
	
	@Override
	public boolean canCenterTick() {
		return false;
	}
}
