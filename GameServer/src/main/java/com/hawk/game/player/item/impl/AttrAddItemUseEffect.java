package com.hawk.game.player.item.impl;

import org.hawk.log.HawkLog;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;

public class AttrAddItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.ADD_ATTR_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		if (player.isZeroEarningState()) {
			HawkLog.errPrintln("player on zero earning status, playerId: {}", player.getId());
			//player.sendIDIPZeroEarningMsg();
			player.sendError(protoType, Status.SysError.ZERO_EARNING_STATE_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		GameUtil.addPlayeAttr(player, itemCfg, itemCount, targetId, Action.TOOL_USE);
		return true;
	}

}
