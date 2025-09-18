package com.hawk.game.idipscript.attrmodify;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;

/**
 * 设置游戏分数(AQ) -- 10282805
 *
 * localhost:8080/script/idip/4139?Partition=&OpenId=&Type =&IsZero=
 * @param Partition 小区id
 * @param OpenId  用户openId
 * @param Type  分数类型(联盟个人积分, 黄金, 石油, 合金, 0 全选)
 * @param SetValue 设定值
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4139")
public class SetScoreHandler extends IdipScriptHandler {
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

		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new SetScoreMsgInvoker(player, request));
		} else {
			setScore(player, request);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class SetScoreMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public SetScoreMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			setScore(player, request);
			player.kickout(Status.IdipMsgCode.IDIP_SET_SCORE_OFFLINE_VALUE, true, null);
			return true;
		}
	}
	
	/**
	 * 设置分数
	 * 
	 * @param player
	 * @param type
	 * @param setValue
	 * @param request
	 */
	public static void setScore(Player player, JSONObject request) {
		JSONObject body = request.getJSONObject("body");
		int type = body.getIntValue("Type");
		int setValue = body.getIntValue("SetValue");
		// 资源添加上限为10w
		setValue = Math.min(setValue, GameConstCfg.getInstance().getResCntAddLimit());
		
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems awardItems = AwardItems.valueOf();
		if (type == ALL) {
			for (int scoreType : SCORE_TYPE) {
				consume.addConsumeInfo(PlayerAttr.valueOf(scoreType), player.getResByType(scoreType));
				awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, scoreType, setValue);
			}
		} else {
			consume.addConsumeInfo(PlayerAttr.valueOf(type), player.getResByType(type));
			awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, type, setValue);
		}
		
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
		
		awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, setValue);
	}
}
