package com.hawk.game.idipscript.mail;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 全服禁止服务端发放邮件  -- 10282195
 *
 * localhost:8080/script/idip/4539?MailId=
 *
 * @param MailId   邮件id
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4539")
public class ForbiddenMailHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		int mailId = request.getJSONObject("body").getIntValue("MailId");
		int type = request.getJSONObject("body").getIntValue("Type"); //类型：0恢复，1禁止
		if (type > 0) {
			GlobalData.getInstance().addForbiddenMailId(mailId);
		} else {
			GlobalData.getInstance().removeForbiddenMailId(mailId);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
