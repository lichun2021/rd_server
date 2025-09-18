package com.hawk.game.idipscript.mail;

import java.net.URLEncoder;
import java.util.List;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.GlobalMail;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询全服发送邮件列表  -- 10282107
 *
 * localhost:8080/script/idip/4357
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4357")
public class QueryGlobalMailHandler extends IdipScriptHandler {
	
	static final int MAX_MAILLIST1_NUM = 10;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int pageNum = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNum > 1 ? (pageNum - 1) * MAX_MAILLIST1_NUM : 0;
		int indexEnd = indexStart + MAX_MAILLIST1_NUM;
		indexStart += 1;
		
		List<GlobalMail> globalMails = GlobalData.getInstance().getAllGlobalMail();
		JSONArray array = new JSONArray();
		int count = 0, totalCount = (int) globalMails.stream().filter(e -> e.getMailId() == MailId.REWARD_MAIL_VALUE).count();
		for (GlobalMail mail : globalMails) {
			if (mail.getMailId() != MailId.REWARD_MAIL_VALUE) {
				continue;
			}
			
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				break;
			}
			
			JSONObject json = new JSONObject();
			json.put("MailId", mail.getUuid());
			String title = mail.getSubTitle();
			if (HawkOSOperator.isEmptyString(title)) {
				title = "";
			} else {
				try {
					title = URLEncoder.encode(title, "UTF-8");
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			json.put("MailTitle", title);
			
			String[] rewards = mail.getReward().split(",");
			json.put("Status", 0); // 邮件状态（1表示已领取、0表示未领取）
			json.put("MailNum", rewards.length);
			array.add(json);
		}
		
		result.getBody().put("TotalPageNum", (int)Math.ceil(totalCount * 1.0d /MAX_MAILLIST1_NUM));
		result.getBody().put("MailList1_count", array.size());
		result.getBody().put("MailList1", array);
		
		return result;
	}
	
}
