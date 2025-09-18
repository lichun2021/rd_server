package com.hawk.game.idipscript.roleexchange;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
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
 * 查询角色是否可登记出售及其原因（心悦角色交易）请求 -- 10282178
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4505")
public class QueryRoleCanSellHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4505 QueryRoleCanSell error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		JSONObject json = new JSONObject();
		List<Integer> codeList = RoleExchangeService.getInstance().getRoleExchangeFailCodeList(player);
		codeList.remove(Integer.valueOf(XinyueRoleExchangeFailReason.ERROR_1023)); //心悦那边说这个接口不需要管这个错误码
		if (codeList.isEmpty()) {
			json.put("isTradable", true);
		} else {
			json.put("isTradable", false);  //bool：该角色是否可交易
			json.put("cwList", codeList.toArray(new Integer[codeList.size()])); //int数组： 当该角色不可交易时，不可交易原因码列表, 原因码需要与需求中⼀⼀对应
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
