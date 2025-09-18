package com.hawk.game.player.item.impl;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.WishingWellCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.WishingService;

public class WishItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.WISH_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		BuildingBaseEntity building = player.getData().getBuildingEntityByType(BuildingType.WISHING_WELL);
		if (building == null) {
			player.sendError(protoType, Status.Error.WISHING_WELL_LOCKED_VALUE, 0);
			return false;
		}
		
		int todayWishCount = player.getData().getWishingEntity().getTodayTotalWishCount();
		int level = player.getData().getBuildingMaxLevel(BuildingType.WISHING_WELL_VALUE);
		WishingWellCfg buildConfig = HawkConfigManager.getInstance().getConfigByKey(WishingWellCfg.class, level);
		if (buildConfig != null && todayWishCount >= buildConfig.getMaxCount()) {
			player.sendError(protoType, Status.Error.WISHING_TODAY_IS_MAX_COUNT_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		WishingService.getInstance().addWishingCount(player, itemCount);
		return true;
	}

}
