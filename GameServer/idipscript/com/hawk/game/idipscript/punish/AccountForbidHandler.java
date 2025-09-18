package com.hawk.game.idipscript.punish;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 封停账号 -- 10282005
 *
 * localhost:8080/script/idip/4105?Partition=&OpenId=&&BanTime=&BanReason=
 *
 * @param OpenId  用户openId
 * @param BanTime  封号时长
 * @param BanReason  封号原因
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4105", desc = "封停账号")
public class AccountForbidHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new AccountForbidMsgInvoker(player, request));
		} else {
			accountForbidden(player, request);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	private static void accountForbidden(Player player, JSONObject request) {
		int banTime = request.getJSONObject("body").getInteger("BanTime");
		long nowTime = HawkTime.getMillisecond();
		long forbidEndTime = nowTime + banTime * 1000L;
		String banReason = request.getJSONObject("body").getString("BanReason");
		
		player.getEntity().setForbidenTime(forbidEndTime);
		// 更新缓存状态
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), IdipUtil.decode(banReason) + "（解封时间：" + HawkTime.formatTime(forbidEndTime) + "）", nowTime, forbidEndTime, banTime);
		GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), forbidEndTime, player.getName());
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_ACCOUNT);
		LogUtil.logIdipSensitivity(player, request, 0, banTime);
	}
	
	public static class AccountForbidMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private JSONObject request;
		
		public AccountForbidMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			accountForbidden(player, request);
			player.kickout(Status.IdipMsgCode.IDIP_ACCOUNT_FORBID_OFFLINE_VALUE, true, null);
			return true;
		}
	}
	
}
