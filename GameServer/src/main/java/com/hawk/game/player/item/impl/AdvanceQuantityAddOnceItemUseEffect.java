package com.hawk.game.player.item.impl;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.item.AbstractItemUseEffect;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Status;

public class AdvanceQuantityAddOnceItemUseEffect extends AbstractItemUseEffect {

	@Override
	public int itemType() {
		return Const.ToolType.ADVANCE_QUANTITY_ADD_ONCE_VALUE;
	}

	@Override
	public boolean useItemCheck(Player player, ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		String key = CustomKeyCfg.getQuantityAddKey(itemCfg.getItemType());
		CustomDataEntity entity = player.getData().getCustomDataEntity(key);
		if (entity != null && entity.getValue() > 0) {
			int onceAddNum = 0;
			if (!HawkOSOperator.isEmptyString(entity.getArg())) {
				onceAddNum = Integer.parseInt(entity.getArg());
			} else if (entity.getValue() == itemCfg.getNum()) {
				onceAddNum = itemCfg.getNum();  // 兼容历史数据
			}
			
			if (onceAddNum != itemCfg.getNum()) {
				player.sendError(protoType, Status.Error.TRAIN_ADVANCE_ADD_FAILD, 0);
				HawkLog.errPrintln("use item, trainQuantityAddOnce buff already exist, playerId: {}, itemId: {}, value: {}, onceAddNum: {}, protocol: {}", 
						player.getId(), itemId, entity.getValue(), onceAddNum, protoType);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean useEffect(Player player, ItemCfg itemCfg, int itemCount, String targetId) {
		String key = CustomKeyCfg.getQuantityAddKey(itemCfg.getItemType());
		CustomDataEntity entity = player.getData().getCustomDataEntity(key);
		if (entity == null) {
			entity = player.getData().createCustomDataEntity(key, itemCount, String.valueOf(itemCfg.getNum()));
		} else {
			if (HawkOSOperator.isEmptyString(entity.getArg())) {
				entity.setValue(entity.getValue() > 0 ? itemCount + 1 : itemCount); // 兼容历史数据
			} else {
				entity.setValue(entity.getValue() + itemCount);
			}
			entity.setArg(String.valueOf(itemCfg.getNum()));
		}
		player.getPush().syncCustomData();
		return true;
	}

}
