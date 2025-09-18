package com.hawk.game.lianmengjunyan.roomstate;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg.QuitReason;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;

/** 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日 */
public class LMJYPreparing extends ILMJYBattleRoomState {
	long lastTick;

	public LMJYPreparing(LMJYBattleRoom room) {
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
			List<ILMJYPlayer> notJoinList = getParent().getPlayerList(PState.PREJOIN);
			for (ILMJYPlayer player : notJoinList) {
				getParent().quitWorld(player,QuitReason.PREPAROVER);
			}
			getParent().setState(new LMJYGameing(getParent()));
			return true;
		}

		return true;
	}
}
