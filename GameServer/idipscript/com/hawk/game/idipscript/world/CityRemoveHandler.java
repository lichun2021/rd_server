package com.hawk.game.idipscript.world;

import java.util.ArrayList;
import java.util.List;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.city.CityManager;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 移除城点 -- 10282051
 *
 * localhost:8080/script/idip/4201
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4201")
public class CityRemoveHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		try {
			CityManager.getInstance().moveCity(player.getId(), true);
			GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					armysCheckAndFix(player);
				}
			});
			
			LogUtil.logIdipSensitivity(player, request, 0, 0);
			if (player.isActiveOnline()) {
				player.kickout(Status.IdipMsgCode.IDIP_MOVE_CITY_VALUE, true, null);
			}
			HawkLog.logPrintln("idip remove city, playerId: {}", player.getId());
			
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "remove city failed");
		}
		
		return result;
	}
	
	private void armysCheckAndFix(Player player) {
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			HawkLog.logPrintln("armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
	}
	
}
