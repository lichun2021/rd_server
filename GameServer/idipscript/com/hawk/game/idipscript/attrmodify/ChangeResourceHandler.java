package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;

/**
 * 修改资源数量 -- 10282013
 *
 * localhost:8080/script/idip/4123?OpenId=&RoleId=&Type=&Value=
 *
 * @param OpenId  用户openId
 * @param RoleId  playerId
 * @param Type 资源类型
 * @param Value 修改值：-减+加
 * @author lating
 */
@HawkScript.Declare(id = "idip/4123")
public class ChangeResourceHandler extends IdipScriptHandler {
	
	// 燃料
	private static final int FUEL = 1111;
		
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int resType = request.getJSONObject("body").getIntValue("Type");
		switch (resType) {
			case FUEL:
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			case PlayerAttr.OIL_UNSAFE_VALUE:
			case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			case PlayerAttr.STEEL_UNSAFE_VALUE:
			case PlayerAttr.GOLDORE_VALUE:
			case PlayerAttr.OIL_VALUE:
			case PlayerAttr.TOMBARTHITE_VALUE:
			case PlayerAttr.STEEL_VALUE:
				break;
			default: {
				result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getHead().put("RetErrMsg", "Type param error");
				return result;
			}
		}
		
		int resNum = request.getJSONObject("body").getIntValue("Value");
		long before = player.getResByType(resType);
		long after = before + resNum;
		if(player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ResChangeMsgInvoker(player, resType, resNum));
		} else {
			changeResource(player, resType, resNum, before);
		}
		
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, resNum);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Value1", before);
		result.getBody().put("Value2", after < 0 ? 0 : after);
		
		return result;
	}
	
	/**
	 * 修改资源数量
	 * 
	 * @param player
	 * @param resType
	 * @param resNum
	 * @param before
	 */
	private static void changeResource(Player player, int resType, int resNum, long before) {
		if(resNum > 0) {
			AwardItems award = AwardItems.valueOf();
			award.addItem(ItemType.PLAYER_ATTR_VALUE, resType, resNum);
			award.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			return;
		}
		
		int resNumAbs = Math.abs(resNum);
		resNumAbs = (int) (resNumAbs > before ? before : resNumAbs);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, resType, resNumAbs), false);
		if (consume.checkConsume(player)) {
			consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
		}
	}
	
	public static class ResChangeMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private int resType;
		private int resNum;
		
		public ResChangeMsgInvoker(Player player, int resType, int resNum) {
			this.player = player;
			this.resType = resType;
			this.resNum = resNum;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			long before = player.getResByType(resType);
			changeResource(player, resType, resNum, before);
			player.getPush().syncPlayerInfo();
			return true;
		}
	}
}
