package com.hawk.game.idipscript.roleexchange;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询买家是否可以购买该角色 -- 10282179
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4507")
public class QueryRoleBoughtHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String sellerOpenId = request.getJSONObject("body").getString("SellerOpenId");
		Player player = IdipUtil.playerCheck(sellerOpenId, request, result, false);
		if (player == null) {
			HawkLog.errPrintln("idip/4507 QueryRoleBought error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		String buyerOpenid = request.getJSONObject("body").getString("BuyerOpenid");
		if (HawkOSOperator.isEmptyString(buyerOpenid)) {
			result.getBody().put("Result", -1);
			result.getBody().put("RetMsg", "params error");
			result.getBody().put("Data", "");
			return result;
		}
		
		JSONObject json = new JSONObject();
		List<Integer> codeList = new ArrayList<>();
		RoleExchangeService.getInstance().sellerRoleLaunchCheck(player, true, codeList);
		RoleExchangeService.getInstance().buyerCheck(buyerOpenid, true, codeList);
		if (codeList.isEmpty()) {
			json.put("isTradable", true);
		} else {
			json.put("isTradable", false); 
			json.put("cwList", codeList.toArray(new Integer[codeList.size()]));
		}
		
		String resultData = json.toJSONString();
		if (GsConfig.getInstance().isXinyueRoleEncode()) {
			try {
				byte[] textByte = resultData.getBytes("UTF-8");
				resultData = Base64.getEncoder().encodeToString(textByte);
				resultData = URLEncoder.encode(resultData, "UTF-8");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		result.getBody().put("Data", resultData);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
