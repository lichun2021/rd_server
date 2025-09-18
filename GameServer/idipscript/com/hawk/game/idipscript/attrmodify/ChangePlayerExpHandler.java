package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改角色经验值 -- 10282012
 *
 * localhost:8080/script/idip/4121?OpenId=&RoleId=&Value=
 *
 * @param OpenId  用户openId
 * @param RoleId  playerId
 * @param Value 修改值：-减+加
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4121")
public class ChangePlayerExpHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int num = request.getJSONObject("body").getIntValue("Value");
		int before = player.getExp();
		int after = before + num;
		if(player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeExpMsgInvoker(player, num));
		} else {
			changeExp(player, num);
		}
		
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, num);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Value1", before);
		result.getBody().put("Value2", after < 0 ? 0 : after);
		return result;
	}
	
	/**
	 * 修改经验值
	 * @param player
	 * @param changeNum
	 */
	public static void changeExp(Player player, int changeNum) {
		if(changeNum > 0) {
			AwardItems award = AwardItems.valueOf();
			award.addExp(changeNum);
			award.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			return;
		} 
		
		int localNum = Math.abs(changeNum);
		int exp = player.getExp();
		ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.EXP, exp > localNum ? localNum : exp);
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
	}
	
	public static class ChangeExpMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private int change;
		
		public ChangeExpMsgInvoker(Player player, int change) {
			this.player = player;
			this.change = change;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changeExp(player, change);
			player.getPush().syncPlayerInfo();
			return true;
		}
	}
}
