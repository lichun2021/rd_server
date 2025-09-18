package com.hawk.game.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 删除道具
 * 
 * localhost:8080/script/itemDel?playerName=l0001&itemIds=
 * 
 * @author lating
 * @param playerName
 * @param itemIds
 *
 */
public class PlayerItemDeleteHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			String itemIds = params.get("itemIds");
			if (player.isActiveOnline()) {
				player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ItemDeleteInvoker(player, itemIds));
			} else {
				delteItems(player, itemIds);
			}

			logger.info("script delete items, playerId: {}, playerName: {}, itemIds: {}", player.getId(), player.getName(), itemIds);

			return HawkScript.successResponse(null);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	
	/**
	 * 删除道具
	 * @param player
	 * @param itemIds
	 */
	static private void delteItems(Player player, String itemIds) {
		PlayerBaseEntity  base = player.getData().getPlayerBaseEntity();
		base.setDiamonds(0);
		base.setGold(0);
		base.setGoldore(0);
		base.setGoldoreUnsafe(0);
		base.setSteel(0);
		base.setSteelUnsafe(0);
		base.setOil(0);
		base.setOilUnsafe(0);
		base.setTombarthite(0);
		base.setTombarthiteUnsafe(0);
		player.getPush().syncPlayerInfo();
		List<ItemEntity> itemList = player.getData().getItemEntities();
		if (itemList.isEmpty()) {
			return;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemEntity> removes = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(itemIds)) {
			removes.addAll(itemList);
		} else {
			String[] items = itemIds.split(",");
			for (String itemId : items) {
				List<ItemEntity> removeItems = player.getData().getItemsByItemId(Integer.valueOf(itemId.trim()));
				if (!removeItems.isEmpty()) {
					removes.addAll(removeItems);
				}
			}
		}
		
		for (ItemEntity item : removes) {
			consume.addItemConsume(item.getItemId(), item.getItemCount(), false);
		}
		
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.GM_EXPLOIT);
		}
	}
	
	/**
	 * 消息回调处理器
	 * 
	 * @author hawk
	 *
	 */
	static class ItemDeleteInvoker extends HawkMsgInvoker {
		private Player player;
		private String itemIds;
		
		ItemDeleteInvoker(Player player, String itemIds) {
			this.player = player;
			this.itemIds = itemIds;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			delteItems(player, itemIds);
			return true;
		}
	}
}
