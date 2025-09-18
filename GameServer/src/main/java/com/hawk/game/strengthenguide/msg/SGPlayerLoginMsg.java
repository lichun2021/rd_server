package com.hawk.game.strengthenguide.msg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.SGMsgInvoker;
import com.hawk.game.strengthenguide.StrengthenGuideData;
import com.hawk.game.strengthenguide.op.SGPlayerEntityOP;

public class SGPlayerLoginMsg extends SGMsgInvoker {

	public SGPlayerLoginMsg(Player player) {
		super(player, StrengthenGuideType.Building);
	}

	@Override
	public void invoke() {
		if (invokeEnable()) {
			// 不要调用super 的 invoke 故意覆盖的
			// 从数据库加载
			StrengthenGuideData.onPlayerLogin(player.getId());
			// 更新整个玩家的数据
			SGPlayerEntityOP.updateWithPlayer(player);
		}
	}
}
