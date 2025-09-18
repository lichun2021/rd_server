package com.hawk.game.script;

import java.util.Map;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.BuildingService;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 荣耀等级一键满级
 * 
 * http://localhost:8080/script/honorLvUpToMax?playerId=
 * 
 * @author lating
 *
 */
public class HonorLevelUpToMax extends HawkScript {
	
	private static final int MAX_LEVEL = 30;
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			int cityLevel = player.getCityLevel();
			if (cityLevel < MAX_LEVEL) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "city level not enough");
			}
			
			BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.CONSTRUCTION_FACTORY);
			BuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, currCfg.getPostStage());
			while (nextLevelCfg != null) {
				BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
				nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, nextLevelCfg.getPostStage());
			}
			
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
}
