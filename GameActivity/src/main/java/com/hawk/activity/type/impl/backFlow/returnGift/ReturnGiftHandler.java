package com.hawk.activity.type.impl.backFlow.returnGift;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyMsg;
import com.hawk.game.protocol.HP;

/***
 * 老玩家回归 C->S
 * @author yang.rao
 *
 */
public class ReturnGiftHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.RETURN_GIFT_C_VALUE)
	public void comeBackPlayerDiscountBuy(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.RETURN_GIFT_VALUE);
		if (!optional.isPresent()){
			return;
		}
		ReturnGiftActivity activity = (ReturnGiftActivity)optional.get();
		ComeBackPlayerBuyMsg proto = protocol.parseProtocol(ComeBackPlayerBuyMsg.getDefaultInstance());
		Result<?> result = activity.onPlayerBuyChest(proto.getBuyId(), proto.getNum(), playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
}
