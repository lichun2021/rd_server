package com.hawk.activity.type.impl.mechacoreexplore;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AutoPickReq;
import com.hawk.game.protocol.Activity.CEBuyPickReq;
import com.hawk.game.protocol.Activity.CERemoveObstacleReq;
import com.hawk.game.protocol.Activity.CEShopExchangeReq;
import com.hawk.game.protocol.Activity.CETechOperReq;
import com.hawk.game.protocol.Activity.CEZoneBoxRewardReq;

/**
 *（机甲）核心勘探活动
 *
 * @author lating
 */
public class CoreExploreHandler extends ActivityProtocolHandler {
	
	/**
	 * 活动信息请求
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_ACTIVITY_INFO_C_VALUE)
	public void onPlayerReqInfo(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			activity.pushActivityInfo(playerId);
		}
	}

	/**
	 * 清除障碍
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_REMOVE_OBSTACLE_C_VALUE)
	public void onRemoveObstacle(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			CERemoveObstacleReq req = protocol.parseProtocol(CERemoveObstacleReq.getDefaultInstance());
			int result = activity.removeObstacle(playerId, req.getLine(), req.getColumn(), req.getType());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	/**
	 * 领取宝箱奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_BOX_REWARD_C_VALUE)
	public void onGetBoxReward(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			CEZoneBoxRewardReq req = protocol.parseProtocol(CEZoneBoxRewardReq.getDefaultInstance());
			int result = activity.receiveBoxAward(playerId, req.getLine(), req.getColumn(), req.getTimes());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	/**
	 * 购买矿镐
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_BUY_PICK_C_VALUE)
	public void buyPick(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			CEBuyPickReq req = protocol.parseProtocol(CEBuyPickReq.getDefaultInstance());
			int result = activity.buyPick(playerId, req.getCount());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	/**
	 * 科技提升
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_TECH_OPER_C_VALUE)
	public void onTechOper(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			CETechOperReq req = protocol.parseProtocol(CETechOperReq.getDefaultInstance());
			int result = activity.techOper(playerId, req.getTechId());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	/**
	 * 商店兑换
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_EXCHANGE_C_VALUE)
	public void onShopExchange(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			CEShopExchangeReq req = protocol.parseProtocol(CEShopExchangeReq.getDefaultInstance());
			int result = activity.shopExchange(playerId, req.getShopId(), req.getCount());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	/**
	 * 自动挖矿
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.CORE_EXPLORE_AUTO_PICK_C_VALUE)
	public void onAutoPick(HawkProtocol protocol, String playerId){
		CoreExploreActivity activity = getActivity(ActivityType.MECHA_CORE_EXPLORE);
		if(activity != null && activity.isOpening(playerId)){
			AutoPickReq req = protocol.parseProtocol(AutoPickReq.getDefaultInstance());
			int result = activity.autoPick(playerId, req.getAutoPick());
			if (result > 0) {
				sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
}
