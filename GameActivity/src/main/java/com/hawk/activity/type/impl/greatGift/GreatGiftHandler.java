package com.hawk.activity.type.impl.greatGift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.greatGiftInfo;
import com.hawk.game.protocol.Activity.greatGiftOperate;
import com.hawk.game.protocol.HP;

public class GreatGiftHandler extends ActivityProtocolHandler {
	
//	/***
//	 * 查看界面
//	 * @param protocol
//	 * @param playerId
//	 */
//	@ProtocolHandler(code = HP.code.GREAT_GIFT_INFO_VALUE)
//	public void reqGreatGiftInfo(HawkProtocol protocol, String playerId){
//		GreatGiftActivity activity = getActivity(ActivityType.GREAT_GIFT);
//		Result<?> result = activity.syncActivityInfo(playerId);
//		if(result.isFail()){
//			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
//			return;
//		}
//		greatGiftInfo.Builder build = (greatGiftInfo.Builder)result.getRetObj();
//		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GREAT_GIFT_INFO_VALUE, build));
//	}
	
//	/***
//	 * 购买礼包
//	 * @param protocol
//	 * @param playerId
//	 */
//	@ProtocolHandler(code = HP.code.GREAT_GIFT_BUY_BAG_VALUE)
//	public void greatGiftBuyBag(HawkProtocol protocol, String playerId){
//		GreatGiftActivity activity = getActivity(ActivityType.GREAT_GIFT);
//		RechargeBuyItemRequest req = protocol.parseProtocol(RechargeBuyItemRequest.getDefaultInstance());
//		String giftId = req.getGiftId();
//		int resultCode = -1;
//		if(req.hasResultCode()){
//			resultCode = req.getResultCode();
//		}
//		Result<?> result = activity.onPlayerBuyGift(playerId, giftId, protocol.getType(), resultCode);
//		if(result.isFail()){
//			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
//			return;
//		}
//		greatGiftInfo.Builder build = (greatGiftInfo.Builder)result.getRetObj();
//		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GREAT_GIFT_BUY_BAG_VALUE, build));
//	}
	
	/***
	 * 领取宝箱奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GREAT_GIFT_RECIEVE_CHEST_VALUE)
	public void greatGiftRecieveChest(HawkProtocol protocol, String playerId){
		GreatGiftActivity activity = getActivity(ActivityType.GREAT_GIFT);
		greatGiftOperate req = protocol.parseProtocol(greatGiftOperate.getDefaultInstance());
		int chestId = req.getId();
		Result<?> result = activity.onPlayerRecieveChest(playerId, chestId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		greatGiftInfo.Builder build = (greatGiftInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GREAT_GIFT_INFO_VALUE, build));
	}
}
