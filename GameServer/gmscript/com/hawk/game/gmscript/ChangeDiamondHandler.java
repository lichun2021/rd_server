package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.Source;
import com.hawk.sdk.SDKManager;

/**
 * 修改钻石数量
 *
 * localhost:8080/script/changeDiamond?playerId=1aat-3f80z2-1&diamond=-704100
 *
 * @param playerId
 * @param diamond 修改值：-减+加
 * 
 * @author lating
 */
public class ChangeDiamondHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			// 未开支付托管才可执行
			int diamond = Integer.valueOf(params.get("diamond"));
			if (!SDKManager.getInstance().isPayOpen()) {
				if (diamond < 0) {
					int localMoney = player.getDiamonds();
					ConsumeItems consume = ConsumeItems.valueOf(PlayerAttr.DIAMOND, Math.abs(diamond) > localMoney ? localMoney : Math.abs(diamond));
					if (consume.checkConsume(player)) {
						consume.consumeAndPush(player, Action.GM_EXPLOIT);
					}
				} else {
					player.getData().getPlayerBaseEntity().setDiamonds(player.getDiamonds() + diamond);
					
					player.getPush().syncPlayerDiamonds();
					
					LogUtil.logMoneyFlow(player, Action.GM_AWARD, LogInfoType.money_add, diamond, IMoneyType.MT_DIAMOND);
					
					BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.GM_AWARD,
							Params.valueOf("playerAttr", PlayerAttr.DIAMOND_VALUE),
							Params.valueOf("add", diamond),
							Params.valueOf("after", player.getDiamonds()));
				}
				
				return HawkScript.successResponse("");
			} else {
				if (diamond <= 0) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "param error");
				}
				int diamonds = player.getDiamonds();
				int playerSaveAmt = player.getPlayerBaseEntity().getSaveAmt();
				
				player.getPlayerBaseEntity().setDiamonds(diamonds + diamond);
				player.getPlayerBaseEntity().setSaveAmt(playerSaveAmt + diamond);
				
				player.rechargeSuccess(playerSaveAmt, diamond, diamonds);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
