package com.hawk.game.player.item.impl;

import org.hawk.log.HawkLog;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.National.NationStatus;

public class NationBuildValItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.NATIONAL_BUILDING_VAL_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		if(player.isCsPlayer()) {
			player.sendError(protoType, Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE, 0);
			return false;
		}
		
		// 检查国家系统是否开启，也就是建设处等级是否大于0
		NationStatus nationStatus = NationService.getInstance().getNationStatus();
		if(nationStatus != NationStatus.COMPLETE){
			player.sendError(protoType, Status.Error.NATION_BUILDING_UNOPEN_VALUE, 0);
			return false;
		}
		// 检查建筑值是否到达上限
		int buildId = itemCfg.getNationBuild();
		NationalBuilding building = NationService.getInstance().getNationBuildingByTypeId(buildId);
		if(building == null){
			HawkLog.errPrintln("item cfg nation build id error, can not find nation build, buildId = {}", buildId);
			return false;
		}
		int limit = building.getBuildDayLimit();
		int current = building.getEntity().getBuildVal();
		// 如果已经到达上限了，则不能再使用
		if(limit - current <= 0) {
			player.sendError(protoType, Status.Error.NATION_BUILD_VAL_LIMIT_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		return NationService.getInstance().nationBuildValItemUseEffect(player, itemCfg, itemCount);
	}

}
