package com.hawk.game.idipscript.recharge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.impl.directGift.DirectGiftActivity;
import com.hawk.activity.type.impl.directGift.cfg.DirectGiftCfg;
import com.hawk.activity.type.impl.greatGift.GreatGiftActivity;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftBagCfg;
import com.hawk.activity.type.impl.greatGift.entity.GreatGiftEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 游戏内直购礼包购买次数回滚  -- 10282152
 *
 * localhost:8081/idip/4453
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4453")
public class RechageGiftRollbackHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isCsPlayer()) {
			result.getBody().put("Result", RechageGiftQueryHandler.CROSS_UNSUPPORT);
			result.getBody().put("RetMsg", "跨服状态下不支持，需要回到原服才能处理");
			return result;
		}
		
		int goodsId = request.getJSONObject("body").getIntValue("GoodsId");
		String goodsIdKey = String.valueOf(goodsId);
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsIdKey);
		if (giftCfg == null) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_INVLIAD);
			result.getBody().put("RetMsg", "商品id错误，不存在对应的商品配置");
			return result;
		}
		
		switch (giftCfg.getGiftType()) {
		case RechargeType.GREAT_GIFT:
			return greatGiftHandle(player, giftCfg.getId(), request, result);
		case RechargeType.DIRECT_GIFT_ACTIVITY:
			return directGift0104Handle(player, giftCfg.getId(), request, result);
		default:
			break;
		}
		
		result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_UNSUPPORT);
		result.getBody().put("RetMsg", "当前不支持这个商品id相关活动");
		return result;
	}
	
	/**
	 * 20240104版本直购礼包购买回滚
	 * @param player
	 * @param goodsId
	 * @param request
	 * @param result
	 * @return
	 */
	private IdipResult directGift0104Handle(Player player, String goodsId, JSONObject request, IdipResult result) {
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		if (goodsIds.contains(goodsId)) {
			RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
			LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			return result;
		}
		
		DirectGiftCfg curCfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
		if(curCfg == null || !goodsId.equals(curCfg.getPayGiftIdByChannel(player.getEntity().getPlatform()))){
			result.getBody().put("Result", RechageGiftQueryHandler.PLATFORM_NOT_MATCH);
			result.getBody().put("RetMsg", "商品id渠道和玩家角色平台不匹配");
			return result;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DIRECT_GIFT_VALUE);
		DirectGiftActivity activity = (DirectGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){
			result.getBody().put("Result", RechageGiftQueryHandler.ACTIVITY_NOT_OPEN);
			result.getBody().put("RetMsg", "活动未开启");
			return result;
		}
		
		activity.rollback(player.getId(), goodsId);
		LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
        result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 超值好礼礼包购买回滚
	 * @param player
	 * @param goodsId
	 * @param request
	 * @param result
	 * @return
	 */
	private IdipResult greatGiftHandle(Player player, String goodsId, JSONObject request, IdipResult result) {
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		if (goodsIds.contains(goodsId)) {
			RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
			LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			return result;
		}
		
		GreatGiftBagCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, goodsId);
		if(!curCfg.getChannelType().equals(player.getEntity().getPlatform())){
			result.getBody().put("Result", RechageGiftQueryHandler.PLATFORM_NOT_MATCH);
			result.getBody().put("RetMsg", "商品id渠道和玩家角色平台不匹配");
			return result;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GREAT_GIFT_VALUE);
		GreatGiftActivity activity = (GreatGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			result.getBody().put("Result", RechageGiftQueryHandler.ACTIVITY_NOT_OPEN);
			result.getBody().put("RetMsg", "活动未开启");
			return result;
		}
		
		Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		GreatGiftEntity entity = opEntity.get();
		// 玩家在线，或者还没有跨天
		if (player.isActiveOnline() || HawkTime.isSameDay(HawkTime.getMillisecond(), player.getLogoutTime())) {
			greatGiftRollback(activity, entity, player, goodsId, request, result);
		} else {
			if(!activity.allBuyBeforeDay(entity)) {
				greatGiftRollback(activity, entity, player, goodsId, request, result);
			} else {
				greatGiftRollbackCrossDay(activity, player, goodsId, request, result);
			}
		}
		
		return result;
	}
	
	/**
	 * 回滚处理
	 * @param activity
	 * @param entity
	 * @param player
	 * @param goodsId
	 * @param request
	 * @param result
	 */
	private void greatGiftRollback(GreatGiftActivity activity, GreatGiftEntity entity, Player player, String goodsId, JSONObject request, IdipResult result) {
		List<String> bagList = entity.getBagList();
		// 返回错误
		if (bagList.isEmpty() || !bagList.get(bagList.size() -1).equals(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_STAGE_ERROR);
			result.getBody().put("RetMsg", "非玩家购买的最新一档，不支持回滚");
			return;
		}
		
		if (!entity.getOutBagList().contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.INNER_ROLLBACK_ERROR);
			result.getBody().put("RetMsg", "游戏内购买的，不支持回滚");
			return;
		}
		
		// 回滚
		activity.rollback(entity, goodsId);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
	}
	
	/**
	 * 跨天后购买缓存检查
	 * @param activity
	 * @param player
	 * @param goodsId
	 * @param request
	 * @param result
	 */
	private void greatGiftRollbackCrossDay(GreatGiftActivity activity, Player player, String goodsId, JSONObject request, IdipResult result) {
		String key = RechageGiftCompleteHandler.getRedisKey(activity.getActivityId(), player.getId());
		Map<String, String> giftIdMap = ActivityGlobalRedis.getInstance().hgetAll(key);
	    if (giftIdMap.isEmpty()) {
	    	Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			GreatGiftEntity entity = opEntity.get();
	    	greatGiftRollback(activity, entity, player, goodsId, request, result);
			return;
        }
        
        List<String> bagIdList = new ArrayList<>(giftIdMap.keySet());
        bagIdList.sort(new Comparator<String>() {
			@Override
			public int compare(String id1, String id2) {
				GreatGiftBagCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id1);
				GreatGiftBagCfg cfg2 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id2);
				return cfg1.getGiftStage() - cfg2.getGiftStage();
			}
        });
        
        // 返回错误
        String lastBoughtId = bagIdList.get(bagIdList.size() -1);
        if (!lastBoughtId.equals(goodsId)) {
        	result.getBody().put("Result", RechageGiftQueryHandler.GOODS_STAGE_ERROR);
			result.getBody().put("RetMsg", "非玩家购买的最新一档，不支持回滚");
			return;
        }
        
        ActivityGlobalRedis.getInstance().hDel(key, goodsId);
        result.getBody().put("Result", 0);
        result.getBody().put("RetMsg", "");
        LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
	}
	
}


