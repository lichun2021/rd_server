package com.hawk.game.idipscript.security;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 禁言接口(AQ)
 *
 * localhost:8080/script/idip/4149
 *
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4149")
public class BanSendMsgHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new BanSendMsgInvoker(player, request));
		} else {
			banSendMsg(player, request);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	/**
	 * 禁止发言
	 * 
	 * @param player
	 * @param silentEndTime
	 * @param banTime
	 * @param banReason
	 */
	private static void banSendMsg(Player player, JSONObject request) {
		int banTime = request.getJSONObject("body").getInteger("BanTime");
		String banReason = request.getJSONObject("body").getString("BanSendMsgReason");
		
		long nowTime = HawkTime.getMillisecond();
		long silentEndTime = nowTime + banTime * 1000L;
		player.getEntity().setSilentTime(silentEndTime);
		// 更新缓存状态
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), IdipUtil.decode(banReason), nowTime, silentEndTime, banTime);
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_SEND_MSG);
		ChatService.getInstance().sendBanMsgNotice(player, silentEndTime);
		LogUtil.logIdipSensitivity(player, request, 0, banTime);
	}
	
	public static class BanSendMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public BanSendMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			banSendMsg(player, request);
			return true;
		}
	}
}
