package com.hawk.game.player.item.impl;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;

public class GuardExpItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.GUARD_EXP_ITEM_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (HawkOSOperator.isEmptyString(guardPlayerId)) {
			player.sendError(protoType, Status.Error.GUARD_EXP_ITEM_USE_ERROR_VALUE, 0);
			return false;
		}
		PlayerRelationEntity playerRelationEnitty = RelationService.getInstance().getPlayerRelationEntity(player.getId(), guardPlayerId);
		PlayerRelationEntity targetPlayerEntity = RelationService.getInstance().getPlayerRelationEntity(guardPlayerId, player.getId());
		if (playerRelationEnitty == null || targetPlayerEntity == null) {
			player.sendError(protoType, Status.Error.GUARD_EXP_ITEM_USE_ERROR_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		// 守护值经验道具
		String guardPlayerId = RelationService.getInstance().getGuardPlayer(player.getId());
		if (!HawkOSOperator.isEmptyString(guardPlayerId)) {
			PlayerRelationEntity relationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), guardPlayerId);
			PlayerRelationEntity targetRelationEntity = RelationService.getInstance().getPlayerRelationEntity(guardPlayerId, player.getId());
			if (relationEntity != null && targetRelationEntity != null) {
				int addValue = itemCfg.getNum() * itemCount;
				RelationService.getInstance().addGuardExp(player.getId(), guardPlayerId, addValue, relationEntity, targetRelationEntity);
			}
		}
		return true;
	}

}
