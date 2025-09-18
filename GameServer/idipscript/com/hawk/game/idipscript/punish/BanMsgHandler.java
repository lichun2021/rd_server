package com.hawk.game.idipscript.punish;

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
 * 账号禁言 -- 10282077
 *
 * localhost:8080/script/idip/4279
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param OpenId     用户openId
 * @param BanTime    禁言时间：分钟
 * @param Reason     禁言原因
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4279")
public class BanMsgHandler extends IdipScriptHandler {
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
	 * @param request
	 */
	private static void banSendMsg(Player player, JSONObject request) {
		int banTime = request.getJSONObject("body").getInteger("BanTime");    // 禁言时间，单位：分钟
		String banReason = request.getJSONObject("body").getString("Reason"); // 禁言原因
		
		long nowTime = HawkTime.getMillisecond();
		int banSecond = banTime * 60;
		long silentEndTime = nowTime + banSecond * 1000L;
		player.getEntity().setSilentTime(silentEndTime);
		// 更新缓存状态
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), IdipUtil.decode(banReason) + "（解封时间：" + HawkTime.formatTime(silentEndTime) + "）", nowTime, silentEndTime, banSecond);
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_SEND_MSG);
		ChatService.getInstance().sendBanMsgNotice(player, silentEndTime);
		LogUtil.logIdipSensitivity(player, request, 0, banSecond);
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
