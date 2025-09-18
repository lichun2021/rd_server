package com.hawk.activity.type.impl.plantFortress;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.OpenPlantFortressTicketReq;
import com.hawk.game.protocol.Activity.PlantFortressChoseBigRewardReq;
import com.hawk.game.protocol.Activity.PlantFortressDefaultBigRewardReq;
import com.hawk.game.protocol.HP;

/**
 * 登录基金活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class PlantFortressHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_INFO_REQ_VALUE)
	public boolean getPlantFortressInfo(HawkProtocol protocol, String playerId) {
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	
	/**
	 * 购买钥匙道具
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_BUY_KEY_REQ_VALUE)
	public boolean buyKeys(HawkProtocol protocol, String playerId) {
		OpenPlantFortressTicketReq req =  protocol.parseProtocol(OpenPlantFortressTicketReq.getDefaultInstance());
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
		int tid = req.getTicketId();
		activity.buyKeys(playerId, tid);
		return true;
	}
	
	/**
	 * 打开奖券
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.OPEN_PLANT_FORTRESS_TICKET_REQ_VALUE)
	public boolean openTicket(HawkProtocol protocol, String playerId) {
		OpenPlantFortressTicketReq req =  protocol.parseProtocol(OpenPlantFortressTicketReq.getDefaultInstance());
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_CHOSE_BIG_REWARD_REQ_VALUE)
	public boolean chooseBigReward(HawkProtocol protocol, String playerId) {
		PlantFortressChoseBigRewardReq req = protocol.parseProtocol(PlantFortressChoseBigRewardReq.getDefaultInstance());
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_ENTER_NEXT_STAGE_REQ_VALUE)
	public boolean enterNextStage(HawkProtocol protocol, String playerId){
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
		activity.nextStage(playerId);
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_SHOW_REWARD_OVER_REQ_VALUE)
	public boolean showRewardOver(HawkProtocol protocol, String playerId){
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
		activity.showRewardOver(playerId);
		return true;
	}
	 
	@ProtocolHandler(code = HP.code.PLANT_FORTRESS_DEFAULT_BIG_REWARD_REQ_VALUE)
	public boolean choseDefaultBigReward(HawkProtocol protocol, String playerId){
		PlantFortressDefaultBigRewardReq req = protocol.parseProtocol(PlantFortressDefaultBigRewardReq.getDefaultInstance());
		PlantFortressActivity activity = getActivity(ActivityType.PLANT_FORTRESS_ACTIVITY);
		int poolId = req.getPoolId();
		int bigRewardId = req.getBigRewardId();
		int type = req.getType();
		activity.choseDefaultBigReward(playerId, poolId,bigRewardId, type);
		return true;
	}

}
