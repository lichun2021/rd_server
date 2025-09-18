package com.hawk.game.player.item.impl;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.util.GameUtil;

public class TrainQuantityAddItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.TRAIN_QUANTITY_ADD_PERIOD_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		GameUtil.addBuff(player, itemCfg, targetId);
		return true;
	}

}
