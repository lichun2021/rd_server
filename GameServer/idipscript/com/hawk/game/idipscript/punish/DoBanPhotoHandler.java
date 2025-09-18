package com.hawk.game.idipscript.punish;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 禁止玩家修改头像 -- 10282141
 *
 * localhost:8081/idip/4425
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4425")
public class DoBanPhotoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String reason = request.getJSONObject("body").getString("OperateReason");
		reason = IdipUtil.decode(reason);
		int minutes = request.getJSONObject("body").getIntValue("BanTime");
		int seconds = minutes * 60;
		
		long now = HawkTime.getMillisecond();
		long endTime = now + 1000L * seconds;
		IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason + "（解封时间：" + HawkTime.formatTime(endTime) + "）", now, endTime, seconds);
		RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, IDIPBanType.BAN_CHANGE_IMAGE);
		
		int timeNow = HawkTime.getSeconds();
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("OperTime", timeNow);     // 封停时间
		result.getBody().put("ExpireTime", timeNow + seconds);   // 解封时间
		result.getBody().put("BanTerm", minutes);      // 封停时长
		return result;
	}
	
}


