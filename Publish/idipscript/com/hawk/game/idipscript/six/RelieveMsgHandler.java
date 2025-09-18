package com.hawk.game.idipscript.six;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 解除禁言
 *
 * localhost:8080/script/idip/4281
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param OpenId     用户openId
 * @param Reason     禁言原因
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4281")
public class RelieveMsgHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.getEntity().getSilentTime() > HawkTime.getMillisecond()) {
			player.getEntity().setSilentTime(0);
			RedisProxy.getInstance().removeIDIPBanInfo(player.getId(), IDIPBanType.BAN_SEND_MSG);
			ChatService.getInstance().sendBanMsgNotice(player, 0);
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}

}
