package com.hawk.game.idipscript.mail;

import java.util.Map;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发送文本邮件请求（指定账号） -- 10282825
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
		String roleid = request.getJSONObject("body").getString("RoleId");
		if (player == null) {
			if (HawkOSOperator.isEmptyString(roleid)) {
				return result;
			}
			String openid = request.getJSONObject("body").getString("OpenId");
			Map<String, String> map = RedisProxy.getInstance().getAccountRole(openid);
			AccountRoleInfo roleInfo = null;
			for (String value : map.values()) {
				AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
				if (roleInfoObj.getPlayerId().equals(roleid)) {
					roleInfo = roleInfoObj;
					break;
				}
			}
			
			if (roleInfo == null) {
				return result;
			}
		} else if (!player.getId().equals(roleid)) {
			roleid = player.getId();
		}
		
		String title = request.getJSONObject("body").getString("Title");
		title = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("Content");
		content = IdipUtil.decode(content);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(roleid)
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
