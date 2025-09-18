package com.hawk.game.module.dayazhizhan.battleroom.roomstate;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;

/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class DYZZPreparing extends IDYZZBattleRoomState {

	public DYZZPreparing(DYZZBattleRoom room) {
		super(room);
	}

	@Override
	public boolean onTick() {
		if (getParent().getCurTimeMil() > getParent().getCollectStartTime()) {
			getParent().setState(new DYZZCollect(getParent()));
			return true;
		}

		return true;
	}
}
