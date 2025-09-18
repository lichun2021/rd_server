package com.hawk.game.idipscript.health;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.city.CityManager;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;


/**
 * 中控禁玩接口 -- 10282101
 *
 * localhost:8080/script/idip/4337
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4337")
public class HealthControlBanHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String title = request.getJSONObject("body").getString("ZK_Title");
		title = IdipUtil.decode(title);
		String msg = request.getJSONObject("body").getString("ZK_Msg");
		msg = IdipUtil.decode(msg);
		String traceId = request.getJSONObject("body").getString("TraceId");
		
		long beginTime = request.getJSONObject("body").getInteger("BeginTime");  // 单位秒
		long endTime = request.getJSONObject("body").getInteger("EndTime");  // 单位秒
		beginTime *= 1000L;
		endTime *= 1000L;
		if (endTime < HawkApp.getInstance().getCurrentTime() || beginTime >= endTime) {
			HawkLog.errPrintln("zk ban script param error, playerId: {}, startTime: {}, endTime: {}", player.getId(), beginTime, endTime);
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "time param error");
			return result;
		}
		
		if (player.isActiveOnline()) {
			if (beginTime <= HawkApp.getInstance().getCurrentTime()) {
				player.sendHealthGameRemind(ReportType.DEFAULT_REMIND_VALUE, endTime, title, msg, traceId, "");
			}
			
			player.kickout(0, true, msg);
		} 
		
		// 移除城点
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				removeCity(player);
				return null;
			}
		}, threadIdx);
				
		HawkLog.logPrintln("zk ban online player, playerId: {}, title: {}, msg: {}, traceId: {}, startTime: {}, endTime: {}", player.getId(), title, msg, traceId, beginTime, endTime);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	/**
	 * 移除城点
	 * 
	 * @param player
	 */
	protected void removeCity(Player player) {
		try {
			CityManager.getInstance().moveCity(player.getId(), true);
			GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					armysCheckAndFix(player);
				}
			});
			
			// 中控飞堡记录
			LogUtil.logZKRemoveCity(player);
			HawkLog.logPrintln("zk ban remove city, playerId: {}", player.getId());
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测兵
	 * @param player
	 */
	private void armysCheckAndFix(Player player) {
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			HawkLog.logPrintln("zk ban armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				HawkLog.logPrintln("zk ban armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			HawkLog.logPrintln("zk ban armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
	}
	
}


