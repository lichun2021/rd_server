package com.hawk.activity.type.impl.rechargeFund;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RFGetAwardReq;
import com.hawk.game.protocol.Activity.RFInvestReq;
import com.hawk.game.protocol.Activity.RFPageInfo;
import com.hawk.game.protocol.Activity.RFSelectAwardReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class RechargeFundHandler extends ActivityProtocolHandler {
	
	
	/**
	 * 获取界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECHARGE_FUND_GET_PAGE_INFO_REQ_VALUE)
	public void getPageInfo(HawkProtocol protocol, String playerId){
		RechargeFundActivity activity = getActivity(ActivityType.RECHARGE_FUND);
		if(null != activity && activity.isOpening(playerId)){
			RFPageInfo.Builder builder = activity.genActivityInfo(playerId);
			if(builder !=null){
				PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_FUND_GET_PAGE_INFO_RESP_VALUE, builder));
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	/**
	 * 设置自选奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECHARGE_FUND_SELECT_AWARD_REQ_VALUE)
	public void selectAward(HawkProtocol protocol, String playerId) {
		RechargeFundActivity activity = getActivity(ActivityType.RECHARGE_FUND);
		if (null != activity && activity.isOpening(playerId)) {
			RFSelectAwardReq req = protocol.parseProtocol(RFSelectAwardReq.getDefaultInstance());
			int result = activity.onDiySelect(playerId, req);
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				sendErrorAndBreak(playerId, HP.code.RECHARGE_FUND_SELECT_AWARD_RESP_VALUE, result);
			}
		}
	}
	
	/**
	 * 投资
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECHARGE_FUND_INVEST_REQ_VALUE)
	public void doInvest(HawkProtocol protocol, String playerId) {
		RechargeFundActivity activity = getActivity(ActivityType.RECHARGE_FUND);
		if (null != activity && activity.isOpening(playerId)) {
			RFInvestReq req = protocol.parseProtocol(RFInvestReq.getDefaultInstance());
			int result = activity.doInvest(playerId, req.getGiftId());
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				sendErrorAndBreak(playerId, HP.code.RECHARGE_FUND_INVEST_REQ_VALUE, result);
			}
		}
	}
	
	/**
	 * 领奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECHARGE_FUND_GET_AWARD_REQ_VALUE)
	public void getAward(HawkProtocol protocol, String playerId){
		RechargeFundActivity activity = getActivity(ActivityType.RECHARGE_FUND);
		if(null != activity && activity.isOpening(playerId)){
			RFGetAwardReq req = protocol.parseProtocol(RFGetAwardReq.getDefaultInstance());
			int result = activity.getReward(playerId, req.getAwardId());
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				sendErrorAndBreak(playerId, HP.code.RECHARGE_FUND_GET_AWARD_RESP_VALUE, result);
			}
		}
	}
	
}
