package com.hawk.game.idipscript.punish;


import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 解除处罚接口(AQ) -- 10282812
 *
 * localhost:8080/script/idip/4153
 *
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4153")
public class RelievePunishHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new PunishRelieveMsgInvoker(player, request));
		} else {
			relievePunish(player, request);
		}

		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 解除处罚
	 * 
	 * @param player
	 * @param relieveZeroProfit
	 * @param RelieveBanJoinRank
	 * @param relieveBan
	 * @param relieveMaskchat
	 */
	private static void relievePunish(Player player, JSONObject request) {
		int relieveZeroProfit = request.getJSONObject("body").getInteger("RelieveZeroProfit");
		int RelieveBanJoinRank = request.getJSONObject("body").getInteger("RelieveBanJoinRank");
		int relieveBan = request.getJSONObject("body").getInteger("RelieveBan");
		int relieveMaskchat = request.getJSONObject("body").getInteger("RelieveMaskchat");
		// 解除零收益状态
		if (relieveZeroProfit == 1 && player.isZeroEarningState()) {
			player.getEntity().setZeroEarningTime(0);
			player.sendIDIPZeroEarningMsg();
		}

		// 解除禁止参与排行榜
		if (RelieveBanJoinRank == 1 && RankService.getInstance().isBan(player.getId())) {
			RankService.getInstance().dealMsg(MsgId.PERSONAL_RANK_UNBAN, new PersonalRankAllUnbanMsgInvoker(player.getId()));
		}

		// 解除封号
		if (relieveBan == 1) {
			player.getEntity().setForbidenTime(0);
			// 更新缓存状态
			GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), 0, player.getName());
			RedisProxy.getInstance().removeIDIPBanInfo(player.getId(), IDIPBanType.BAN_ACCOUNT);
		}

		// 解除禁言
		if (relieveMaskchat == 1 && player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			player.getEntity().setSilentTime(0);
			RedisProxy.getInstance().removeIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			ChatService.getInstance().sendBanMsgNotice(player, 0);
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, 0);
	}
	
	public static class PunishRelieveMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public PunishRelieveMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			relievePunish(player, request);
			return true;
		}
	}
	
	public static class PersonalRankAllUnbanMsgInvoker extends HawkMsgInvoker {
		private String playerId;
		
		public PersonalRankAllUnbanMsgInvoker(String playerId) {
			this.playerId = playerId;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
				RankService.getInstance().removeFromBan(rankType, playerId);
			}
			
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null) {
				RankService.getInstance().sendBanRankNotice(player, 0, "");
			}
			
			return true;
		}
	}
}
