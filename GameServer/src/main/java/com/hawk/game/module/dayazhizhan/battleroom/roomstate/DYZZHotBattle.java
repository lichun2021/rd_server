package com.hawk.game.module.dayazhizhan.battleroom.roomstate;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;

/**
 * 鏖战阶段
 * 
 * @author lwt
 * @date 2018年11月15日
 */
public class DYZZHotBattle extends IDYZZBattleRoomState {

	public DYZZHotBattle(DYZZBattleRoom room) {
		super(room);

	}
	
	@Override
	public void init() {
		// 广播战场
		ChatParames paramesBroad = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_341)
				.build();
		getParent().addWorldBroadcastMsg(paramesBroad);
	}

	@Override
	public boolean onTick() {
		super.onBattleTick();

		return true;
	}

}
