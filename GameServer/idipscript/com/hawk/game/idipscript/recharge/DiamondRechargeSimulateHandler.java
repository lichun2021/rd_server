package com.hawk.game.idipscript.recharge;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;


/**
 * 模拟个人充值金条请求 -- 10282160
 *
 * localhost:8081/idip/4469
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4469")
public class DiamondRechargeSimulateHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int diamonds = request.getJSONObject("body").getIntValue("Value");
		if (diamonds <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "request failed, invalid Value: " + diamonds);
			return result;
		}
		
		player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new RechargeSimulateMsgInvoker(player, diamonds));

		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, diamonds);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	private static class RechargeSimulateMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private int diamonds;
		
		public RechargeSimulateMsgInvoker(Player player, int diamonds) {
			this.player = player;
			this.diamonds = diamonds;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			int playerSaveAmt = player.getPlayerBaseEntity().getSaveAmt();
			int diamondsBefore = player.getDiamonds();
			player.increaseDiamond(diamonds, Action.IDIP_CHANGE_PLAYER_ATTR, null, DiamondPresentReason.COMPENSATION);
			int oldNum = player.getPlayerBaseEntity().getSaveAmt();
			player.getPlayerBaseEntity().setSaveAmt(oldNum + diamonds);
			player.rechargeSuccess(playerSaveAmt, diamonds, diamondsBefore);
			player.getPlayerBaseEntity().setSaveAmt(oldNum);
			return true;
		}
	}
}


