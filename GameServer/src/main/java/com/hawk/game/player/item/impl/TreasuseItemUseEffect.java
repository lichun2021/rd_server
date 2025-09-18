package com.hawk.game.player.item.impl;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.world.WorldMarchService;

public class TreasuseItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.TREASURE_HUNT_TOOL_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		int maxMarchCnt = player.getMaxMarchNum();
		if (maxMarchCnt - marchCount < itemCount) {
			player.sendError(protoType, Status.Error.WORLD_MARCH_MAX_LIMIT_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		WorldMarchService.getInstance().startTreasureHuntMarch(player,itemCount);
		return true;
	}

}
