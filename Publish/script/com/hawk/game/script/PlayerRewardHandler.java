package com.hawk.game.script;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

/**
 * 加 属性 道具 兵种   ---   (不支持减)
 * 
 * localhost:8080/script/reward?playerName=l0001&items=type_id_count,type_id_count&reset=true
 * http://localhost:8080/script/reward?playerId=5c3749e7debd46c2a79712dae5aaea45&items=10000_1004_10000&reset=true
 *
 * playerId: 玩家Id
 * playerName: 玩家名字
 * reset: 设置数据
 * --------------------以下参数可选--------------------
 * 属性数100 :  10000_id_100
 * 道具数100 :  30000_id_100
 * 兵种数100 :  70000_id_100
 * 
 * @author hawk
 *
 */
public class PlayerRewardHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			boolean isReset = false;
			if (params.containsKey("reset")) {
				isReset = "true".equals(params.get("reset"));
			}

			String items = params.get("items");
			if (HawkOSOperator.isEmptyString(items)) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "items set error");
			}

			// 给玩家发奖
			AwardItems awardItems = AwardItems.valueOf(items);
			List<ItemInfo> itemInfos = awardItems.getAwardItems();

			// 过滤不合法数据
			Iterator<ItemInfo> iterator = itemInfos.iterator();
			while (iterator.hasNext()) {
				ItemInfo info = iterator.next();
				if (info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.PLAYER_ATTR_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.TOOL_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.SOLDIER_VALUE
						&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.EQUIP_VALUE
								&& info.getType() != GsConst.ITEM_TYPE_BASE * Const.ItemType.ARMOUR_VALUE
						|| info.getCount() < 0) {
					return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "item info error " + info.toString());
				}
			}

			if (isReset) {

				ConsumeItems comsumeItems = ConsumeItems.valueOf();
				for (ItemInfo itemInfo : itemInfos) {

					if (itemInfo.getType() == GsConst.ITEM_TYPE_BASE * Const.ItemType.PLAYER_ATTR_VALUE) {
						switch (itemInfo.getItemId()) {
						case PlayerAttr.COIN_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.COIN, player.getCoin());
							break;

						case PlayerAttr.GOLD_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.GOLD, player.getGold());
							break;
						case PlayerAttr.DIAMOND_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.DIAMOND, player.getDiamonds());
							break;
						case PlayerAttr.VIT_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.VIT, player.getVit());
							break;

						case PlayerAttr.GOLDORE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.GOLDORE, (int) player.getGoldore());
							break;
						case PlayerAttr.GOLDORE_UNSAFE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.GOLDORE_UNSAFE, (int) player.getGoldoreUnsafe());
							break;

						case PlayerAttr.OIL_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.OIL, (int) player.getOil());
							break;
						case PlayerAttr.OIL_UNSAFE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.OIL_UNSAFE, (int) player.getOilUnsafe());
							break;

						case PlayerAttr.STEEL_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.STEEL, (int) player.getSteel());
							break;
						case PlayerAttr.STEEL_UNSAFE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.STEEL_UNSAFE, (int) player.getSteelUnsafe());
							break;

						case PlayerAttr.TOMBARTHITE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.TOMBARTHITE, (int) player.getTombarthite());
							break;
						case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.TOMBARTHITE_UNSAFE, (int) player.getTombarthiteUnsafe());
							break;
							
						case PlayerAttr.GUILD_CONTRIBUTION_VALUE:
							comsumeItems.addConsumeInfo(PlayerAttr.GUILD_CONTRIBUTION, (int) player.getGuildContribution());
							break;

						default:
							break;
						}
					} else {

						List<ItemEntity> itemEntitys = player.getData().getItemsByItemId(itemInfo.getItemId());
						if (itemEntitys == null || itemEntitys.size() <= 0) {
							break;
						}
						for (ItemEntity itemEntity : itemEntitys) {
							if (itemEntity.getItemId() == itemInfo.getItemId()) {
								comsumeItems.addConsumeInfo(ItemType.valueOf(itemInfo.getType() / GsConst.ITEM_TYPE_BASE), itemEntity.getId(), itemEntity.getItemId(), itemEntity.getItemCount());
							}
						}
					}
				}
				
				if (comsumeItems.checkConsume(player)) {
					comsumeItems.consumeAndPush(player, Action.GM_EXPLOIT);
				}
			}
			awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);

			logger.info("gm reward, playerId: {}, playerName: {}, reward: {}, isReset: {}", player.getId(), player.getName(), awardItems.toDbString(), isReset);

			return HawkScript.successResponse(null);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
