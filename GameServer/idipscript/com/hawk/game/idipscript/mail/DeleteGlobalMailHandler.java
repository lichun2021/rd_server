package com.hawk.game.idipscript.mail;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 撤回/删除全服发送邮件  -- 10282106
 *
 * localhost:8080/script/idip/4355?MailId=
 *
 * @param MailId   邮件uuid
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4355")
public class DeleteGlobalMailHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		String mailId = request.getJSONObject("body").getString("MailId");
		
		GlobalData.getInstance().deleteGlobalMail(mailId);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
