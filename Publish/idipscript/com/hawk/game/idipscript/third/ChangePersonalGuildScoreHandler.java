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
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 设置联盟个人积分（联盟贡献属于个人，联盟积分是属于联盟的，不属于个人）
 *
 * localhost:8080/script/idip/4227
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4227")
public class ChangePersonalGuildScoreHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangePersonalGuildScoreMsgInvoker(player, request));
		} else {
			try {
				changePersonalGuildScore(request, player);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "person alliance integral change failed");
				return result;
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	public static class ChangePersonalGuildScoreMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public ChangePersonalGuildScoreMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changePersonalGuildScore(request, player);
			return true;
		}
	}
	
	/**
	 * 修改个人联盟积分
	 * @param request
	 * @param player
	 */
	private static void changePersonalGuildScore(JSONObject request, Player player) {
		int value = request.getJSONObject("body").getIntValue("Value");
		if (value > 0) {
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GUILD_CONTRIBUTION_VALUE, value);
			awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR, true);
		} else {
			ConsumeItems consume = ConsumeItems.valueOf();
			int playerOldScore = (int) player.getGuildContribution();
			int consumeScore = Math.abs(value);
			consume.addConsumeInfo(PlayerAttr.GUILD_CONTRIBUTION, consumeScore > playerOldScore ? playerOldScore : consumeScore);
			if (consume.checkConsume(player)) {
				consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			}
		}
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_CHANGE_GUILD_SCORE_VALUE, true, null);
		}
		
		//IDIP协议文档中此协议并没有敏感日志相关字段
		//LogUtil.logIdipSensitivity(player, request, 0, value);
		HawkLog.logPrintln("idip change player guild contribution, playerId: {}, value: {}", player.getId(), value);
	}
}
