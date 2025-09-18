package com.hawk.game.idipscript.roleexchange;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 获取角色筛选字段（心悦角色交易）请求 -- 10282185
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4519")
public class QueryRoleFilterHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4519 QueryRoleFilter error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		//获取角色筛选字段, 待策划给出返回信息定义
		JSONObject filterObj = new JSONObject();
		BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
		List<ArmyEntity> armyList = player.getData().getArmyEntities();
		Set<Integer> armyTypeSet = new HashSet<>();
		for (ArmyEntity entity : armyList) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			if (cfg.getType() < 100) {
				armyTypeSet.add(cfg.getType());
			}
		}
		
		JSONArray array = new JSONArray();
		array.addAll(armyTypeSet);
		filterObj.put("armyType", array);
		filterObj.put("baseType", buildingCfg.getLevel() < 30 ? 1 : 2);
		filterObj.put("baseLevel", buildingCfg.getLevel());
		filterObj.put("basePercentage", buildingCfg.getProgress());
		filterObj.put("vip", player.getVipLevel());
		filterObj.put("power", player.getPower());
		JSONObject json = new JSONObject();
		json.put("gameFilter", filterObj);
		String resultData = json.toJSONString();
		if (GsConfig.getInstance().isXinyueRoleEncode()) {
			try {
				byte[] textByte = resultData.getBytes("UTF-8");
				resultData = Base64.getEncoder().encodeToString(textByte);
				resultData = URLEncoder.encode(resultData, "UTF-8");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		result.getBody().put("Data", resultData);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
