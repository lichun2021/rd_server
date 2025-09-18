package com.hawk.game.module.dayazhizhan.battleroom.roomstate;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 游戏中
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class DYZZBattle extends IDYZZBattleRoomState {

	public DYZZBattle(DYZZBattleRoom room) {
		super(room);

	}
	
	@Override
	public void init() {
		// 广播战场
		ChatParames paramesBroad = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_340)
				.build();
		getParent().addWorldBroadcastMsg(paramesBroad);
	}

	@Override
	public boolean onTick() {

		if (getParent().getCurTimeMil() > getParent().getHotBattleStartTime()) {
			getParent().setState(new DYZZHotBattle(getParent()));
			return true;
		}

		super.onBattleTick();

		return true;
	}

}
