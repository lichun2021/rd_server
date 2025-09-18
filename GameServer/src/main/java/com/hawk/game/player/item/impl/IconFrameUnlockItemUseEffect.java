package com.hawk.game.player.item.impl;

import org.hawk.app.HawkApp;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.ImageType;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;

public class IconFrameUnlockItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.ICONFRAME_UNLOCK_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		PlayerUnlockImageMsg msg = new PlayerUnlockImageMsg(UnlockType.EFFECT, itemCfg.getId());
		msg.setType(ImageType.FRAME);
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		return true;
	}

}
