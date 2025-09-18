package com.hawk.activity.type.impl.virtualLaboratory;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.VirtualOpenCardReq;
import com.hawk.game.protocol.Activity.VirtualOpenCardResp;
import com.hawk.game.protocol.Activity.VirtualOpenCardResp.Builder;

/**
 * 武装技术（翻牌）
 * @author Winder
 */
public class VirtualLaboratoryHandler extends ActivityProtocolHandler {
	//挖幽灵秘宝
	@ProtocolHandler(code = HP.code.VIRTUAL_OPEN_CARD_REQ_VALUE)
	public void drewGhostSecret(HawkProtocol protocol, String playerId){
		VirtualOpenCardReq req = protocol.parseProtocol(VirtualOpenCardReq.getDefaultInstance());
		VirtualLaboratoryActivity activity = getActivity(ActivityType.VIRTUAL_LABORATORY_ACTIVITY);
		Result<?> result = activity.openCard(playerId, req.getCardIndex(), req.getCardTwoIndex());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		
		VirtualOpenCardResp.Builder builder = (Builder) result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.VIRTUAL_OPEN_CARD_RESP_VALUE, builder));
	}
	//重置次数
	@ProtocolHandler(code = HP.code.VIRTUAL_RESET_REQ_VALUE)
	public void resetDrewTimes(HawkProtocol protocol, String playerId){
		VirtualLaboratoryActivity activity = getActivity(ActivityType.VIRTUAL_LABORATORY_ACTIVITY);
		Result<?> result = activity.resetCard(playerId, HP.code.VIRTUAL_RESET_REQ_VALUE);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	//界面信息
	@ProtocolHandler(code = HP.code.VIRTUAL_CARD_INFO_REQ_VALUE)
	public void getPageInfo(HawkProtocol protocol, String playerId){
		VirtualLaboratoryActivity activity = getActivity(ActivityType.VIRTUAL_LABORATORY_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
}
