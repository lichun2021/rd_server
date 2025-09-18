package com.hawk.game.idipscript.attrmodify;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.BuildingService;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改建筑等级 -- 10282034
 *
 * localhost:8080/script/idip/4197
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4197")
public class ChangePlayerBuildLevelHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String buildingId = request.getJSONObject("body").getString("BuildUuid");
		int finalLevel = request.getJSONObject("body").getIntValue("LastLevel");
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(buildingId);
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (buildingEntity == null || buildingCfg.getLevel() >= finalLevel) {
			HawkLog.logPrintln("idip change building level failed, {}", buildingEntity == null ? "building not exist" : "buiding level gt finalLevel");
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			return result;
		}
		
		if (player.isActiveOnline()) {
			player.dealMsg(MsgId.IDIP_CHANGE_PLAYER_INFO, new ChangeBuildLevelMsgInvoker(player, request, buildingEntity, finalLevel));
		} else {
			try {
				changeBuildLevel(request, player, buildingEntity, finalLevel);
			} catch (Exception e) {
				HawkException.catchException(e);
				result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
				result.getBody().put("RetMsg", "change build level failed");
				return result;
			}
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	public static class ChangeBuildLevelMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		private BuildingBaseEntity buildingEntity;
		private int finalLevel;
		
		public ChangeBuildLevelMsgInvoker(Player player, JSONObject request, BuildingBaseEntity buildingEntity, int finalLevel) {
			this.player = player;
			this.request = request;
			this.buildingEntity = buildingEntity;
			this.finalLevel = finalLevel;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			changeBuildLevel(request, player, buildingEntity, finalLevel);
			return true;
		}
	}
	
	/**
	 * 修改建筑等级
	 * @param request
	 * @param player
	 */
	private static void changeBuildLevel(JSONObject request, Player player, BuildingBaseEntity buildingEntity, int finalLevel) {
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		int changeLevel = finalLevel - buildingCfg.getLevel();
		for (int i = 0; i < changeLevel; i++) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, finalLevel);
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_CHANGE_BUILD_LEVEL_VALUE, true, null);
		}
		HawkLog.logPrintln("idip change build level, playerId: {}, buildingId: {}, startLevel: {}, finalLevel: {}", 
				player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId() - changeLevel, buildingEntity.getBuildingCfgId());
	}
	
	
}
