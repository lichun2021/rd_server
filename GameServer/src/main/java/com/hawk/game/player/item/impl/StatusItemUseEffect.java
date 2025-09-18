package com.hawk.game.player.item.impl;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;

public class StatusItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.STATUS_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		if (GameUtil.isResProduceUpEffect(itemCfg.getEffect())) {
			if (HawkOSOperator.isEmptyString(targetId)) {
				HawkLog.errPrintln("use item, params invalid, targetId is null, playerId: {}, itemId: {}", player.getId(), itemId);
				player.sendError(protoType, Status.SysError.PARAMS_INVALID_VALUE, 0);
				return false;
			}

			BuildingBaseEntity building = player.getData().getBuildingBaseEntity(targetId);
			if (building == null || !BuildingCfg.isResBuildingType(building.getType())) {
				HawkLog.errPrintln("use item, params invalid, target building type error, playerId: {}, itemId: {}, targetId: {}", player.getId(), itemId, targetId);
				player.sendError(protoType, Status.SysError.PARAMS_INVALID_VALUE, 0);
				return false;
			}
			return true;
		}
		
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, itemCfg.getBuffId());
		if (buffCfg == null) {
			HawkLog.errPrintln("use item, buff config error, playerId: {}, itemId: {}, buffId: {}, protocol: {}",
					player.getId(), itemId, itemCfg.getBuffId(), protoType);
			player.sendError(protoType, Status.SysError.CONFIG_ERROR_VALUE, 0);
			return false;
		}
		// 免战道具在有出征队列时不能使用（采集除外）
		if (buffCfg.getEffect() == Const.EffType.CITY_SHIELD_VALUE
				&& (WorldMarchService.getInstance().hasOffensiveMarch(player.getId()) || player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond())) {
			HawkLog.errPrintln("use item, buff error, playerId: {}, itemId: {}, buffId: {}, protocol: {}", player.getId(), itemId, itemCfg.getBuffId(), protoType);
			player.sendError(protoType, Status.Error.CANT_ADD_CITY_PROTECTED_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		GameUtil.addBuff(player, itemCfg, targetId);
		return true;
	}

}
