package com.hawk.activity.type.impl.evolution;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.EvolutionExchangeReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 英雄进化之路活动网络消息接收句柄
 * 
 * @author lating
 *
 */
public class EvolutionActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EVOLUTION_ACTIVITY_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		EvolutionActivity activity = getActivity(ActivityType.EVOLUTION_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	/**
	 * 奖励兑换
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EVOLUTION_EXCHANGE_C_VALUE)
	public boolean onExchange(HawkProtocol protocol, String playerId) {
		EvolutionExchangeReq req =  protocol.parseProtocol(EvolutionExchangeReq.getDefaultInstance());
		
		EvolutionActivity activity = getActivity(ActivityType.EVOLUTION_ACTIVITY);
		int result = activity.onPoolExchange(playerId, req.getExchangeId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 领取等级奖励
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.EVOLUTION_LEVEL_AWARD_GET_C_VALUE)
	public boolean onGetLevelAward(HawkProtocol protocol, String playerId) {
		EvolutionActivity activity = getActivity(ActivityType.EVOLUTION_ACTIVITY);
		int code = activity.onRecPoolLevelAward(playerId);
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
}
