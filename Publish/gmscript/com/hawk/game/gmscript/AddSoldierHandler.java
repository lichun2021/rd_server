package com.hawk.game.gmscript;

import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 添加士兵
 *
 * localhost:8080/script/addSoldier?playerId=7py-ela5r-1&count=100
 *
 * @param playerId
 * @param count
 * 
 * @author lating
 */
public class AddSoldierHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			int count = Integer.valueOf(params.get("count"));
			List<ArmyEntity> armyEntities = player.getData().getArmyEntities();
			for (ArmyEntity entity : armyEntities) {
				entity.immSetTaralabsCountWithoutSync(count);
				LogUtil.logArmyChange(player, entity, count, count, ArmySection.TARALABS, ArmyChangeReason.GMSET);
			}
			
			  player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
			
			return HawkScript.successResponse("");
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
