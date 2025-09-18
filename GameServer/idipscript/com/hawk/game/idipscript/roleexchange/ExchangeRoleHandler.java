package com.hawk.game.idipscript.roleexchange;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeState;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 转移角色(心悦角色交易)请求 -- 10282176
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4501")
public class ExchangeRoleHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String sellerOpenId = request.getJSONObject("body").getString("SellerOpenId");
		Player player = IdipUtil.playerCheck(sellerOpenId, request, result, false);
		if (player == null) {
			HawkLog.errPrintln("idip/4501 ExchangeRole error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		String targetOpenid = request.getJSONObject("body").getString("BuyerOpenid");
		try {
			int resultCode = RoleExchangeService.getInstance().roleExchange(player, targetOpenid);
			if (resultCode == 0) {
				RoleExchangeService.getInstance().roleExchangeStateSwitch(player, XinyueRoleExchangeState.EXCHANGE_INSPECTION);
				result.getBody().put("Result", 0);
				result.getBody().put("RetMsg", "");
				LogUtil.logIdipSensitivity(null, request, 0, 0);
			} else {
				result.getBody().put("Result", resultCode);
				result.getBody().put("RetMsg", "exchange role failed");
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "server exception");
		}
		
		return result;
	}
	
}
