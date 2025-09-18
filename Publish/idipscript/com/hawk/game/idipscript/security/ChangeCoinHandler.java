package com.hawk.game.idipscript.security;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改游戏币数量(AQ)
 *
 * localhost:8080/script/idip/4133
 * @param Partition 小区id
 * @param OpenId  用户openId
 * @param Value 修改值：-减+加
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4133")
public class ChangeCoinHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}

		int moneyCount = request.getJSONObject("body").getIntValue("Value");
		// 黄金添加上限为5000
		moneyCount = moneyCount > 0 ? Math.min(moneyCount, GameConstCfg.getInstance().getMoneyCntAddLimit()) : moneyCount;
				
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeCoinMsgInvoker(player, moneyCount));
		} else {
			changeCoin(player, moneyCount, player.getGold());
		}

		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, moneyCount);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 修改货币
	 * 
	 * @param player
	 * @param moneyCount
	 * @param moneyBefore
	 */
	private static void changeCoin(Player player, int moneyCount, int moneyBefore) {
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
	
	public static class ChangeCoinMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private int change;
		
		public ChangeCoinMsgInvoker(Player player, int change) {
			this.player = player;
			this.change = change;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changeCoin(player, change, player.getGold());
			return true;
		}
	}
}
