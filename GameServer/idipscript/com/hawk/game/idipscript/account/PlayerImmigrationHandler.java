package com.hawk.game.idipscript.account;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.service.SysOpService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 单个玩家向指定服务器迁移（迁服） -- 10282197
 *
 * localhost:8080/script/idip/4543
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4543")
public class PlayerImmigrationHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int targetServerId = request.getJSONObject("body").getIntValue("DstPartition");
		boolean operResult = SysOpService.getInstance().playerImmigrate(player.getId(), String.valueOf(targetServerId));
		if (!operResult) {
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "server handle failed");
			return result;
		}
		
		String title = request.getJSONObject("body").getString("MailTitle");
		String finalTitle = IdipUtil.decode(title);
		String content = request.getJSONObject("body").getString("MailContent");
		String finalContent = IdipUtil.decode(content);
		HawkTaskManager.getInstance().postTask(new HawkDelayTask(300000L, 300000L, 1) {
			@Override
			public Object run() {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.SYSTEM_NOTICE)
						.setAwardStatus(MailRewardStatus.GET)
						.addSubTitles(finalTitle)
						.addContents(finalContent)
						.build());
				return null;
			}
		});
		
		HawkLog.logPrintln("idip immigration playerId: {}, targetServerId: {}, local server: {}", player.getId(), targetServerId, GsConfig.getInstance().getServerId());
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
