package com.hawk.game.strengthenguide.msg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.SGMsgInvoker;

public class SGPlayerTechnologyUpdgrdeMsg extends SGMsgInvoker {
	
	public SGPlayerTechnologyUpdgrdeMsg(Player player, int scienceId){
		super(player, StrengthenGuideType.Science);
	}
}
