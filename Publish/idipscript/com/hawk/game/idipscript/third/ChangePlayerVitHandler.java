package com.hawk.game.idipscript.third;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
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
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改体力值
 *
 * localhost:8080/script/idip/4195
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4195")
public class ChangePlayerVitHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeVitMsgInvoker(player, request));
		} else {
			try {
				changePlayerVit(request, player);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "vit change failed");
				return result;
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class ChangeVitMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public ChangeVitMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changePlayerVit(request, player);
			return true;
		}
	}
	
	/**
	 * 修改体力
	 * @param request
	 * @param player
	 */
	private static void changePlayerVit(JSONObject request, Player player) {
		int value = request.getJSONObject("body").getIntValue("Value");
		if (value > 0) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addVit(value);
			awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR, true);
		} else {
			ConsumeItems consume = ConsumeItems.valueOf();
			int playerVit = player.getVit();
			int consumeVit = Math.abs(value);
			consume.addConsumeInfo(PlayerAttr.VIT, consumeVit > playerVit ? playerVit : consumeVit);
			if (consume.checkConsume(player)) {
				consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			}
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, value);
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_CHANGE_VIT_VALUE, true, null);
		}
		HawkLog.logPrintln("idip change player vit, playerId: {}, value: {}", player.getId(), value);
	}
}
