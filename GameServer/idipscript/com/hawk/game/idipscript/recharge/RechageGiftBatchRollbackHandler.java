package com.hawk.game.idipscript.recharge;

import java.util.HashSet;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 游戏内直购礼包购买次数回滚（批量回滚） -- 10282167
 *
 * localhost:8081/idip/4483
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4483")
public class RechageGiftBatchRollbackHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isCsPlayer()) {
			result.getBody().put("Result", RechageGiftQueryHandler.CROSS_UNSUPPORT);
			result.getBody().put("RetMsg", "unsupport on cross server state");
			return result;
		}
		
		JSONArray goodsList = request.getJSONObject("body").getJSONArray("GoodsList");
		if (goodsList.isEmpty()) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "GoodsList param empty error");
			return result;
		}
		
		Set<String> set = new HashSet<>();
		for (int i = 0; i < goodsList.size(); i++) {
			JSONObject goodsObj = goodsList.getJSONObject(i);
			int goodsId = goodsObj.getIntValue("GoodsId");
			PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, String.valueOf(goodsId));
			if (giftCfg == null) {
				result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_INVLIAD);
				result.getBody().put("RetMsg", "config of goodsId not exist");
				return result;
			}
			
			if (giftCfg.getGiftType() != RechargeType.DIRECT_GIFT_ACTIVITY) {
				result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_UNSUPPORT);
				result.getBody().put("RetMsg", "batch request unsupport giftType");
				return result;
			}
			
			if (set.contains(giftCfg.getId())) {
				result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getBody().put("RetMsg", "goodsId repeated");
				return result;
			}
			set.add(giftCfg.getId());
		}
		
		String[] ids = set.toArray(new String[set.size()]);
		RedisProxy.getInstance().removeRechargeOutterGoodsId(player, ids);
		LogUtil.logIdipSensitivity(player, request, Integer.parseInt(ids[0]), 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}


