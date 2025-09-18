package com.hawk.game.idipscript.punish;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 屏蔽玩家头像拉取  -- 10282824
 *
 * localhost:8081/idip/4319
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4319")
public class BanPortraitHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		AccountInfo account = IdipUtil.accounCheck(request, result, true);
		if (account == null) {
			return result;
		}
		
		String openId = request.getJSONObject("body").getString("OpenId");
		int second = request.getJSONObject("body").getIntValue("Time");
		int type = request.getJSONObject("body").getIntValue("Type");
		if (type == 1) {
			long endTime = HawkTime.getMillisecond() + second * 1000L;
			GlobalData.getInstance().addBanPortraitAccount(openId, endTime);
		} else {
			GlobalData.getInstance().removeBanPortraitAccount(openId);
		}
		
		Player player = GlobalData.getInstance().getActivePlayer(account.getPlayerId());
		if (player != null) {
			player.getPush().syncPlayerInfo();
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


