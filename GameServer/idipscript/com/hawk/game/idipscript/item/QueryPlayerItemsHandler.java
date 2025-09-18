package com.hawk.game.idipscript.item;

import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询角色背包信息 -- 10282018
 *
 * localhost:8080/script/idip/4159
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4159")
public class QueryPlayerItemsHandler extends IdipScriptHandler {
	
	static final int MAX_ITEMLIST_NUM = 20;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int pageNum = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNum > 1 ? (pageNum - 1) * MAX_ITEMLIST_NUM : 0;
		int indexEnd = indexStart + MAX_ITEMLIST_NUM;
		indexStart += 1;
		
		List<ItemEntity> itemList = player.getData().getItemEntities();
		JSONArray itemArray = new JSONArray();
		int count = 0, totalCount = (int)itemList.stream().filter(e -> e.getItemCount() > 0).count();
		for (ItemEntity entity : itemList) {
			if (entity.getItemCount() <= 0) {
				continue;
			}
			
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			JSONObject item = new JSONObject();
			item.put("ItemId", entity.getItemId());
			item.put("ItemNum", entity.getItemCount());
			itemArray.add(item);
		}
		
		result.getBody().put("TotalPageNum", (int)Math.ceil(totalCount * 1.0d /MAX_ITEMLIST_NUM));
		result.getBody().put("ItemList_count", itemArray.size());
		result.getBody().put("ItemList", itemArray);
		
		return result;
	}
}
