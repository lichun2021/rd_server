package com.hawk.game.module.lianmengyqzz.battleroom.roomstate;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;


/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class YQZZPreparing extends IYQZZBattleRoomState {
	long lastTick;

	public YQZZPreparing(YQZZBattleRoom room) {
		super(room);
	}

	@Override
	public boolean onTick() {
		long timenow = HawkTime.getMillisecond();
		if (timenow - lastTick < 1000) {
			return true;
		}
		lastTick = timenow;
		if (timenow > getParent().getGameStartTime()) {
			getParent().setState(new YQZZGameing(getParent()));
			return true;
		}

		return true;
	}
}
