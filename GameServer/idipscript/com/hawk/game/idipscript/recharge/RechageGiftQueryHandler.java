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
import com.hawk.activity.type.impl.directGift.entity.DirectGiftEntity;
import com.hawk.activity.type.impl.greatGift.GreatGiftActivity;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftBagCfg;
import com.hawk.activity.type.impl.greatGift.entity.GreatGiftEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.recharge.RechargeType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 游戏内直购礼包是否已购买情况查询 -- 10282150
 *
 * localhost:8081/idip/4449
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4449")
public class RechageGiftQueryHandler extends IdipScriptHandler {
	
	public static final int GOODS_ID_INVLIAD     = -1;  // 商品id错误，不存在对应的商品配置
	public static final int GOODS_ID_UNSUPPORT   = -2;  // 当前不支持这个商品id相关活动
	public static final int PLATFORM_NOT_MATCH   = -3;  // 商品id渠道和玩家角色平台不匹配
	public static final int ACTIVITY_NOT_OPEN    = -4;  // 活动未开启
	public static final int GOODS_STAGE_ERROR    = -5;  // 非玩家购买的最新一档，不支持回滚
	public static final int INNER_ROLLBACK_ERROR = -6;  // 游戏内购买的，不支持回滚
	public static final int GOODS_COND_ERROR     = -7;  // 商品购买条件不满足
	public static final int GOODS_BOUGHT         = -8;  // 已购买过此商品，不能重复购买
	public static final int RECHARGE_UNCOMPLETE  = -9;  // 此礼包上一次发起的购买还未完成，不能重复发起购买
	public static final int CROSS_UNSUPPORT      = -10; // 跨服状态下不支持，需要回到原服才能购买
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		if (player.isCsPlayer()) {
			fillResult(result, CROSS_UNSUPPORT, "跨服状态下不支持，需要回到原服才能购买");
			return result;
		}
		
