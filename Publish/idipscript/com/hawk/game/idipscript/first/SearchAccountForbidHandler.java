package com.hawk.game.idipscript.first;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询账号封停状态
 *
 * localhost:8080/script/idip/4101?OpenId=
 *
 * @param OpenId  用户openId
 * @param playerId
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4101")
public class SearchAccountForbidHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(player.getPuid(), player.getServerId());
		long currTime = HawkTime.getMillisecond();
		result.getBody().put("Result", 0);
		result.getBody().put("Status", currTime < accountInfo.getForbidenTime() ? 1 : 0);
		if(currTime < accountInfo.getForbidenTime()) {
			IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_ACCOUNT);
			String banReason = "";
			try {
				banReason = URLEncoder.encode(banInfo.getBanMsg(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}
			
			result.getBody().put("Time", banInfo.getBanSecond());
			result.getBody().put("BanDate", HawkTime.formatTime(accountInfo.getForbidenTime()));
			result.getBody().put("BanReason", banReason);
		}
		
		return result;
	}
}
