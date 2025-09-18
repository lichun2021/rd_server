package com.hawk.activity.type.impl.monthcard;

import java.util.List;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MonthCard.MonthCardActiveReq;
import com.hawk.game.protocol.MonthCard.MonthCardCustomSelectReq;
import com.hawk.game.protocol.MonthCard.MonthCardExchangeReq;
import com.hawk.game.protocol.MonthCard.PBShopItemTipsReq;
import com.hawk.game.protocol.MonthCard.ReceiveMonthCardAwardReq;

/**
 * 月卡周卡活动网络消息接收句柄
 * 
 * @author lating
 *
 */
public class MonthCardActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 领取周卡月卡礼包奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECEIVE_MONTHCARD_AWARD_C_VALUE)
	public boolean onReceiveMonthCardAward(HawkProtocol protocol, String playerId) {
		ReceiveMonthCardAwardReq req = protocol.parseProtocol(ReceiveMonthCardAwardReq.getDefaultInstance());
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		int cardId = req.getCardId();
		Result<?> result = activity.receiveMonthCardAward(playerId, cardId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	/**
	 * 月卡状态检测
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CHECK_MONTHCARD_C_VALUE)
	public boolean onMonthCardStateCheck(HawkProtocol protocol, String playerId) {
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		activity.checkMonthCardState(playerId);
		return true;
	}
	
	/**
	 * 使用道具激活月卡
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MONTH_CARD_ACTIVE_REQ_VALUE)
	public boolean onMonthCardActive(HawkProtocol protocol, String playerId) {
		MonthCardActiveReq req = protocol.parseProtocol(MonthCardActiveReq.getDefaultInstance());
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		int itemId = req.getItemId();
		Result<?> result = activity.activeMonthCardByItem(playerId, itemId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	/**
	 *  免费月卡激活、提升
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MONTH_CARD_FREE_REQ_VALUE)
	public boolean onFreeCardActive(HawkProtocol protocol, String playerId) {
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		Result<?> result = activity.activeFreeCard(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	/**
	 * 定制月卡选择物品
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MONTH_CARD_SELECT_REQ_VALUE)
	public boolean onSelectCustomReward(HawkProtocol protocol, String playerId) {
		MonthCardCustomSelectReq req = protocol.parseProtocol(MonthCardCustomSelectReq.getDefaultInstance());
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		int cardId = req.getCardId();
		List<Integer> rewardIds = req.getRewardIdList();
		int result = activity.selectCustomReward(playerId, cardId, rewardIds);
		if (result != 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		return true;
	}
	
	/**
	 *  兑换商店兑换
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.MONTH_CARD_EXCHANGE_REQ_VALUE)
	public boolean onExchange(HawkProtocol protocol, String playerId) {
		MonthCardExchangeReq req = protocol.parseProtocol(MonthCardExchangeReq.getDefaultInstance());
		MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
		int exchangeId = req.getExchangeId();
		int count = req.getCount();
		int result = activity.exchange(playerId, exchangeId, count);
		if (result != 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		return true;
	}
	
	/**
     * 特权兑换商店兑换物品勾选变更请求
     */
    @ProtocolHandler(code = HP.code2.MONTH_CARD_TIP_REQ_VALUE)
    public void lottery(HawkProtocol hawkProtocol, String playerId) {
    	PBShopItemTipsReq req = hawkProtocol.parseProtocol(PBShopItemTipsReq.getDefaultInstance());
    	MonthCardActivity activity = getActivity(ActivityType.MONTHCARD_ACTIVITY);
        activity.updateShopItemTips(playerId, req.getTipsList());
    }
	
}
