package com.hawk.game.idipscript.army;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;


/**
 * 修改玩家兵种数量 -- 10282123
 *
 * localhost:8080/script/idip/4389
 *
 * @param OpenId  用户openId
 * @param SoldierType
 * @param SoldierLeve
 * @param SoldierNum
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4389")
public class ChangeArmyCountHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int soldierType = request.getJSONObject("body").getInteger("SoldierType");
		int level = request.getJSONObject("body").getInteger("SoldierLeve");
		int num = request.getJSONObject("body").getInteger("SoldierNum");
		
		int armyId = 0;
		ConfigIterator<BattleSoldierCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		while (configIterator.hasNext()) {
			BattleSoldierCfg cfg = configIterator.next();
			if (cfg.getType() == soldierType && cfg.getLevel() == level) {
				armyId = cfg.getId();
				break;
			}
		}
		
		if (armyId == 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "type or level param error");
			return result;
		}
		
		ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
		if (armyEntity == null) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "type level soldier not exist");
			return result;
		}
		
		if (num < 0 && num + armyEntity.getFree() < 0) {
			num = 0 - armyEntity.getFree();
		}
		
		armyEntity.addFree(num);
		LogUtil.logArmyChange(player, armyEntity, num, ArmySection.FREE, ArmyChangeReason.AWARD);
		if (player.isActiveOnline()) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	     	map.put(armyId, num);
	     	player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT, map);
		}
		
		HawkLog.logPrintln("idip change soldierCount, playerId: {}, armyId: {}, count: {}", player.getId(), armyId, num);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


