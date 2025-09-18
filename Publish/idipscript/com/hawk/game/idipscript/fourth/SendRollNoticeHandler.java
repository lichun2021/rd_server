package com.hawk.game.idipscript.fourth;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.protocol.Const;
import com.hawk.game.service.chat.ChatService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送滚动公告
 *
 * localhost:8080/script/idip/4117?NoticeTitle=&NoticeContent=&Speed=&StartTime=&EndTime=
 *
 * @param NoticeTitle    公告标题
 * @param NoticeContent  公告内容
 * @param Speed    公告滚动速度（发送间隔，分钟为单位）
 * @param StartTime  公告开始生效时间
 * @param EndTime    公告结束生效时间
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4117")
public class SendRollNoticeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		@SuppressWarnings("unused")
		String title = request.getJSONObject("body").getString("NoticeTitle");
		String content = request.getJSONObject("body").getString("NoticeContent");
		@SuppressWarnings("unused")
		int speed = request.getJSONObject("body").getIntValue("Speed");
		@SuppressWarnings("unused")
		long startTime = request.getJSONObject("body").getLongValue("StartTime");
		@SuppressWarnings("unused")
		long endTime = request.getJSONObject("body").getLongValue("EndTime");
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SYS_BROADCAST, null, null, IdipUtil.decode(content));
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
