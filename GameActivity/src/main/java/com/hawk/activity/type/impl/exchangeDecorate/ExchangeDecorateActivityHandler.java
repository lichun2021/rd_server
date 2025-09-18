package com.hawk.activity.type.impl.exchangeDecorate;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DecorateExchangeLevelRewardReq;
import com.hawk.game.protocol.Activity.ExchangeCommonReq;
import com.hawk.game.protocol.HP;

/**
 * 免费装扮活动
 * 1.等级列表
 * 2.等级奖励领取
 * 3.等级解锁
 * 4.等级特惠
 * 5.各类兑换操作
 * 6.装扮列表
 * @author luke
 */
public class ExchangeDecorateActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 兑换列表
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE)
	public void onDecorateExchangeLevelList(HawkProtocol hawkProtocol, String playerId) {
		ExchangeDecorateActivity activity = this.getActivity(ActivityType.EXCHANGE_DECORATE_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 请求等级奖励
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DECORATE_EXCHANGE_LEVEL_REWARD_REQ_VALUE)
	public void onDecorateExchangeLevelReward(HawkProtocol hawkProtocol, String playerId) {
		DecorateExchangeLevelRewardReq req = hawkProtocol.parseProtocol(DecorateExchangeLevelRewardReq.getDefaultInstance());
		ExchangeDecorateActivity activity = this.getActivity(ActivityType.EXCHANGE_DECORATE_ACTIVITY);
		activity.exchangeLevelReward(playerId,req.getLevelId());
	}
	
	/**
	 * 限时等级列表
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DECORATE_EXCHANGE_LIMIT_TIME_REQ_VALUE)
	public void onDecorateExchangeLimitTime(HawkProtocol hawkProtocol, String playerId) {
		ExchangeDecorateActivity activity = this.getActivity(ActivityType.EXCHANGE_DECORATE_ACTIVITY);
		activity.exchangeLimitTimeList(playerId);
	}
	
	/**
	 * 领取通用奖励
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.EXCHANGE_COMMON_REQ_VALUE)
	public void onExchangeCommon(HawkProtocol hawkProtocol, String playerId) {
		ExchangeCommonReq req = hawkProtocol.parseProtocol(ExchangeCommonReq.getDefaultInstance());
		ExchangeDecorateActivity activity = this.getActivity(ActivityType.EXCHANGE_DECORATE_ACTIVITY);
		activity.exchangeCommonReward(playerId,req.getAct(),req.getParamsList());
	}	
	
	/**
	 * 装扮列表
	 */
	@ProtocolHandler(code = HP.code.DECORATE_EXCHANGE_LIST_REQ_VALUE)
	public void onDecorateExchangeList(HawkProtocol hawkProtocol, String playerId) {
		ExchangeDecorateActivity activity = this.getActivity(ActivityType.EXCHANGE_DECORATE_ACTIVITY);
		activity.exchangeList(playerId);
	}	
}