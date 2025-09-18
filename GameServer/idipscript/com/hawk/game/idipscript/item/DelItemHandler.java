package com.hawk.game.idipscript.item;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除物品 -- 10282024
 *
 * localhost:8080/script/idip/4171
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4171")
public class DelItemHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ItemDeleteMsgInvoker(player, request, itemId));
		} else {
			try {
				deleteItem(request, player, itemId);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "remove item failed");
				return result;
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");

		return result;
	}
	
	/**
	 * 删除物品
	 * 
	 * @param player
	 * @param itemId
	 */
	private static void deleteItem(JSONObject request, Player player, int itemId) {
		List<ItemEntity> removeItems =  player.getData().getItemsByItemId(itemId);
		if (removeItems.isEmpty()) {
			return;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		int count = 0;
		for (ItemEntity item : removeItems) {
			count += item.getItemCount();
		}
		
		consume.addItemConsume(itemId, count, false);
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, itemId, count);
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_DEL_ITEM_VALUE, true, null);
		}
	}
	
	public static class ItemDeleteMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private JSONObject request;
		private int itemId;
		
		public ItemDeleteMsgInvoker(Player player, JSONObject request, int itemId) {
			this.player = player;
			this.request = request;
			this.itemId = itemId;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			deleteItem(request, player, itemId);
			return true;
		}
	}
}
