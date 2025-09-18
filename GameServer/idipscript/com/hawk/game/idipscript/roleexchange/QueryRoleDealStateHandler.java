package com.hawk.game.idipscript.roleexchange;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询角色交易状态（心悦角色交易）请求 -- 10282180
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4509")
public class QueryRoleDealStateHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4509 QueryRoleDealState error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		String redisKey = RoleExchangeService.getInstance().getRoleExchangeStatusKey(player.getOpenId(), player.getId());
		String status = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Status", HawkOSOperator.isEmptyString(status) ? 0 : Integer.parseInt(status));
		return result;
	}
}
