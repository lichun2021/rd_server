package com.hawk.game.idipscript.online.pack0604;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.uuid.HawkUUIDGenerator;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.tsssdk.util.UicNameDataInfo;

/**
 * 修改联盟堡垒名称
 *
 * localhost:8080/script/idip/4369
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4369")
public class ChangeGuildFortressNameHandler extends IdipScriptHandler {
	
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
				HawkLog.logPrintln("ChangeGuildFortressName failed, catch exception, allianceId: {}", allianceId);
				HawkException.catchException(e);
			} finally {
				if (guildInfo == null) {
					result.getBody().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
					result.getBody().put("RetMsg", "guild not exist");
					return result;
				}
			}
		}
		
		final String guildId = guildInfo.getId();
		// 新的联盟堡垒名称
		String encodedName = request.getJSONObject("body").getString("FortressName");
		String name = IdipUtil.decode(encodedName);
		int op = GuildUtil.checkGuildManorName(name);
		if (op != Status.SysError.SUCCESS_OK_VALUE) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "FortressName check failed");
			return result;
		}
		
		// 联盟堡垒序号
		int index = request.getJSONObject("body").getIntValue("AlliancePara");
        AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameUtil.wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("ChangeGuildFortressName failed, guildId: {}, allianceId: {}, tarName: {}, result: {}", guildId, allianceId, name, dataInfo.msg_result_flag);
					return 0;
				}
				
				GuildManorObj manor = GuildManorService.getInstance().getManorByIdx(guildId, index);
				if(manor != null){
					manor.changeManorName(name);
					RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME, HawkApp.getInstance().getCurrentTime());
				} else {
					checkResult.set(1);
					HawkLog.logPrintln("ChangeGuildFortressName failed, guildMonor not exist, guildId: {}, allianceId: {}, index: {}, tarName: {}", guildId, allianceId, index, name);
					return 0;
				}
				
				checkResult.set(0);
				//推送变化消息
				GuildManorList.Builder builder = GuildManorList.newBuilder();
				//领地哨塔列表
				GuildManorService.getInstance().makeManorBastion(builder, guildId);
				//广播消息
				GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				
				HawkLog.logPrintln("ChangeGuildFortressName succ, guildId: {}, allianceId: {}, tarName: {}", guildId, allianceId, name);
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "FortressName change failed");
		
		return result;
	}
}


