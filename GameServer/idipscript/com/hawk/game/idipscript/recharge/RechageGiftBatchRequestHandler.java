package com.hawk.game.idipscript.recharge;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.directGift.DirectGiftActivity;
import com.hawk.activity.type.impl.directGift.cfg.DirectGiftCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.recharge.RechargeType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 游戏外商城发起礼包购买请求（批量购买） -- 10282166
 *
 * localhost:8081/idip/4481
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4481")
public class RechageGiftBatchRequestHandler extends IdipScriptHandler {
	
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
		
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < goodsList.size(); i++) {
			JSONObject goodsObj = goodsList.getJSONObject(i);
			int goodsId = goodsObj.getIntValue("GoodsId");
			int goodsNum = goodsObj.getIntValue("GoodsNum");
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
			
			if (goodsNum <= 0) {
				result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getBody().put("RetMsg", "goodsNum param invalid");
				return result;
			}
			
			if (map.containsKey(giftCfg.getId())) {
				result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
				result.getBody().put("RetMsg", "goodsId repeated");
				return result;
			}
			map.put(giftCfg.getId(), goodsNum);
		}
		
		Set<String> outterRechargeGoodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		Set<String> innerRechargeGoodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(player.getId());
		for (Entry<String, Integer> entry : map.entrySet()) {
			boolean success = directGift0104Buy(player, entry.getKey(), entry.getValue(), result, outterRechargeGoodsIds, innerRechargeGoodsIds);
			if (!success) {
				return result;
			}
		}
		
		String[] ids = map.keySet().toArray(new String[map.keySet().size()]);
		RedisProxy.getInstance().addRechargeOutterGoodsId(player, ids);
		return result;
	}
	
	/**
	 * 尊享好礼礼包
	 * @param player
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private boolean directGift0104Buy(Player player, String goodsId, int goodsNum, IdipResult result, Set<String> outterRechargeGoodsIds, Set<String> innerRechargeGoodsIds) {
		DirectGiftCfg curCfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
		if(curCfg == null || !goodsId.equals(curCfg.getPayGiftIdByChannel(player.getEntity().getPlatform()))){
			result.getBody().put("Result", RechageGiftQueryHandler.PLATFORM_NOT_MATCH);
			result.getBody().put("RetMsg", "gift pay channel not match the player channel");
			return false;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DIRECT_GIFT_VALUE);
		DirectGiftActivity activity = (DirectGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			result.getBody().put("Result", RechageGiftQueryHandler.ACTIVITY_NOT_OPEN);
			result.getBody().put("RetMsg", "activity not open");
			return false;
		}
		
		if (outterRechargeGoodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "last request unfinish");
			return false;
		}
		
		String playerId = player.getId();
		if(!activity.buyGiftCheck(playerId, goodsId, goodsNum)){
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_COND_ERROR);
			result.getBody().put("RetMsg", "gameserver buyGift check failed");
			return false;
		}
		
		if (innerRechargeGoodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "gameserver buyGift unfinish");
			return false;
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return true;
	}
	
}


