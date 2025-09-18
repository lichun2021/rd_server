package com.hawk.game.strengthenguide.msg;

import org.hawk.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
import com.hawk.game.strengthenguide.SGMsgInvoker;

@SuppressWarnings("unused")
public class SGBuildingLevelUpMsg extends SGMsgInvoker {
	public SGBuildingLevelUpMsg(Player player) {
		super(player, StrengthenGuideType.Building);
		this.player = player;
	}
}