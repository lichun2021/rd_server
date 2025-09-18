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
 * 修改联盟名称(AQ) -- 10282829
 *
 * localhost:8080/script/idip/4349
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4349")
public class ChangeGuildNameAQHandler extends IdipScriptHandler {
	
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
		// 新的联盟名称
		String encodedName = request.getJSONObject("body").getString("AllianceName");
		String name = IdipUtil.decode(encodedName);
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild name failed, guildId: {}, allianceId: {}, tarName: {}, result: {}", guildId, allianceId, name, dataInfo.msg_result_flag);
					return 0;
				}

				int retCode = GuildService.getInstance().onChangeGuildName(name, guildId);
				checkResult.set(retCode);
				RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
				HawkLog.logPrintln("script change guildName success, guildId: {}, allianceId: {}, guildName: {}, result: {}", guildId, allianceId, name, retCode);
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "allianceName change failed");
		
		return result;
	}
}


