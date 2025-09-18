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
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyDirectGiftEvent;
import com.hawk.activity.event.impl.GreatGiftBuyEvent;
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
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 游戏外商城购买礼包完成通知 -- 10282151
 *
 * localhost:8081/idip/4451
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4451")
public class RechageGiftCompleteHandler extends IdipScriptHandler {
	
	public static final String RECHARGE_REDIS_KEY = "gift_rechage_daily";
	
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
			return directGift0104Buy(player, giftCfg.getId(), goodsNum, request, result);
		default:
			break;
		}
		
		result.getBody().put("Result", RechageGiftQueryHandler.GOODS_ID_UNSUPPORT);
		result.getBody().put("RetMsg", "当前不支持这个商品id相关活动");
		return result;
	}
	
	/**
	 * 获取存储redis数据的key
	 * @param activityId
	 * @param playerId
	 * @return
	 */
	public static String getRedisKey(int activityId, String playerId) {
		String todayTime = String.valueOf(HawkTime.getYyyyMMddIntVal());
		String key = RECHARGE_REDIS_KEY + ":" + activityId + ":" + todayTime + ":" + playerId;
		return key;
	}
	
	/**
	 * 20240104版本的直购礼包
	 * @param player
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private IdipResult directGift0104Buy(Player player, String goodsId, int goodsNum, JSONObject request, IdipResult result) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DIRECT_GIFT_VALUE);
		DirectGiftActivity activity = (DirectGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
			// 活动已经结束了，直接发货
			if (goodsIds.contains(goodsId)) {
				RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
				LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
				result.getBody().put("Result", 0);
				result.getBody().put("RetMsg", "activity end");
				return result;
			}
			
			result.getBody().put("Result", RechageGiftQueryHandler.ACTIVITY_NOT_OPEN);
			result.getBody().put("RetMsg", "活动未开启");
			return result;
		}
		
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		// 直接发货
		if (goodsIds.contains(goodsId)) {
			RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
			// deliverLast
			directGift0104DiliverGoods(player, goodsId, goodsNum, request);
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
		
		String playerId = player.getId();
		if(!activity.buyGiftCheck(playerId, goodsId, goodsNum)){
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_COND_ERROR);
			result.getBody().put("RetMsg", "商品购买条件不满足");
			return result;
		}
		
		// deliverLast
		directGift0104DiliverGoods(player, goodsId, goodsNum, request);
		
		// 购买成功
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 直购礼包发货
	 * @param player
	 * @param goodsId
	 * @param goodsNum
	 * @param request
	 */
	private void directGift0104DiliverGoods(Player player, String goodsId, int goodsNum, JSONObject request) {
		BuyDirectGiftEvent event = new BuyDirectGiftEvent(player.getId(), goodsId);
		event.setTimes(goodsNum);
		event.setRewardDeliver(false);
		ActivityManager.getInstance().postEvent(event);
		LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
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
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GREAT_GIFT_VALUE);
		GreatGiftActivity activity = (GreatGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
			// 活动已经结束了，直接发货
			if (goodsIds.contains(goodsId)) {
				RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
				otherActivityHandler(player, goodsId, request);
				result.getBody().put("Result", 0);
				result.getBody().put("RetMsg", "");
				return result;
			}
			
			result.getBody().put("Result", RechageGiftQueryHandler.ACTIVITY_NOT_OPEN);
			result.getBody().put("RetMsg", "活动未开启");
			return result;
		}
		
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		// 直接发货
		if (goodsIds.contains(goodsId)) {
			RedisProxy.getInstance().removeRechargeOutterGoodsId(player, goodsId);
			rechargeDeliver(player, activity, goodsId, request, result);
			return result;
		}
		
		GreatGiftBagCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, goodsId);
		if(!curCfg.getChannelType().equals(player.getEntity().getPlatform())){
			result.getBody().put("Result", RechageGiftQueryHandler.PLATFORM_NOT_MATCH);
			result.getBody().put("RetMsg", "商品id渠道和玩家角色平台不匹配");
			return result;
		}
		
		// 玩家在线，或者还没有跨天
		if (player.isActiveOnline() || HawkTime.isSameDay(HawkTime.getMillisecond(), player.getLogoutTime())) {
			buyGreatGift(player, activity, goodsId, request, result);
		} else {
			Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			GreatGiftEntity entity = opEntity.get();
			if(!activity.allBuyBeforeDay(entity)) {
				buyGreatGift(player, activity, goodsId, request, result);
			} else {
				buyGreatGiftCrossDay(player, activity, goodsId, request, result);
			}
		}
		
		return result;
	}
	
	/**
	 * 已发起过购买请求的，直接发货
	 * @param player
	 */
	private void rechargeDeliver(Player player, GreatGiftActivity activity, String goodsId, JSONObject request, IdipResult result) {
		if (player.isActiveOnline() || HawkTime.isSameDay(HawkTime.getMillisecond(), player.getLogoutTime())) {
			deliverLast(player, activity, goodsId, request, result, false);
		} else {
			Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			GreatGiftEntity entity = opEntity.get();
			if(!activity.allBuyBeforeDay(entity)) {
				deliverLast(player, activity, goodsId, request, result, false);
			} else {
				deliverLast(player, activity, goodsId, request, result, true);
			}
		}
	}
	
	/**
	 * 购买超值礼包（不满足跨天重置的情况）
	 * @param activity
	 * @param playerId
	 * @param goodsId
	 * @param crossDay
	 * @param result
	 */
	private void buyGreatGift(Player player, GreatGiftActivity activity, String goodsId, JSONObject request, IdipResult result) {
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
		
		// 超值好礼活动购买礼包事件触发
		deliverLast(player, activity, goodsId, request, result, false);
	}
	
	/**
	 * 满足跨天重置的情况下对缓存数据进行判断
	 * @param activity
	 * @param playerId
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private void buyGreatGiftCrossDay(Player player, GreatGiftActivity activity, String goodsId, JSONObject request, IdipResult result) {
		Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		GreatGiftEntity entity = opEntity.get();
		if (HawkTime.isSameDay(entity.getFinishTime(), HawkTime.getMillisecond())) {
			result.getBody().put("Result", RechageGiftQueryHandler.GOODS_BOUGHT);
			result.getBody().put("RetMsg", "已购买过此商品，不能重复购买");
			return;
		}
		
		String key = getRedisKey(activity.getActivityId(), player.getId());
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
		
		// 超值好礼活动，玩家跨天后还未登录过游戏，先记redis
		deliverLast(player, activity, goodsId, request, result, true);
	}
	
	/**
	 * 最终发货
	 * @param player
	 * @param activityId
	 * @param goodsId
	 * @param request
	 * @param result
	 * @param crossDay
	 */
	private void deliverLast(Player player, GreatGiftActivity activity, String goodsId, JSONObject request, IdipResult result, boolean crossDay) {
		if (!crossDay) {
			ActivityManager.getInstance().postEvent(new GreatGiftBuyEvent(player.getId(), goodsId));
		} else {
			String key = getRedisKey(activity.getActivityId(), player.getId());
			ActivityGlobalRedis.getInstance().hset(key, goodsId, String.valueOf(HawkTime.getMillisecond()), (int)(HawkTime.DAY_MILLI_SECONDS/1000));
		}
		
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
		int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
		// 修改玩家数据的逻辑，还是抛到玩家所在业务线程中去处理
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
				GreatGiftEntity entity = opEntity.get();
				entity.getOutBagList().add(goodsId);
				entity.notifyUpdate();
				
				List<GreatGiftBagCfg> cfgList = GreatGiftBagCfg.getSortedCfgList(player.getEntity().getPlatform());
				GreatGiftBagCfg configLastCfg = cfgList.get(cfgList.size() -1);
				if (configLastCfg.getGiftId().equals(goodsId)) {
					entity.setFinishTime(HawkTime.getMillisecond());
				}
				return null;
			}
		}, threadIndex);
		
		otherActivityHandler(player, goodsId, request);
		// 购买成功
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
	}
	
	/**
	 * 游戏外充值触发其它活动累计
	 * @param player
	 * @param goodsId
	 * @param request
	 */
	private void otherActivityHandler(Player player, String goodsId, JSONObject request) {
		// 触发玫瑰赠礼和点券夺宝的充值累计增加
//		PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsId);
//		int diamonds = cfg.getPayRMB() / 10;
//		ActivityManager.getInstance().postEvent(new IDIPGmRechargeEvent(player.getId(), diamonds));
//		ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), diamonds/10));
//		String redisKey = NotifyGmRechageHandler.RECHARGE_REDIS_KEY + ":" + player.getId();
//		RedisProxy.getInstance().getRedisSession().hIncrBy(redisKey, String.valueOf(HawkTime.getYyyyMMddIntVal()), diamonds, (int)(HawkTime.DAY_MILLI_SECONDS/1000));
		
		// 记录tlog打点日志
		LogUtil.logIdipSensitivity(player, request, Integer.parseInt(goodsId), 0);
	}
	
}


