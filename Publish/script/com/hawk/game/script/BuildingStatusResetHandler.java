package com.hawk.game.script;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.ArmyService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 建筑状态重置
 * http://localhost:8080/script/buildResetStatus?playerId=7pt-4by1s-3
 * playerName: 玩家名字
 * 
 * @author lating
 *
 */
public class BuildingStatusResetHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			for (ArmyEntity entity : player.getData().getArmyEntities()) {
				if (entity.getCureFinishCount() > 0) {
					int cureFinishCount = entity.getCureFinishCount();
					entity.addFree(cureFinishCount);
					entity.setCureFinishCount(0);
					LogUtil.logArmyChange(player, entity, cureFinishCount, ArmySection.FREE, ArmyChangeReason.CURE_COLLECT);
					HawkLog.logPrintln("change army cure finish count script, playerId: {}, armyId: {}, count: {}", 
							player.getId(), entity.getArmyId(), cureFinishCount);
				}
			}
			
			if (ArmyService.getInstance().getWoundedCount(player) > 0) {
				GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.SOLDIER_WOUNDED);
			} else {
				GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, BuildingStatus.COMMON);
			}
			
			HawkLog.logPrintln("change hospital status script, playerId: {}", player.getId());
			
			return successResponse("");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	
}
