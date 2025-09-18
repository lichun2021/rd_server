package com.hawk.game.idipscript.online;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送文本邮件请求（指定账号）
 *
 * localhost:8081/idip/4321
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4321")
public class SendTextMailHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String title = request.getJSONObject("body").getString("Title");
		title = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("Content");
		content = IdipUtil.decode(content);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
                .setMailId(MailId.SYSTEM_NOTICE)
                .setAwardStatus(MailRewardStatus.NOT_GET)
                .addSubTitles(title)
                .addContents(content)
                .build());
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
