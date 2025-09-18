package com.hawk.game.idipscript.attrmodify;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;

/**
 * 清零游戏分数(AQ) -- 10282804
 *
 * localhost:8080/script/idip/4137?Partition=&OpenId=&Type =&IsZero=
 * @param Partition 小区id
 * @param OpenId  用户openId
 * @param Type  分数类型(联盟个人积分, 黄金, 石油, 合金, 0 全选)
 * @param IsZero  是否清零(0 不清零, 1 清零)
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4137")
public class ResetScoreHandler extends IdipScriptHandler {
	// 全选
	private static final int ALL = 0; 
	// 燃料
	private static final int FUEL = 1111;
	
	private static final int[] SCORE_TYPE = {PlayerAttr.GOLDORE_UNSAFE_VALUE, PlayerAttr.OIL_UNSAFE_VALUE, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, PlayerAttr.STEEL_UNSAFE_VALUE,
			PlayerAttr.GOLDORE_VALUE, PlayerAttr.OIL_VALUE, PlayerAttr.TOMBARTHITE_VALUE, PlayerAttr.STEEL_VALUE, PlayerAttr.GUILD_CONTRIBUTION_VALUE};
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int type = request.getJSONObject("body").getIntValue("Type");
		switch (type) {
			case ALL:
			case FUEL:
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			case PlayerAttr.OIL_UNSAFE_VALUE:
			case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			case PlayerAttr.STEEL_UNSAFE_VALUE:
			case PlayerAttr.GOLDORE_VALUE:
			case PlayerAttr.OIL_VALUE:
			case PlayerAttr.TOMBARTHITE_VALUE:
			case PlayerAttr.STEEL_VALUE:
			case PlayerAttr.GUILD_CONTRIBUTION_VALUE:
				break;
			default: {
				result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getHead().put("RetErrMsg", "Type param error");
				return result;
			}
		}

		boolean isZero = request.getJSONObject("body").getIntValue("IsZero") == 1;
		if (isZero) {
			if (player.isActiveOnline()) {
				player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ResetScoreMsgInvoker(player, request));
			} else {
				resetScore(player, request);
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class ResetScoreMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public ResetScoreMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			resetScore(player, request);
			player.kickout(Status.IdipMsgCode.IDIP_RESET_SCORE_OFFLINE_VALUE, true, null);
			return true;
		}
	}
	
	/**
	 * 重置分数
	 * 
	 * @param player
	 * @param type
	 * @param request
	 * 
	 */
	private static void resetScore(Player player, JSONObject request) {
		int type = request.getJSONObject("body").getIntValue("Type");
		ConsumeItems consume = ConsumeItems.valueOf();
		
		if (type == ALL) {
			for (int scoreType : SCORE_TYPE) {
				consume.addConsumeInfo(PlayerAttr.valueOf(scoreType), player.getResByType(scoreType));
			}
		} else {
			consume.addConsumeInfo(PlayerAttr.valueOf(type), player.getResByType(type));
		}

		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
		
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, 0);
	}
}
