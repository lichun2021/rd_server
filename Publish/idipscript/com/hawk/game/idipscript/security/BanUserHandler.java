package com.hawk.game.idipscript.security;

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
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 封停账号
 *
 * localhost:8080/script/idip/4147
 *
 * @param Partition 小区Id
 * @param OpenId  用户openId
 * @param BanTime  封号时长
 * @param BanReason  封号原因
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4147")
public class BanUserHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new BanUserMsgInvoker(player, request)); 
		} else {
			banUser(player, request, false);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 封停账号处理
	 * 
	 * @param player
	 * @param forbidEndTime
	 * @param banTime
	 * @param banReason
	 */
	private static void banUser(Player player, JSONObject request, boolean kickout) {
		String banReason = request.getJSONObject("body").getString("BanReason");
		int banTime = request.getJSONObject("body").getInteger("BanTime");
		long nowTime = HawkTime.getMillisecond();
		long forbidEndTime = nowTime + banTime * 1000L;
		player.getEntity().setForbidenTime(forbidEndTime);
		
		banReason = IdipUtil.decode(banReason);
		
		// 更新缓存状态
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), banReason, nowTime, forbidEndTime, banTime);
		GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), forbidEndTime, player.getName());
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_ACCOUNT);
		if (kickout) {
			player.sendIdipNotice(NoticeType.KICKOUT, NoticeMode.MSG_BOX, forbidEndTime, banReason);
			player.kickout(0, false, null);
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, banTime);
	}
	
	public static class BanUserMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public BanUserMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			banUser(player, request, true);
			return true;
		}
	}
}
