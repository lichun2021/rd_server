package com.hawk.game.script;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GsConst;

/**
 * 
 * localhost:8080/script/itemchange?playerId=hawk&items=type_id_count,type_id_count
 *
 * @author hawk
 *
 */
public class ItemChangeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			String items = params.get("items");
			if (HawkOSOperator.isEmptyString(items)) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "item params error");
			}

			AwardItems awardItems = AwardItems.valueOf(items);
			Iterator<ItemInfo> iterator = awardItems.getAwardItems().iterator();
			while (iterator.hasNext()) {
				ItemInfo info = iterator.next();
				
				if (info.getType() == GsConst.ITEM_TYPE_BASE * Const.ItemType.TOOL_VALUE && info.getCount() > 0) {
					List<ItemEntity> itemList = player.getData().getItemsByItemId(info.getItemId());
					if (itemList.size() == 0) {
						ItemEntity itemEntity = new ItemEntity();
						itemEntity.setItemId(info.getItemId());
						itemEntity.setItemCount((int) info.getCount());
						itemEntity.setPlayerId(player.getId());
						
						boolean success = false;
						if (GsConfig.getInstance().isEntityAsyncCreate()) {
							itemEntity.setId(HawkUUIDGenerator.genUUID());
							success = itemEntity.create(true);
						} else {
							success = itemEntity.create();
						}
						
						if (success) {
							player.getData().addItemEntity(itemEntity);
							player.getPush().syncItemInfo(itemEntity.getId());
						}
					} else if (itemList.size() == 1) {
						ItemEntity itemEntity = itemList.get(0);
						itemEntity.setItemCount(itemEntity.getItemCount() + (int)info.getCount());
						player.getPush().syncItemInfo(itemEntity.getId());
					}
				}
				
				if (info.getType() == GsConst.ITEM_TYPE_BASE * Const.ItemType.SOLDIER_VALUE && info.getCount() > 0) {
					List<ArmyEntity> armyList = player.getData().getArmyEntities();
					for (ArmyEntity entity : armyList) {
						if (entity.getArmyId() != info.getItemId()) {
							continue;
						}
						
						entity.addFree((int) info.getCount());
						player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT, entity.getArmyId());
						break;
					}
				}
			}
			
			// 返回执行完成
			return HawkScript.successResponse(null);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 执行异常
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
