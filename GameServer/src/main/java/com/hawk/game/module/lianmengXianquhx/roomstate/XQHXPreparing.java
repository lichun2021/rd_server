package com.hawk.game.module.lianmengXianquhx.roomstate;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;

/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class XQHXPreparing extends IXQHXBattleRoomState {
	long lastTick;

	public XQHXPreparing(XQHXBattleRoom room) {
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
			getParent().setState(new XQHXGameing(getParent()));
			return true;
		}

		return true;
	}
}
