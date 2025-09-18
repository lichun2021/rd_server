package com.hawk.activity.type.impl.chronoGift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.AchieveChronoGiftFreeAwardsReq;
import com.hawk.game.protocol.Activity.ChronoGiftKeyBuyReq;
import com.hawk.game.protocol.Activity.ChronoGiftUnlockReq;
import com.hawk.game.protocol.Activity.ConfirmBuyAwardsReq;
import com.hawk.game.protocol.HP;

/**
 * 时空豪礼消息处理
 * 
 * @author che
 *
 */
public class ChronoGiftActivityHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.CHRONO_GIFT_KEY_BUY_REQ_VALUE)
	public void ChronoGiftKeyBuy(HawkProtocol hawkProtocol, String playerId){
		ChronoGiftActivity activity = this.getActivity(ActivityType.CHRONO_GIFT);
		if(activity == null){
			return;
		}
		ChronoGiftKeyBuyReq req = hawkProtocol.parseProtocol(ChronoGiftKeyBuyReq.getDefaultInstance());
		int buyNum = req.getNum();
		activity.buyChronoGiftKey(playerId, buyNum,hawkProtocol.getType());
		
	}
	
	
	/**
	 * 打开时空之门
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CHRONO_GIFT_UNLOCK_REQ_VALUE)
	public void openChronoGift(HawkProtocol hawkProtocol, String playerId){
		ChronoGiftActivity activity = this.getActivity(ActivityType.CHRONO_GIFT);
		if(activity == null){
			return;
		}
		ChronoGiftUnlockReq req = hawkProtocol.parseProtocol(ChronoGiftUnlockReq.getDefaultInstance());
		int giftId = req.getChronoGiftId();
		activity.openChronoGift(playerId, giftId);
		
	}
	
	/**
	 * 获取时空之门免费礼包
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CHRONO_GIFT_FREE_AWARD_ACHIEVE_REQ_VALUE)
	public void achiveChronoDoorFreeAward(HawkProtocol hawkProtocol, String playerId){
		
		ChronoGiftActivity activity = this.getActivity(ActivityType.CHRONO_GIFT);
		if(activity == null){
			return;
		}
		AchieveChronoGiftFreeAwardsReq req = hawkProtocol.parseProtocol(AchieveChronoGiftFreeAwardsReq.getDefaultInstance());
		int giftId = req.getGiftId();
		activity.getChronoGiftFreeReward(playerId, giftId);
		
	}
	
	
	
	/**
	 * 选择奖励请求
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CHRONO_GIFT_BUY_AWARD_CONFIRM_REQ_VALUE)
	public void onSelectCustomReward(HawkProtocol hawkProtocol, String playerId) {
		ChronoGiftActivity activity = this.getActivity(ActivityType.CHRONO_GIFT);
		if(activity == null){
			return;
		}
		ConfirmBuyAwardsReq req = hawkProtocol.parseProtocol(ConfirmBuyAwardsReq.getDefaultInstance());
		activity.confirmGiftBuyRewards(playerId, req.getGiftId(), req.getBuyAwardsList(),hawkProtocol.getType());
		
	}
	
	
	
	
}