		int goodsId = request.getJSONObject("body").getIntValue("GoodsId");
		String goodsIdKey = String.valueOf(goodsId);
		PayGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsIdKey);
		if (giftCfg == null) {
			fillResult(result, GOODS_ID_INVLIAD, "商品id错误，不存在对应的商品配置");
			return result;
		}
		
		switch (giftCfg.getGiftType()) {
		case RechargeType.GREAT_GIFT:
			return greatGiftQuery(player, giftCfg.getId(), result);
		case RechargeType.DIRECT_GIFT_ACTIVITY:
			return directGift0104Query(player, giftCfg.getId(), result);
		default:
			break;
		}
		
		fillResult(result, GOODS_ID_UNSUPPORT, "当前不支持这个商品id相关活动");
		return result;
	}
	
	/**
	 * 填充返回参数
	 * @param result
	 */
	private void fillResult(IdipResult result, int errorCode, String msg) {
		result.getBody().put("Result", errorCode);
		result.getBody().put("RetMsg", msg);
		result.getBody().put("BuyTimes", 0);
		result.getBody().put("LimitedTimes", 0);
		result.getBody().put("LimitedAvailable", 0);
	}
	
	/**
	 * 20240104版本的直购礼包
	 * @param player
	 * @param goodsId
	 * @param result
	 * @return
	 */
	private IdipResult directGift0104Query(Player player, String goodsId, IdipResult result) {
		DirectGiftCfg curCfg = DirectGiftCfg.getConfigBuyGoodsId(goodsId);
		if(curCfg == null || !goodsId.equals(curCfg.getPayGiftIdByChannel(player.getEntity().getPlatform()))){
			fillResult(result, PLATFORM_NOT_MATCH, "商品id渠道和玩家角色平台不匹配");
			return result;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DIRECT_GIFT_VALUE);
		DirectGiftActivity activity = (DirectGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			fillResult(result, ACTIVITY_NOT_OPEN, "活动未开启");
			return result;
		}
		
		Optional<DirectGiftEntity> optional = activity.getPlayerDataEntity(player.getId());
        DirectGiftEntity entity = optional.get();
        int buyTimes = entity.getBuyTimes(curCfg.getId());
        int limit = curCfg.getLimit();
        
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("BuyTimes", buyTimes);  // 已购买次数
		result.getBody().put("LimitedTimes", limit);  // 当前限购次数
		result.getBody().put("LimitedAvailable", buyTimes >= limit ? 0 : 1);  // 当前是否可购买
		return result;
	}
	
	/**
	 * 超值好礼
	 * @param result
	 * @return
	 */
	private IdipResult greatGiftQuery(Player player, String goodsId, IdipResult result) {
		GreatGiftBagCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, goodsId);
		if(!curCfg.getChannelType().equals(player.getEntity().getPlatform())){
			fillResult(result, PLATFORM_NOT_MATCH, "商品id渠道和玩家角色平台不匹配");
			return result;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GREAT_GIFT_VALUE);
		GreatGiftActivity activity = (GreatGiftActivity) opActivity.get();
		if(!activity.isOpening(player.getId())){ 
			fillResult(result, ACTIVITY_NOT_OPEN, "活动未开启");
			return result;
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("LimitedTimes", 1);  // 限购次数
		
		// 玩家在线，或者还没有跨天
		if (player.isActiveOnline() || HawkTime.isSameDay(HawkTime.getMillisecond(), player.getLogoutTime())) {
			assembleResultData(activity, player.getId(), curCfg, result);
		} else {
			// 还需要判断redis中记录的已购买的
			Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			GreatGiftEntity entity = opEntity.get();
			if(!activity.allBuyBeforeDay(entity)) {
				assembleResultData(activity, player.getId(), curCfg, result);
			} else {
				crossDayAssemble(activity, player, goodsId, result);
			}
		}
		
		return result;
	}
	
	/**
	 * 组装返回数据（不满足跨天重置的情况）
	 * @param activity
	 * @param playerId
	 * @param curCfg
	 * @param result
	 */
	private void assembleResultData(GreatGiftActivity activity, String playerId, GreatGiftBagCfg curCfg, IdipResult result) {
		int canBuyStage = activity.getCanBuyStage(playerId);
		if (curCfg.getGiftStage() < canBuyStage) {
			result.getBody().put("BuyTimes", 1);  // 已经买过了
			result.getBody().put("LimitedAvailable", 0);  // 不可购买
		} else if (canBuyStage > 0 && curCfg.getGiftStage() > canBuyStage) {
			result.getBody().put("BuyTimes", 0);
			result.getBody().put("LimitedAvailable", 0);  // 还不可购买
		} else if (curCfg.getGiftStage() == canBuyStage) {
			Set<String> goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(playerId);
			int available = goodsIds.contains(curCfg.getGiftId()) ? 0 : 1;
			result.getBody().put("BuyTimes", 0);  // 当前可购买
			result.getBody().put("LimitedAvailable", available);  // 1是可购买，0不可购买
		} else {
			result.getBody().put("BuyTimes", 1);  // 特殊情况（都买完了）
			result.getBody().put("LimitedAvailable", 0);  // 还不可购买
		}
	}
	
	/**
	 * 满足跨天重置的情况下对缓存数据进行判断
	 * @param activity
	 * @param player
	 * @param goodsId
	 * @param result
	 */
	private void crossDayAssemble(GreatGiftActivity activity, Player player, String goodsId, IdipResult result) {
		Optional<GreatGiftEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		GreatGiftEntity entity = opEntity.get();
		if (HawkTime.isSameDay(entity.getFinishTime(), HawkTime.getMillisecond())) {
			result.getBody().put("BuyTimes", 1);  // 已经买过了
			result.getBody().put("LimitedAvailable", 0);  // 不可购买
			return;
		}
		
		String key = RechageGiftCompleteHandler.getRedisKey(activity.getActivityId(), player.getId());
		Map<String, String> giftIdMap = ActivityGlobalRedis.getInstance().hgetAll(key);
		if (giftIdMap.isEmpty()) {
			List<GreatGiftBagCfg> list = GreatGiftBagCfg.getSortedCfgList(player.getEntity().getPlatform());
			if (list.get(0).getGiftId().equals(goodsId)) {
				result.getBody().put("BuyTimes", 0);  // 当前可购买
				result.getBody().put("LimitedAvailable", 1);  // 1是可购买，0不可购买
			} else {
				result.getBody().put("BuyTimes", 0);
				result.getBody().put("LimitedAvailable", 0);  // 还不可购买
			}
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
		
		List<GreatGiftBagCfg> cfgList = GreatGiftBagCfg.getSortedCfgList(player.getEntity().getPlatform());
		GreatGiftBagCfg configLastCfg = cfgList.get(cfgList.size() -1);
		String boughtLastGoodsId = bagIdList.get(bagIdList.size() -1);
		// 全都买过了
		if (configLastCfg.getGiftId().equals(boughtLastGoodsId)) {
			result.getBody().put("BuyTimes", 1);  // 已经买过了
			result.getBody().put("LimitedAvailable", 0);  // 不可购买
			return;
		} 
		
		GreatGiftBagCfg boughtLastGoodsCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, boughtLastGoodsId);
		int index = cfgList.indexOf(boughtLastGoodsCfg);
		GreatGiftBagCfg nextCfg = cfgList.get(index + 1);
		if (nextCfg.getGiftId().equals(goodsId)) {
			result.getBody().put("BuyTimes", 0);  // 当前可购买
			result.getBody().put("LimitedAvailable", 1);  // 1是可购买，0不可购买
		} else if (bagIdList.contains(goodsId)) {
			result.getBody().put("BuyTimes", 1);  // 已经买过了
			result.getBody().put("LimitedAvailable", 0);  // 不可购买
		} else {
			result.getBody().put("BuyTimes", 0);
			result.getBody().put("LimitedAvailable", 0);  // 还不可购买
		}
	}
	
}


