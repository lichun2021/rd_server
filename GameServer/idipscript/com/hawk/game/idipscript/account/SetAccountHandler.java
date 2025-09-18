package com.hawk.game.idipscript.account;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 初始化帐号接口(AQ) -- 10282807
 *
 * localhost:8080/script/idip/4143
 *
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4143")
public class SetAccountHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		// 删除守护关系
		RelationService.getInstance().dealMsg(GameConst.MsgId.GUARD_DELETE, new DeleteRoleHandler.GuardDeleteSubInvoker(player));
		
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
