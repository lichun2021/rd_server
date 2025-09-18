package com.hawk.activity.type.impl.honourMobilize;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HonorMobilizeChooseReq;
import com.hawk.game.protocol.Activity.HonorMobilizeLotteryReq;
import com.hawk.game.protocol.HP;

/**
 * 
 * @author che
 *
 */
public class HonourMobilizeHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.HONOR_MOBILIZE_CHOOSE_REQ_VALUE)
	public void chooseId(HawkProtocol hawkProtocol, String playerId){
		HonourMobilizeActivity activity = this.getActivity(ActivityType.HONOUR_MOBILIZE);
		if(activity == null){
			return;
		}
		HonorMobilizeChooseReq req = hawkProtocol.parseProtocol(HonorMobilizeChooseReq.getDefaultInstance());
		activity.chooseLotteryId(playerId, req.getChooseId());
	}
	
	
	
	@ProtocolHandler(code = HP.code2.HONOR_MOBILIZE_LOTTERY_REQ_VALUE)
	public void lottery(HawkProtocol hawkProtocol, String playerId){
		HonourMobilizeActivity activity = this.getActivity(ActivityType.HONOUR_MOBILIZE);
		if(activity == null){
			return;
		}
		HonorMobilizeLotteryReq req = hawkProtocol.parseProtocol(HonorMobilizeLotteryReq.getDefaultInstance());
		activity.lottery(playerId, req.getType(),hawkProtocol.getType());
	}
	
	
}