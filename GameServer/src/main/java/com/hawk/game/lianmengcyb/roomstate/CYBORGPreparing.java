package com.hawk.game.lianmengcyb.roomstate;

import org.hawk.os.HawkTime;

import com.hawk.game.lianmengcyb.CYBORGBattleRoom;

/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class CYBORGPreparing extends ICYBORGBattleRoomState {
	long lastTick;

	public CYBORGPreparing(CYBORGBattleRoom room) {
		super(room);
	}

	@Override
	public boolean onTick() {
		long timenow = HawkTime.getMillisecond();
		if (timenow - lastTick < 1000) {
			return true;
		}
		lastTick = timenow;
		if (timenow > getParent().getStartTime()) {
			getParent().setState(new CYBORGGameing(getParent()));
			return true;
		}

		return true;
	}
}
