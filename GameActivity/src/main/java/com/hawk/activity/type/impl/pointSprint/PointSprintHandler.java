package com.hawk.activity.type.impl.pointSprint;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintExchangeInfo;
import com.hawk.game.protocol.ActivityPointSprint.PointSprintGetPointReward;
import com.hawk.game.protocol.HP;

/**
 * 巅峰荣耀
 * @author Golden
 *
 */
public class PointSprintHandler extends ActivityProtocolHandler{
	/**
	 * 请求界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.POINT_SPRINT_PAGE_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId){
		PointSprintActivity activity = getActivity(ActivityType.POINT_SPRINT_345);
		activity.pushPageInfo(playerId);
	}
	
	/**
	 * 领取个人奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.POINT_SPRINT_OWN_POINT_REWARD_VALUE)
	public void ownAward(HawkProtocol protocol, String playerId){
		PointSprintGetPointReward req = protocol.parseProtocol(PointSprintGetPointReward.getDefaultInstance());
		PointSprintActivity activity = getActivity(ActivityType.POINT_SPRINT_345);
		activity.getOwnAward(playerId, req.getId());
	}
	
	/**
	 * 领取轮奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.POINT_SPRINT_ROUND_REWARD_VALUE)
	public void ownRoundAward(HawkProtocol protocol, String playerId){
		PointSprintActivity activity = getActivity(ActivityType.POINT_SPRINT_345);
		activity.getRoundAward(playerId);
	}
	
	  /**
     * 兑换
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.POINT_SPRINT_EXCHANGE_VALUE)
    public void exchange(HawkProtocol protocol, String playerId){
        //获得活动实例
    	PointSprintActivity activity = getActivity(ActivityType.POINT_SPRINT_345);
        //解析前端协议
    	PointSprintExchangeInfo req = protocol.parseProtocol(PointSprintExchangeInfo.getDefaultInstance());
        //执行兑换逻辑
        Result<?> result = activity.exchange(playerId, req.getExchangeId(), req.getNum());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

//    /**
//     * 兑换提醒
//     * @param protocol 前端协议
//     * @param playerId 玩家id
//     */
//    @ProtocolHandler(code = HP.code2.POINT_SPRINT_TIPS_VALUE)
//    public void exchangeTips(HawkProtocol protocol, String playerId){
//        //获得活动实例
//    	PointSprintActivity activity = getActivity(ActivityType.POINT_SPRINT_345);
//        //解析前端协议
//    	PointSprintExchangeTipsInfo req = protocol.parseProtocol(PointSprintExchangeTipsInfo.getDefaultInstance());
//        //勾选提醒协议
//        int tip = 0;
//        List<Integer> ids = new ArrayList<>();
//        for(PointSprintExchangeTips exchangeTips : req.getTipsList()){
//            tip = exchangeTips.getTip();
//            ids.add(exchangeTips.getId());
//        }
//        Result<?> result = activity.exchangeTips(playerId, ids, tip);
//        //如果没有成功执行，返回对应错误码
//        if(result.isFail()){
//            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
//        }
//    }
}
