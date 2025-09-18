package com.hawk.game.player.item.impl;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.util.GsConst;

public class RandomCityMoveItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.RANDOM_MOVE_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		PlayerWorldModule worldModule = player.getModule(GsConst.ModuleType.WORLD_MODULE);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		if (!worldModule.moveCityCheck(HP.code.WORLD_MOVE_CITY_C_VALUE, consumeItems, CityMoveType.RANDOM_MOVE_VALUE, false, 0, 0, false)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		PlayerWorldModule worldModule = player.getModule(GsConst.ModuleType.WORLD_MODULE);
		worldModule.moveCity(CityMoveType.RANDOM_MOVE_VALUE, 0, 0, false, false);
		return true;
	}

}
