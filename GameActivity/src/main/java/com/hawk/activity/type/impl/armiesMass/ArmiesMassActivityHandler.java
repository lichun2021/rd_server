package com.hawk.activity.type.impl.armiesMass;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BuyArmiesMassGiftReq;
import com.hawk.game.protocol.Activity.OpenArmiesMassSculptureReq;
import com.hawk.game.protocol.HP;

/**
 * 时空豪礼消息处理
 * 
 * @author che
 *
 */
public class ArmiesMassActivityHandler extends ActivityProtocolHandler {
	
	
	
	@ProtocolHandler(code = HP.code.ARMIES_MASS_PAGE_INFO_REQ_VALUE)
	public void armiesMassInfo(HawkProtocol hawkProtocol, String playerId){
		ArmiesMassActivity activity = this.getActivity(ActivityType.ARMIES_MASS_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	
	
	@ProtocolHandler(code = HP.code.ARMIES_MASS_OPEN_SCULPTURE_REQ_VALUE)
	public void openSculpture(HawkProtocol hawkProtocol, String playerId){
		ArmiesMassActivity activity = this.getActivity(ActivityType.ARMIES_MASS_ACTIVITY);
		if(activity == null){
			return;
		}
		OpenArmiesMassSculptureReq req = hawkProtocol.parseProtocol(OpenArmiesMassSculptureReq.getDefaultInstance());
		int index = req.getIndex();
		activity.openSculpture(playerId, index);
	}
	
	
	@ProtocolHandler(code = HP.code.ARMIES_MASS_BUY_GIFT_REQ_VALUE)
	public void buyGift(HawkProtocol hawkProtocol, String playerId){
		ArmiesMassActivity activity = this.getActivity(ActivityType.ARMIES_MASS_ACTIVITY);
		if(activity == null){
			return;
		}
		BuyArmiesMassGiftReq req = hawkProtocol.parseProtocol(BuyArmiesMassGiftReq.getDefaultInstance());
		int gid = req.getGiftId();
		activity.buyGiftPackage(playerId, gid,hawkProtocol.getType());
	}
	
	
	
	
	
}