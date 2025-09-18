package com.hawk.game.idipscript.mail;

import java.net.URLEncoder;
import java.util.List;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.service.MailService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询邮件信息列表 -- 10282026
 *
 * localhost:8080/script/idip/4175
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4175")
public class QueryMailHandler extends IdipScriptHandler {
	
	static final int MAX_MAILLIST1_NUM = 10;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		int pageNum = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNum > 1 ? (pageNum - 1) * MAX_MAILLIST1_NUM : 0;
		int indexEnd = indexStart + MAX_MAILLIST1_NUM;
		indexStart += 1;
		
		List<MailLiteInfo.Builder> mails = MailService.getInstance().listAllMail(player.getId());
		JSONArray array = new JSONArray();
		int count = 0;
		for (MailLiteInfo.Builder mail : mails) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				break;
			}
			
			JSONObject json = new JSONObject();
			json.put("MailId", mail.getId());
			String title = mail.getTitle();
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
			json.put("Status", mail.getHasReward() ? 0 : 1); // 邮件状态（1表示已领取、0表示未领取）
			json.put("MailNum", rewards.length);
			array.add(json);
		}
		
		result.getBody().put("TotalPageNum", (int)Math.ceil(mails.size() * 1.0d /MAX_MAILLIST1_NUM));
		result.getBody().put("MailList1_count", array.size());
		result.getBody().put("MailList1", array);
		
		return result;
	}
}
