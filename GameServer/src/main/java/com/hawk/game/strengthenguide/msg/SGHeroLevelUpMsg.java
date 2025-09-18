package com.hawk.game.strengthenguide.msg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.SGMsgInvoker;

public class SGHeroLevelUpMsg extends SGMsgInvoker {

	public SGHeroLevelUpMsg(Player player) {
		super(player, StrengthenGuideType.Hero);
	}

}
