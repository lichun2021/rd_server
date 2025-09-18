package com.hawk.game.module.lianmenxhjz.battleroom.roomstate;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;


/**
 * 准备阶段. 可以进入游戏. 倒计时
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class XHJZPreparing extends IXHJZBattleRoomState {
	long lastTick;

	public XHJZPreparing(XHJZBattleRoom room) {
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
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XHJZ_START)
					.build();
			getParent().addWorldBroadcastMsg(parames);
			getParent().setState(new XHJZGameing(getParent()));
			return true;
		}

		return true;
	}
}
