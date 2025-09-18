package com.hawk.game.idipscript.item;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 删除指定数量的指定物品 -- 10282155
 *
 * localhost:8080/script/idip/4459
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4459")
public class DelSomeItemHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		int itemId = request.getJSONObject("body").getIntValue("ItemId");
		int num = request.getJSONObject("body").getIntValue("Value");
		if (num <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "request failed, invalid Value: " + num);
			return result;
		}
		
		int itemNum = player.getData().getItemNumByItemId(itemId);
		int finalNum = Math.min(itemNum, num);
		if (finalNum <= 0) {
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "request failed, player item num: " + finalNum);
			return result;
		}
		
		player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ItemDeleteMsgInvoker(player, request, itemId, num));
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");

		return result;
	}
	
	private static class ItemDeleteMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private JSONObject request;
		private int itemId;
		private int num;
		
		public ItemDeleteMsgInvoker(Player player, JSONObject request, int itemId, int num) {
			this.player = player;
			this.request = request;
			this.itemId = itemId;
			this.num = num;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			int itemNum = player.getData().getItemNumByItemId(itemId);
			int finalNum = Math.min(itemNum, num);
			if (finalNum <= 0) {
				HawkLog.errPrintln("delete some item failed, playerId: {}, itemId: {}, num: {}, itemNum: {}", player.getId(), itemId, num, itemNum);
				return false;
			}
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addItemConsume(itemId, finalNum, false);
			if (consume.checkConsume(player)) {
				consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			}

			// 记录敏感日志
			LogUtil.logIdipSensitivity(player, request, itemId, finalNum);
			HawkLog.logPrintln("delete some item success, playerId: {}, itemId: {}, num: {}, itemNum: {}", player.getId(), itemId, num, itemNum);
			return true;
		}
	}
}
