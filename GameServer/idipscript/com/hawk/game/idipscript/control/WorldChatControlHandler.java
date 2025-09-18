package com.hawk.game.idipscript.control;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 世界聊天控制 -- 10282066
 *
 * localhost:8080/script/idip/4235?Switch=
 *
 * @param Switch       开关
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4235")
public class WorldChatControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.WORLD_CHAT);
		ChatService.getInstance().chatSystemControlNotice(NoticeType.WORLD_CHAR_CONTROL, switchVal);
		
		// 添加敏感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
