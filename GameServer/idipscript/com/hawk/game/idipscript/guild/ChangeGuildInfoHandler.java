package com.hawk.game.idipscript.guild;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.tsssdk.util.UicNameDataInfo;

/**
 * 修改联盟信息 -- 10282138
 *
 * localhost:8080/script/idip/4419
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4419")
public class ChangeGuildInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String allianceId = request.getJSONObject("body").getString("AllianceId");
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(allianceId);
		
		if (guildInfo == null) {
			try {
				String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(allianceId));
				guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			} catch (Exception e) {
				HawkLog.logPrintln("script change guild name failed, catch exception, allianceId: {}", allianceId);
			} finally {
				if (guildInfo == null) {
					result.getBody().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
					result.getBody().put("RetMsg", "guild not eixt");
					return result;
				}
			}
		}
		
		final String guildId = guildInfo.getId();
		// 修改文本
		String encodedtxt = request.getJSONObject("body").getString("Text");
		String txt = IdipUtil.decode(encodedtxt);
		int type = request.getJSONObject("body").getIntValue("Type"); // 修改类型：0-联盟名字，1-联盟简称
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(txt, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild info failed, guildId: {}, allianceId: {}, type: {}, txt: {}, result: {}", guildId, allianceId, type, txt, dataInfo.msg_result_flag);
					return 0;
				}
				
				int retCode = 0;
				if (type == 0) {  // 修改联盟名称
					retCode = GuildService.getInstance().onChangeGuildName(txt, guildId);
					RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
				} else if (type == 1) {  // 修改联盟简称
					retCode = GuildService.getInstance().onChangeGuildTag(txt, guildId);
				}
				
				checkResult.set(retCode);
				HawkLog.logPrintln("script change guild info success, guildId: {}, allianceId: {}, type: {}, txt: {}, result: {}", guildId, allianceId, type, txt, retCode);
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "guild info change failed");
		
		return result;
	}
}


