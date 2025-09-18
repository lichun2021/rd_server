package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改水晶数量 -- 10282020
 *
 * localhost:8080/script/idip/4163
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4163")
public class ChangeGoldHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		// 水晶数量，协议中未定义此字段，需要加上
		int moneyCount = request.getJSONObject("body").getIntValue("Value");
		int moneyBefore = player.getGold();
		int moneyAfter = moneyBefore + moneyCount;
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new GoldChangedMsgInvoker(player, moneyCount));
		} else {
			try {
				changeGold(player, moneyCount, moneyBefore);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "change gold failed");
				return result;
			}
			
			moneyAfter = player.getGold();
		}

		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, moneyCount);
		result.getBody().put("Value", moneyCount);
		result.getBody().put("BeginValue", moneyBefore);
		result.getBody().put("EndValue", moneyAfter < 0 ? 0 : moneyAfter);

		return result;
	}
	
	/**
	 * 修改水晶数量
	 * @param player
	 * @param moneyCount
	 * @param moneyBefore
	 */
	private static void changeGold(Player player, int moneyCount, int moneyBefore) {
		if (moneyCount > 0) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLD_VALUE, moneyCount);
			awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			return;
		} 
		
		int localMoney = Math.abs(moneyCount);
		ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.GOLD, moneyBefore > localMoney ? localMoney : moneyBefore);
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
	}
	
	public static class GoldChangedMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private int change;
		
		public GoldChangedMsgInvoker(Player player, int change) {
			this.player = player;
			this.change = change;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			int moneyBefore = player.getGold();
			changeGold(player, change, moneyBefore);
			return true;
		}
	}
}
