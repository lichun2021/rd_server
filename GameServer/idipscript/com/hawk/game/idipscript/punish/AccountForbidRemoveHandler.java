package com.hawk.game.idipscript.punish;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 解封账号 -- 10282006
 *
 * localhost:8080/script/idip/4107?OpenId=&UanReason=
 *
 * @param OpenId  用户openId
 * @param UanReason 解封原因
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4107", desc = "解封账号")
public class AccountForbidRemoveHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		AccountInfo accountInfo = IdipUtil.accounCheck(request, result, true);
		if (accountInfo == null) {
			return result;
		}

		accountForbidRemove(accountInfo);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	private static void accountForbidRemove(AccountInfo accountInfo) {
		// 更新缓存状态
		GlobalData.getInstance().updateAccountInfo(accountInfo.getPuid(), accountInfo.getServerId(), accountInfo.getPlayerId(), 0, accountInfo.getPlayerName());
		RedisProxy.getInstance().removeIDIPBanInfo(accountInfo.getPlayerId(), IDIPBanType.BAN_ACCOUNT);
		Player player = GlobalData.getInstance().scriptMakesurePlayer(accountInfo.getPlayerId());
		if (player != null) {
			player.getEntity().setForbidenTime(0);
		}
	}
}
