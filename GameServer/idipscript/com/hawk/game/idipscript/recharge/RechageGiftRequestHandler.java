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
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 游戏外商城发起礼包购买请求 -- 10282153
 *
 * localhost:8081/idip/4451
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4455")
public class RechageGiftRequestHandler extends IdipScriptHandler {
	
	public static final String RECHARGE_REDIS_KEY = "gift_rechage_daily";
	
	public static final int EXPIRE_TIME = (int)(HawkTime.MINUTE_MILLI_SECONDS / 1000) * 16;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isCsPlayer()) {
			result.getBody().put("Result", RechageGiftQueryHandler.CROSS_UNSUPPORT);
			result.getBody().put("RetMsg", "跨服状态下不支持，需要回到原服才能购买");
			return result;
		}
		
		int goodsId = request.getJSONObject("body").getIntValue("GoodsId");
		int goodsNum = request.getJSONObject("body").getIntValue("GoodsNum");
		if (goodsNum <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "参数错误");
			return result;
		}
		
		String goodsIdKey = String.valueOf(goodsId);
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsIdKey);
		if (giftCfg == null) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_INVLIAD);
			result.getBody().put("RetMsg", "商品id错误，不存在对应的商品配置");
			return result;
		}
		
		switch (giftCfg.getGiftType()) {
		case RechargeType.GREAT_GIFT:
			return buyGreatGift(player, giftCfg.getId(), request, result);
		case RechargeType.DIRECT_GIFT_ACTIVITY:
			return directGift0104Buy(player, giftCfg.getId(), goodsNum, result);
		default:
			break;
		}
		
		result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_UNSUPPORT);
		result.getBody().put("RetMsg", "当前不支持这个商品id相关活动");
		return result;
	}
	
	/**
	 * 20240104版本的直购礼包
	 * @param player
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private IdipResult directGift0104Buy(Player player, String goodsId, int goodsNum, IdipResult result) {
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
		
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		if (goodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "此商品礼包上一次发起的购买还未完成，不能重复发起购买");
			return result;
		}
		
		String playerId = player.getId();
		if(!activity.buyGiftCheck(playerId, goodsId, goodsNum)){
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_COND_ERROR);
			result.getBody().put("RetMsg", "商品购买条件不满足");
			return result;
		}
		
		goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(playerId);
		if (goodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "此礼包游戏内发起的购买还未完成，不能重复发起购买");
			return result;
		}
		
		// 记录已发起购买还未完成的礼包ID
		RedisProxy.getInstance().addRechargeOutterGoodsId(player, goodsId);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 购买超值礼包
	 * @param player
	 * @param goodsId
	 * @param request
	 * @param result
	 * @return
	 */
	private IdipResult buyGreatGift(Player player, String goodsId, JSONObject request, IdipResult result) {
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
		
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		if (goodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "此商品礼包上一次发起的购买还未完成，不能重复发起购买");
			return result;
		}
		
		// 玩家在线，或者还没有跨天
		if (player.isActiveOnline() || HawkTime.isSameDay(HawkTime.getMillisecond(), player.getLogoutTime())) {
			buyGreatGift(activity, player, goodsId, request, result);
		} else {
			Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			GreatGiftEntity entity = opEntity.get();
			if(!activity.allBuyBeforeDay(entity)) {
				buyGreatGift(activity, player, goodsId, request, result);
			} else {
				buyGreatGiftCrossDay(activity, player, goodsId, request, result);
			}
		}
		
		return result;
	}
	
	/**
	 * 购买超值礼包（不满足跨天重置的情况）
	 * @param activity
	 * @param playerId
	 * @param goodsId
	 * @param crossDay
	 * @param result
	 */
	private void buyGreatGift(GreatGiftActivity activity, Player player, String goodsId, JSONObject request, IdipResult result) {
		String playerId = player.getId();
		if(!activity.canBuy(playerId, goodsId)){
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_COND_ERROR);
			result.getBody().put("RetMsg", "商品购买条件不满足");
			return;
		}
		
		Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		GreatGiftEntity entity = opEntity.get();
		if (entity.getBagList().contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_BOUGHT);
			result.getBody().put("RetMsg", "已购买过此商品，不能重复购买");
			return;
		}
		
		Set<String> goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(playerId);
		if (goodsIds.contains(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.RECHARGE_UNCOMPLETE);
			result.getBody().put("RetMsg", "此礼包游戏内发起的购买还未完成，不能重复发起购买");
			return;
		}
		
		// 记录已发起购买还未完成的礼包ID
		RedisProxy.getInstance().addRechargeOutterGoodsId(player, goodsId);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
	}
	
	/**
	 * 满足跨天重置的情况下对缓存数据进行判断
	 * @param activity
	 * @param playerId
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private void buyGreatGiftCrossDay(GreatGiftActivity activity, Player player, String goodsId, JSONObject request, IdipResult result) {
		Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		GreatGiftEntity entity = opEntity.get();
		if (HawkTime.isSameDay(entity.getFinishTime(), HawkTime.getMillisecond())) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_BOUGHT);
			result.getBody().put("RetMsg", "已购买过此商品，不能重复购买");
			return;
		}
		
		String key = RechageGiftCompleteHandler.getRedisKey(activity.getActivityId(), player.getId());
		Map<String, String> giftIdMap = ActivityGlobalRedis.getInstance().hgetAll(key);
		if (giftIdMap.containsKey(goodsId)) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_BOUGHT);
			result.getBody().put("RetMsg", "已购买过此商品，不能重复购买");
			return;
		}
		
		if (!giftIdMap.isEmpty()) {
			List<String> bagIdList = new ArrayList<>(giftIdMap.keySet());
	        bagIdList.sort(new Comparator<String>() {
				@Override
				public int compare(String id1, String id2) {
					GreatGiftBagCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id1);
					GreatGiftBagCfg cfg2 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id2);
					return cfg1.getGiftStage() - cfg2.getGiftStage();
				}
	        });
	        
	        List<GreatGiftBagCfg> cfgList = GreatGiftBagCfg.getSortedCfgList(player.getEntity().getPlatform());
	        GreatGiftBagCfg configLastCfg = cfgList.get(cfgList.size() -1);
	        String boughtLastGoodsId = bagIdList.get(bagIdList.size() -1);
	        if (configLastCfg.getGiftId().equals(boughtLastGoodsId)) {
	        	result.getBody().put("Result", RechageGiftQueryHandler.GOODS_BOUGHT);
				result.getBody().put("RetMsg", "已购买过此商品，不能重复购买");
				return;
	        } else {
	        	GreatGiftBagCfg boughtLastGoodsCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, boughtLastGoodsId);
	        	int index = cfgList.indexOf(boughtLastGoodsCfg);
	        	GreatGiftBagCfg nextCfg = cfgList.get(index + 1);
	        	if (!nextCfg.getGiftId().equals(goodsId)) {
	        		result.getBody().put("Result", RechageGiftQueryHandler.GOODS_COND_ERROR);
	    			result.getBody().put("RetMsg", "商品购买条件不满足");
					return;
	        	}
	        }
		}
		
		// 记录已发起购买还未完成的礼包ID
		RedisProxy.getInstance().addRechargeOutterGoodsId(player, goodsId);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
	}
	
}


