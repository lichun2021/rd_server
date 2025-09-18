package com.hawk.game.idipscript.army;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家兵力情况 -- 10282122
 *
 * localhost:8080/script/idip/4387
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4387")
public class QueryArmyInfoHandler extends IdipScriptHandler {
	
	static final int MAX_SOLDIERLIST_NUM = 30;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int pageNo = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNo > 1 ? (pageNo - 1) * MAX_SOLDIERLIST_NUM : 0;
		int indexEnd = indexStart + MAX_SOLDIERLIST_NUM;
		indexStart += 1;
		
		List<ArmyEntity> armyEntities = player.getData().getArmyEntities();
		int count = 0, totalCount = (int) armyEntities.stream().filter(e -> (e.getFree() + e.getMarch()) > 0).count();
		JSONArray array = new JSONArray();
		for (ArmyEntity army : armyEntities) {
			int soldierCount = army.getFree() + army.getMarch();
			if (soldierCount <= 0) {
				continue;
			}
			
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("SoldierId", army.getArmyId());
			jsonObj.put("SoldierLeve", armyCfg.getLevel());
			jsonObj.put("SoldierNum",  soldierCount);
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNum", (int)Math.ceil(totalCount * 1.0d /MAX_SOLDIERLIST_NUM));
		result.getBody().put("SoldierList_count", array.size());
		result.getBody().put("SoldierList", array);
		
		return result;
	}
}


