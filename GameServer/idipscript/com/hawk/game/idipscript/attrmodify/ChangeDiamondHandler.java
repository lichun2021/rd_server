package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;

/**
 * 修改钻石数量(AQ) -- 10282803
 *
 * localhost:8080/script/idip/4135?Partition=&OpenId=&Value=
 * @param Partition 小区id
 * @param OpenId  用户openId
 * @param Value 修改值：-减+加
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4135")
public class ChangeDiamondHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}

		int moneyCount = request.getJSONObject("body").getIntValue("Value");
		// 钻石添加上限为5000
		moneyCount = moneyCount > 0 ? Math.min(moneyCount, GameConstCfg.getInstance().getMoneyCntAddLimit()) : moneyCount;
		
		if(player.isActiveOnline()) {
			// isLogin=0: 无需处理强制重新登录； isLogin=1: 做标记，待玩家下一次发起请求和服务端通信时，强制用户重新登录
			int isLogin = request.getJSONObject("body").getIntValue("IsLogin");
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeDiamondMsgInvoker(player, moneyCount, isLogin));
		} else {
			player.addMoneyReissueItem(moneyCount, Action.IDIP_CHANGE_PLAYER_ATTR, null);
		}

		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, moneyCount);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	private static class ChangeDiamondMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private int change;
		private int isLogin;
		
		public ChangeDiamondMsgInvoker(Player player, int change, int isLogin) {
			this.player = player;
			this.change = change;
			this.isLogin = isLogin;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			player.addMoneyReissueItem(change, Action.IDIP_CHANGE_PLAYER_ATTR, null);
			player.idipChangeDiamonds(true);
			if(isLogin == 1) {
				HawkLog.logPrintln("idip force player to offline, playerId: {}, changeDiamonds: {}", player.getId(), change);
				player.kickout(Status.IdipMsgCode.IDIP_CHANGE_DIAMOND_OFFLINE_VALUE, true, null);
			}
			return true;
		}
	}
}
