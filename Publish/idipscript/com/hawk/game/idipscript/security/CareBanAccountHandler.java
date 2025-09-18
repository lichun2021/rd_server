package com.hawk.game.idipscript.security;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 成长守护平台封停账号
 *
 * @param OpenId  用户openId
 * @param Type    禁玩类型（比如，1：禁玩，2：解禁）
 * @param Msg     禁玩原因或描述，如果是解禁此参数可以忽略
 * @param StartTime 禁玩开始时间（整型时间戳），如果是解禁此参数可以忽略
 * @param EndTime   禁玩结束时间（整型时间戳），如果是解禁此参数可以忽略
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4277")
public class CareBanAccountHandler extends IdipScriptHandler {
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
	 */
	private static void banUser(Player player, JSONObject request, boolean kickout) {
		if (!kickout) {
			return;
		}
		
		String banMsg = request.getJSONObject("body").getString("Msg");
		long startTime = request.getJSONObject("body").getLong("BeginTime") * 1000L;
		banMsg = IdipUtil.decode(banMsg);
		if (startTime <= HawkApp.getInstance().getCurrentTime()) {
			long endTime = request.getJSONObject("body").getLong("EndTime") * 1000L;
			player.sendIdipNotice(NoticeType.KICKOUT, NoticeMode.MSG_BOX, endTime, banMsg);
			player.kickout(0, false, null);
		} else {
			player.setCareBanStartTime(startTime);
		}
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
