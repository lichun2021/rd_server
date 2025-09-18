package com.hawk.game.module.lianmengfgyl.battleroom.roomstate;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;


/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class FGYLPreparing extends IFGYLBattleRoomState {
	long lastTick;

	public FGYLPreparing(FGYLBattleRoom room) {
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
			getParent().setState(new FGYLGameing(getParent()));
			return true;
		}

		return true;
	}
}
