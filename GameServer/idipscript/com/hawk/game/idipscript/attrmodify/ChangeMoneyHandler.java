package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改货币数量 -- 10282015
 *
 * localhost:8080/script/idip/4127?OpenId=&RoleId=&Type=&Value=
 *
 * @param OpenId  用户openId
 * @param RoleId  playerId
 * @param Type  货币类型：1001金币，1000钻石
 * @param Value 修改值：-减+加
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4127")
public class ChangeMoneyHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int type = request.getJSONObject("body").getIntValue("Type");
		if (type != PlayerAttr.GOLD_VALUE && type != PlayerAttr.DIAMOND_VALUE) {
			result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getHead().put("RetErrMsg", "Type param error");
			return result;
		}
		
		int moneyCount = request.getJSONObject("body").getIntValue("Value");
		int moneyBefore = type == PlayerAttr.GOLD_VALUE ? player.getGold() : player.getDiamonds();
		int moneyAfter = moneyBefore + moneyCount;
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeMoneyMsgInvoker(player, type, moneyCount));
		} else {
			changeMoney(player, type, moneyCount);
		}
		
		// 记录敏感日志
		LogUtil.logIdipSensitivity(player, request, 0, moneyCount);

		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Value1", moneyBefore);
		result.getBody().put("Value2", moneyAfter < 0 ? moneyBefore : moneyAfter);

		return result;
	}
	
	/**
	 * 修改货币数量
	 * @param player
	 * @param moneyType
	 * @param moneyCount
	 */
	private static void changeMoney(Player player, int moneyType, int moneyCount) {
		if (moneyType == PlayerAttr.DIAMOND_VALUE && !player.isActiveOnline()) {
			player.addMoneyReissueItem(moneyCount, Action.IDIP_CHANGE_PLAYER_ATTR, null);
			return;
		}
		
		if (moneyCount < 0) {
			int localMoney = Math.abs(moneyCount);
			int moneyBefore = moneyType == PlayerAttr.GOLD_VALUE ? player.getGold() : player.getDiamonds();
			ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.valueOf(moneyType), moneyBefore > localMoney ? localMoney : moneyBefore);
			if (consume.checkConsume(player)) {
				consume.consumeAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
			}
			
			if (moneyType == PlayerAttr.DIAMOND_VALUE && moneyBefore < localMoney) {
				String key = "todoSubDiamonds:" + player.getId();
				RedisProxy.getInstance().getRedisSession().increaseBy(key, localMoney - moneyBefore, 0);
				HawkLog.logPrintln("ChangeMoneyHandler-4127 dimonds subcrease record success, playerId: {}, diamonds: {}", player.getId(), localMoney - moneyBefore);
			}
			
			return;
		}
		
		if (moneyType == PlayerAttr.DIAMOND_VALUE) {
			// 赠送原因  customer_experience
			player.increaseDiamond(moneyCount, Action.IDIP_CHANGE_PLAYER_ATTR, null, DiamondPresentReason.EXPERIENCE);
			return;
		}
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(ItemType.PLAYER_ATTR_VALUE, moneyType, moneyCount);
		awardItems.rewardTakeAffectAndPush(player, Action.IDIP_CHANGE_PLAYER_ATTR);
	}
	
	public static class ChangeMoneyMsgInvoker extends HawkMsgInvoker {
		
		private Player player;
		private int moneyType;
		private int moneyCount;
		
		public ChangeMoneyMsgInvoker(Player player, int moneyType, int moneyCount) {
			this.player = player;
			this.moneyType = moneyType;
			this.moneyCount = moneyCount;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changeMoney(player, moneyType, moneyCount);
			return true;
		}
	}
}
