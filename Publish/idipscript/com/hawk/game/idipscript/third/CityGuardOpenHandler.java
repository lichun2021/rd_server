package com.hawk.game.idipscript.third;

import org.hawk.app.HawkAppObj;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 单区全员开启保护罩
 *
 * localhost:8080/script/idip/4205
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4205")
public class CityGuardOpenHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				openCityShieldToAll(request);
				return true;
			}
		});
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 对全服玩家开启保护罩
	 * 
	 * @param shieldTime
	 */
	private void openCityShieldToAll(JSONObject request) {
		int shieldTime = request.getJSONObject("body").getIntValue("EffectiveDate");  // 单位：分钟
		long now = HawkTime.getMillisecond();
		long endTime = now + shieldTime * 60000L;
		
		GlobalData.getInstance().clearBrokenProtectPlayer();
		GlobalData.getInstance().setGlobalProtectEndTime(endTime);

//		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
//		for (String playerId : playerIds) {
//			PlayerData playerData = GlobalData.getInstance().getPlayerData(playerId, true);
//			StatusDataEntity entity = playerData.getStatusById(Const.EffType.CITY_SHIELD_VALUE);
//			if (entity == null) {
//				HawkLog.warnPrintln("idip open city shield failed, playerId: {}", playerId);
//				continue;
//			}
//			
//			if (endTime < entity.getEndTime()) {
//				continue;
//			}
//			
//			Player player = GlobalData.getInstance().makesurePlayer(playerId);
//			if (GlobalData.getInstance().isOnline(playerId)) {
//				player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new OpenCityShieldMsgInvoker(entity, now, endTime));
//			} else {
//				changeProtectData(entity, now, endTime);
//			}
//			
//			LogUtil.logIdipSensitivity(player, request, 0, 0);
//		}
		
	}
	
	public static class OpenCityShieldMsgInvoker extends HawkMsgInvoker {
		private StatusDataEntity entity;
		private long startTime;
		private long endTime;
		
		public OpenCityShieldMsgInvoker(StatusDataEntity entity, long startTime, long endTime) {
			this.entity = entity;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changeProtectData(entity, startTime, endTime);
			Player player = GlobalData.getInstance().scriptMakesurePlayer(entity.getPlayerId());
			player.getPush().syncPlayerEffect(Const.EffType.CITY_SHIELD);
			return true;
		}
	}
	
	/**
	 * 修改玩家数据
	 * @param entity
	 * @param startTime
	 * @param endTime
	 */
	private static void changeProtectData(StatusDataEntity entity, long startTime, long endTime) {
		entity.setVal(GsConst.ProtectState.POP_TOOL);
		entity.setStartTime(startTime);
		entity.setEndTime(endTime);
		entity.resetShieldNoticed(false);
		entity.setInitiative(false);
		WorldPlayerService.getInstance().updateWorldPointProtected(entity.getPlayerId(), endTime);
		
		HawkLog.logPrintln("idip open city shield, playerId: {}, endTime: {}", entity.getPlayerId(), endTime);
	}
	
}
