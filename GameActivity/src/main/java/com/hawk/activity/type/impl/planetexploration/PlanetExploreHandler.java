package com.hawk.activity.type.impl.planetexploration;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PlanetExploreBuyItemReq;
import com.hawk.game.protocol.Activity.PlanetExploreReq;

/**
 * 星能探索活动
 * author:lating
 */
public class PlanetExploreHandler extends ActivityProtocolHandler {
	
	/**
	 * 星能探索活动信息请求
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANET_EXPLORE_INFO_REQ_VALUE)
	public void onPlayerReqInfo(HawkProtocol protocol, String playerId){
		PlanetExploreActivity activity = getActivity(ActivityType.PLANET_EXPLORE_347);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		activity.syncActivityInfo(playerId);
	}
	
	/**
	 * 购买探索道具
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANET_EXPLORE_BUY_REQ_VALUE)
	public void onBuyItemReq(HawkProtocol protocol, String playerId){
		PlanetExploreActivity activity = getActivity(ActivityType.PLANET_EXPLORE_347);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlanetExploreBuyItemReq req = protocol.parseProtocol(PlanetExploreBuyItemReq.getDefaultInstance());
		int result = activity.onExploreItemBuy(playerId, req.getCount());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 探索抽奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANET_EXPLORE_REQ_VALUE)
	public void onExploreReq(HawkProtocol protocol, String playerId){
		PlanetExploreActivity activity = getActivity(ActivityType.PLANET_EXPLORE_347);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		
		PlanetExploreReq req = protocol.parseProtocol(PlanetExploreReq.getDefaultInstance());
		int result = activity.onPlanetExplore(playerId, req.getTimesNum());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 能探索活动挖掘信息请求
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.ACTIVITY_PLANET_EXPLORE_COLLECT_INFO_REQ_VALUE)
	public void onCollectInfoReq(HawkProtocol protocol, String playerId){
		PlanetExploreActivity activity = getActivity(ActivityType.PLANET_EXPLORE_347);
		if(activity == null || !activity.isOpening(playerId)){
			return;
		}
		activity.syncCollectInfo(playerId);
	}
	
}
