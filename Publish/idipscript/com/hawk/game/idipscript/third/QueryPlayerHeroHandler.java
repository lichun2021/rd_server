package com.hawk.game.idipscript.third;

import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 英雄信息查询
 *
 * localhost:8080/script/idip/4215
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4215")
public class QueryPlayerHeroHandler extends IdipScriptHandler {
	
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
		
		List<HeroEntity> heros = player.getData().getHeroEntityList();
		int count = 0;
		JSONArray array = new JSONArray();
		for (HeroEntity entity : heros) {
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("HeroId", entity.getHeroId());
			jsonObj.put("Time", entity.getCreateTime() / 1000);
			jsonObj.put("Lv", entity.getHeroObj().getLevel());
			jsonObj.put("HeroStar", entity.getStar());
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(count * 1.0d /IdipUtil.PAGE_SHOW_COUNT));
		result.getBody().put("List", array);
		result.getBody().put("List_count", array.size());
		
		return result;
	}
	
}
