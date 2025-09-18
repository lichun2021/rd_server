package com.hawk.game.idipscript.second;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 通过玩家角色名称查询openid和角色id（SQ）
 *
 * localhost:8080/script/idip/4161
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4161")
public class QueryOpenIdByNameHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String playerName = request.getJSONObject("body").getString("RoleName");
		
		try {
			playerName = URLDecoder.decode(playerName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		
		String playerId = GameUtil.getPlayerIdByName(playerName);
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		if (accountInfo == null) {
			result.getHead().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getHead().put("RetErrMsg", "account not found");
			return result;
		}
		
		result.getBody().put("OpenId", accountInfo.getPuid().split("#")[0]);
		result.getBody().put("RoleId", playerId);
		return result;
	}
}
