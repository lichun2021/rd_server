package com.hawk.game.idipscript.third;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询角色建筑信息
 *
 * localhost:8080/script/idip/4233
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4233")
public class QueryPlayerBuildInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int pageNo = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNo > 1 ? (pageNo - 1) * IdipUtil.PAGE_SHOW_COUNT : 0;
		int indexEnd = indexStart + IdipUtil.PAGE_SHOW_COUNT;
		indexStart += 1;
		
		List<BuildingBaseEntity> buildingEntities = player.getData().getBuildingEntities();
		int count = 0;
		JSONArray array = new JSONArray();
		for (BuildingBaseEntity building : buildingEntities) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("BuildName", "");
			jsonObj.put("BuildId", building.getId());
			jsonObj.put("Buildlevel", buildingCfg.getLevel());
			jsonObj.put("LastUpgradeTime", HawkTime.formatTime(building.getLastUpgradeTime()));
			array.add(jsonObj);
		}
		
		result.getBody().put("RoleId", player.getId());
		result.getBody().put("RoleName", player.getNameEncoded());
		result.getBody().put("TotalPageNo", (int)Math.ceil(count * 1.0d /IdipUtil.PAGE_SHOW_COUNT));
		result.getBody().put("BuildList_count", array.size());
		result.getBody().put("BuildList", array);
		return result;
	}
	
}
