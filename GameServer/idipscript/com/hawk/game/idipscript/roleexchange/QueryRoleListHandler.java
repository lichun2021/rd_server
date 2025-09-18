package com.hawk.game.idipscript.roleexchange;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 获取角色列表（心悦角色交易）请求 -- 10282184
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4517")
public class QueryRoleListHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String openId = request.getJSONObject("body").getString("OpenId");
		int platId = request.getJSONObject("body").getIntValue("PlatId");
		JSONArray array = new JSONArray();
		Map<String, AccountRoleInfo> roleMap = GlobalData.getInstance().getPlayerAccountInfos(openId);
		if (roleMap != null) {
			for (AccountRoleInfo roleInfo : roleMap.values()) {
				String mainServerId = GlobalData.getInstance().getMainServerId(roleInfo.getServerId());
				if (!GsConfig.getInstance().getServerId().equals(mainServerId)) {
					continue;
				}
				Player player = GlobalData.getInstance().makesurePlayer(roleInfo.getPlayerId());
				if (player == null || player.getPlatId() != platId) {
					continue;
				}
				JSONObject json = RoleExchangeService.getInstance().getXinyuePlayerAbstractInfo(player);
				array.add(json);
			}
		}
		
		JSONObject json = new JSONObject();
		json.put("roleList", array);
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
