package com.hawk.game.script;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ImmgrationActivityCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.PlayerImmgrationModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Immgration;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.util.GsConst;

/**
 * 127.0.0.1:8080/script/immigrate?playerId=&serverId=
 * @author Golden
 *
 */
public class ImmigratePlayerHandler extends HawkScript {

	@SuppressWarnings("deprecation")
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		// 玩家id
		String playerId = params.get("playerId");
		// 迁到哪个服(区分主服和从服)
		String serverId = params.get("serverId");
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		PlayerImmgrationModule module = player.getModule(GsConst.ModuleType.IMMGRATION);
		
		// 检测一下能不能迁
		if (!module.checkBeforeImmgration(serverId)) {
			return HawkScript.failedResponse(-1, "");
		}
		
		try {
			// 序列化玩家数据
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToRedis) {
				return HawkScript.failedResponse(-2, "");
			}
			// 序列化活动数据
			ConfigIterator<ImmgrationActivityCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityCfg.class);
			while (cfgIter.hasNext()) {
				ImmgrationActivityCfg cfg = cfgIter.next();
				if (module.flushActivityToRedis(player.getId(), cfg.getActivityId())) {
					HawkLog.logPrintln("player immgration, onImmgration, flush activity to redis, playerId:{}, activityId:{}", player.getId(), cfg.getActivityId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkScript.failedResponse(-3, "");
		}

		// 通知目标服
		Immgration.ImmgrationServerReq.Builder builder = Immgration.ImmgrationServerReq.newBuilder();
		builder.setPlayerId(playerId);
		builder.setTarServerId(serverId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_REQ_VALUE, builder);
		CrossProxy.getInstance().sendNotify(protocol, GlobalData.getInstance().getMainServerId(serverId), player.getId(), null);
		
		// 日志记录
		try {
			int termId = ImmgrationService.getInstance().getImmgrationActivityTermId();
			JSONObject immgrationLog = new JSONObject();
			immgrationLog.put("playerId", player.getId());
			immgrationLog.put("fromServer", player.getServerId());
			immgrationLog.put("tarServer", serverId);
			immgrationLog.put("time", HawkTime.formatNowTime());
			immgrationLog.put("puid", player.getPuid());
			RedisProxy.getInstance().updateImmgrationRecord(termId, player.getId(), immgrationLog.toJSONString());
			RedisProxy.getInstance().addPlayerImmgrationLog(immgrationLog);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.successResponse("ok");
	}
}
