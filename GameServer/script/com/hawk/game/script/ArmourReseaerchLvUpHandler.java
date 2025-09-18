package com.hawk.game.script;

import java.util.Map;
import java.util.Set;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.PlayerArmourModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GsConst;

/**
 * 装备科技一键升级
 * 
 * localhost:8080/script/equipLvlUp?playerId=1aat-2fqakl-1&all=1
 * 
 * @author lating
 *
 */
public class ArmourReseaerchLvUpHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
		    //  装备科技未解锁
			if (player.getEntity().getUnlockEquipResearch() <= 0) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "装备科技还未解锁");
			}
			
			// 默认只升级一期的装备科技
			int all = Integer.parseInt(params.getOrDefault("all", "0"));
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						researchLevelUp(player, all);
						return null;
					}
				}, threadIdx);
			} else {
				researchLevelUp(player, all);
			}
			
			return successResponse("success");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	
	private void researchLevelUp(Player player, int all) {
		PlayerArmourModule module = player.getModule(GsConst.ModuleType.ARMOUR_MODULE);
		Set<Integer> rechargeCfgIds = AssembleDataManager.getInstance().getEquipResearchIds();
		for (int researchId : rechargeCfgIds) {
			if (all == 0 && researchId > 7) {
				continue;
			}
			
			EquipResearchEntity entity = player.getData().getEquipResearchEntity(researchId);
			int nextLevel = entity.getResearchLevel() + 1;
			EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, nextLevel);
			while (cfg != null) {
				module.researchLevelUp(cfg.getResearchId(), cfg.getLevel());
				nextLevel += 1;
				cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(researchId, nextLevel);
			}
		}
	}
	
}
