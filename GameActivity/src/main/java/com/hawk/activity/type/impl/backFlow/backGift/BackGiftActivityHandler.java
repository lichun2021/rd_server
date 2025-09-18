package com.hawk.activity.type.impl.backFlow.backGift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 回归大礼消息处理
 * 
 * @author che
 *
 */
public class BackGiftActivityHandler extends ActivityProtocolHandler {
	
	
	/**
	 * 活动信息
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BACK_GIFT_INFO_REQ_VALUE)
	public void getBackGiftInfo(HawkProtocol hawkProtocol, String playerId){
		BackGiftActivity activity = this.getActivity(ActivityType.BACK_GIFT);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 抽奖
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BACK_GIFT_LOTTERY_REQ_VALUE)
	public void backGiftLottery(HawkProtocol hawkProtocol, String playerId){
		BackGiftActivity activity = this.getActivity(ActivityType.BACK_GIFT);
		if(activity == null){
			return;
		}
		activity.lottery(playerId);
	}
	
	/**
	 * 刷新
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BACK_GIFT_REFRESH_REQ_VALUE)
	public void achiveChronoDoorFreeAward(HawkProtocol hawkProtocol, String playerId){
		BackGiftActivity activity = this.getActivity(ActivityType.BACK_GIFT);
		if(activity == null){
			return;
		}
		activity.giftRefresh(playerId);
	}
	
	
	
}