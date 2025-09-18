package com.hawk.activity.type.impl.backFlow.powerSend;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PowerSendMessageBackReq;
import com.hawk.game.protocol.Activity.PowerSendMessageInfoReq;
import com.hawk.game.protocol.Activity.PowerSendMessagePressedBackReq;
import com.hawk.game.protocol.HP;

/**
 * 体力赠送活动
 * 
 * @author che
 *
 */
public class PowerSendActivityHandler extends ActivityProtocolHandler {
	
	
	
	
	@ProtocolHandler(code = HP.code.POWER_SEND_MESSAGE_INFO_REQ_VALUE)
	public void getMessageInfo(HawkProtocol hawkProtocol, String playerId){
		PowerSendActivity activity = this.getActivity(ActivityType.POWER_SEND);
		if(activity == null){
			return;
		}
		
		PowerSendMessageInfoReq req = hawkProtocol.parseProtocol(PowerSendMessageInfoReq.getDefaultInstance()); 
		String messageId = req.getMessageId();
		activity.getMessageInfo(messageId,playerId);
	}
	
	
	@ProtocolHandler(code = HP.code.POWER_SEND_MESSAGE_PRESSED_BACK_REQ_VALUE)
	public void readSendMessage(HawkProtocol hawkProtocol, String playerId){
		PowerSendActivity activity = this.getActivity(ActivityType.POWER_SEND);
		if(activity == null){
			return;
		}
		PowerSendMessagePressedBackReq req = hawkProtocol.parseProtocol(PowerSendMessagePressedBackReq.getDefaultInstance()); 
		String messageId = req.getMessageId();
		activity.pressedSendPowerMessageBack(messageId, playerId);
	}
	

	@ProtocolHandler(code = HP.code.POWER_SEND_MESSAGE_BACK_REQ_VALUE)
	public void powerSendBack(HawkProtocol hawkProtocol, String playerId){
		PowerSendActivity activity = this.getActivity(ActivityType.POWER_SEND);
		if(activity == null){
			return;
		}
		PowerSendMessageBackReq req = hawkProtocol.parseProtocol(PowerSendMessageBackReq.getDefaultInstance()); 
		String messageId = req.getMessageId();
		activity.sendPowerBackAndAchieve(playerId, messageId);
	}
	
	
}