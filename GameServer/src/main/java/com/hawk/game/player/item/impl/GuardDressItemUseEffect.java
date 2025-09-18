package com.hawk.game.player.item.impl;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.GuardianItemDressCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.invoker.guard.GuardDressUpdateInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.MapUtil;
import com.hawk.gamelib.GameConst;

public class GuardDressItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.GUARD_DRESS_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		GuardianItemDressCfg itemDressCfg = HawkConfigManager.getInstance().getConfigByKey(GuardianItemDressCfg.class, itemCfg.getId());
		int dressId = itemDressCfg.getSingleDressId();
		int validTime = MapUtil.getIntValue(player.getData().getPlayerOtherEntity().getDressItemInfoMap(), dressId);
		if (validTime < 0) {
			player.sendError(protoType, Status.Error.GUARD_DRESS_HAS_OWN_VALUE, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		GuardianItemDressCfg guardianItemDress = HawkConfigManager.getInstance().getConfigByKey(GuardianItemDressCfg.class, itemCfg.getId());
		int dressId = guardianItemDress.getSingleDressId();	
		//原来如果已经是永久的话
		Map<Integer, Integer> dressMap = player.getData().getPlayerOtherEntity().getDressItemInfoMap();
		int value = MapUtil.get(dressMap, dressId, HawkTime.getSeconds());
		//只有非永久的才加时间.
		if (value > 0) {
			if (guardianItemDress.getValidTime() < 0) {
				player.getData().getPlayerOtherEntity().addDressItemInfo(dressId, guardianItemDress.getValidTime());
			} else {
				player.getData().getPlayerOtherEntity().addDressItemInfo(dressId, guardianItemDress.getValidTime() + value);
			}
		}
		
		if (player.isCsPlayer()) {
			return false;
		}
		
		String guardPlayer = RelationService.getInstance().getGuardPlayer(player.getId());
		//如果没有守护对象同步一下自己的就可以了.
		if (HawkOSOperator.isEmptyString(guardPlayer)) {
			RelationService.getInstance().synGuardDressId(player.getId());
			return true;
		}
		
		if (CrossService.getInstance().isCrossPlayer(guardPlayer)) {
			return false;
		}
		
		//只有在非跨服的情况下才可以默认使用dress.		
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DRESS_UPDATE, new GuardDressUpdateInvoker(player, dressId, false));		
		return true;
	}

}
