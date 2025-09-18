package com.hawk.activity.type.impl.ordnanceFortress;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.OpenOrdnanceFortressTicketReq;
import com.hawk.game.protocol.Activity.OrdnanceFortressChoseBigRewardReq;
import com.hawk.game.protocol.Activity.OrdnanceFortressDefaultBigRewardReq;
import com.hawk.game.protocol.HP;

/**
 * 登录基金活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class OrdnanceFortressHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDNANCE_FORTRESS_INFO_REQ_VALUE)
	public boolean getOrdnanceFortressInfo(HawkProtocol protocol, String playerId) {
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	/**
	 * 打开奖券
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.OPEN_ORDNANCE_FORTRESS_TICKET_REQ_VALUE)
	public boolean openTicket(HawkProtocol protocol, String playerId) {
		OpenOrdnanceFortressTicketReq req =  protocol.parseProtocol(OpenOrdnanceFortressTicketReq.getDefaultInstance());
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		int tid = req.getTicketId();
		activity.openTicket(playerId, tid);
		return true;
	}
	
	/**
	 * 选择大奖
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDNANCE_FORTRESS_CHOSE_BIG_REWARD_REQ_VALUE)
	public boolean chooseBigReward(HawkProtocol protocol, String playerId) {
		OrdnanceFortressChoseBigRewardReq req = protocol.parseProtocol(OrdnanceFortressChoseBigRewardReq.getDefaultInstance());
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		int rewardId = req.getBigRewardId();
		activity.chooseBigReward(playerId, rewardId);
		return true;
	}
	
	/**
	 * 进入下一个阶段
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDNANCE_FORTRESS_ENTER_NEXT_STAGE_REQ_VALUE)
	public boolean enterNextStage(HawkProtocol protocol, String playerId){
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		activity.nextStage(playerId);
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code.ORDNANCE_FORTRESS_SHOW_REWARD_OVER_REQ_VALUE)
	public boolean showRewardOver(HawkProtocol protocol, String playerId){
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		activity.showRewardOver(playerId);
		return true;
	}
	 
	@ProtocolHandler(code = HP.code.ORDNANCE_FORTRESS_DEFAULT_BIG_REWARD_REQ_VALUE)
	public boolean choseDefaultBigReward(HawkProtocol protocol, String playerId){
		OrdnanceFortressDefaultBigRewardReq req = protocol.parseProtocol(OrdnanceFortressDefaultBigRewardReq.getDefaultInstance());
		OrdnanceFortressActivity activity = getActivity(ActivityType.ORDNANCE_FORTRESS_ACTIVITY);
		int poolId = req.getPoolId();
		int bigRewardId = req.getBigRewardId();
		int type = req.getType();
		activity.choseDefaultBigReward(playerId, poolId,bigRewardId, type);
		return true;
	}

}
