package com.hawk.game.idipscript.online.pack0627;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家装备情况
 *
 * localhost:8080/script/idip/4385
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4385")
public class QueryEquipHandler extends IdipScriptHandler {
	
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
		
		List<ArmourEntity> armourEntities = player.getData().getArmourEntityList();
		int count = 0;
		JSONArray array = new JSONArray();
		for (ArmourEntity armour : armourEntities) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			JSONObject jsonObj = new JSONObject();
			// 装备部位
			jsonObj.put("EquipPart", armourCfg.getPos());
			// 装备品质
			jsonObj.put("EquipQuality", armour.getQuality());
			// 装备ID
			jsonObj.put("EquipId", armour.getArmourId());
			// 装备强化等级
			jsonObj.put("StrenLevel", armour.getLevel());
			
			List<EffectObject> mainAttrList = armourCfg.getBaseAttrList();
			// 装备主属性
			jsonObj.put("MainPro", mainAttrList.isEmpty() ? 0 : mainAttrList.get(0).getEffectValue());
			
			List<ArmourEffObject> list = armour.getExtraAttrEff();
			int size = list.size();
			// 装备附加属性1
			jsonObj.put("AdditionalPro1", size > 0 ? list.get(0).getEffectValue() : 0);
			// 装备附加属性2
			jsonObj.put("AdditionalPro2", size > 1 ? list.get(1).getEffectValue() : 0);
			// 装备附加属性3
			jsonObj.put("AdditionalPro3", size > 2 ? list.get(2).getEffectValue() : 0);
			// 装备附加属性4
			jsonObj.put("AdditionalPro4", size > 3 ? list.get(3).getEffectValue() : 0);
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNum", (int)Math.ceil(count * 1.0d /IdipUtil.PAGE_SHOW_COUNT));
		result.getBody().put("EquipList_count", array.size());
		result.getBody().put("EquipList", array);
		
		return result;
	}
}


