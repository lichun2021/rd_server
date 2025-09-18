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
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.rank.RankService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 零收益接口(AQ)
 *
 * localhost:8080/script/idip/4145
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4145")
public class ZeroEarningHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ZeroEarningMsgInvoker(player, request));
		} else {
			setZeroEarning(player, request, false);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class ZeroEarningMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public ZeroEarningMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			setZeroEarning(player, request, true);
			return true;
		}
	}
	
	/**
	 * 零收益处理
	 * 
	 * @param player
	 * @param zeroEarningTime
	 * @param time
	 * @param zeroEarningReason
	 */
	private static void setZeroEarning(Player player, JSONObject request, boolean kickout) {
		String zeroEarningReason = request.getJSONObject("body").getString("ZeroEarningReason");
		String reason = IdipUtil.decode(zeroEarningReason);
		int time = request.getJSONObject("body").getInteger("Time");
		long nowTime = HawkTime.getMillisecond();
		long zeroEarningTime = nowTime + time * 1000L;
		
		player.getEntity().setZeroEarningTime(zeroEarningTime);
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason, nowTime, zeroEarningTime, time);
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_ZERO_EARNING);
		// 禁止参与排行榜处理
		RankService.getInstance().dealMsg(MsgId.PERSONAL_RANK_BAN, new PersonalRankAllBanMsgInvoker(player, reason, zeroEarningTime));
		if (kickout) {
			player.sendIdipNotice(NoticeType.KICKOUT, NoticeMode.MSG_BOX, zeroEarningTime, reason);
			player.kickout(0, false, null);
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, time);
	}
	
	public static class PersonalRankAllBanMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private String reason;
		private long zeroEarningTime;
		
		public PersonalRankAllBanMsgInvoker(Player player, String reason, long zeroEarningTime) {
			this.player = player;
			this.reason = reason;
			this.zeroEarningTime = zeroEarningTime;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
				GlobalData.getInstance().addBanRankInfo(player.getId(), rankType.name().toLowerCase(), reason, zeroEarningTime);
				RankService.getInstance().banJoinRank(player.getId(), rankType, zeroEarningTime, false, player);
			}
			return true;
		}
	}
	
}
