package com.hawk.game.gmscript;

import java.util.Map;
import java.util.Optional;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.config.PushSurveyCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.SurveyNotifyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 问卷调查回调
 * 
 * localhost:8080/script/survey
 *
 * @author hawk
 * 
 */
public class SurveyNotifyHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		// 问卷Id
		int surveyId = 0;
		if (params.containsKey("qnid")) {
			surveyId = Integer.parseInt(params.get("qnid"));
		} else {
			String sid = params.get("sid");
			int areaId = Integer.parseInt(GsConfig.getInstance().getAreaId());
			Optional<PushSurveyCfg> optional = HawkConfigManager.getInstance().getConfigIterator(PushSurveyCfg.class).stream().filter(e -> e.getSid().equals(sid) && e.getChannel() == areaId).findFirst();
			surveyId = optional.isPresent() ? optional.get().getId() : 0; //PushSurveyCfg.getSurveyId(sid);
			if (surveyId == 0) {
				throw new RuntimeException("sid config error");
			}
		}
		
		// 透传参数
		String rid = params.get("rid");
		
		// 提取玩家Id
		// rid=areaId@serverId@playerId
		String playerId = rid.split("@")[2];
		Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("survey notify failed, surveyId: {}, playerId: {}", surveyId, playerId);
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, null);
		}
		
		HawkApp.getInstance().postMsg(player.getXid(), SurveyNotifyMsg.valueOf(surveyId));
		return HawkScript.successResponse(null);
	}
}
