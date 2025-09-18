package com.hawk.activity.type.impl.luckyBox;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.LuckBoxItemExchangeReq;
import com.hawk.game.protocol.Activity.LuckBoxItemExchangeTipReq;
import com.hawk.game.protocol.Activity.LuckBoxNeedItemBuyReq;
import com.hawk.game.protocol.Activity.LuckBoxRadnomRewardReq;
import com.hawk.game.protocol.Activity.LuckBoxSelectRewardReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class LuckyBoxHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code2.LUCK_BOX_INFO_REQ_VALUE)
	public void luckBoxInfo(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}

	@ProtocolHandler(code = HP.code2.LUCK_BOX_RANDOM_REQ_VALUE)
	public void luckBoxRandom(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		LuckBoxRadnomRewardReq req = hawkProtocol.parseProtocol(LuckBoxRadnomRewardReq.getDefaultInstance());
		int times = req.getCount();
		activity.luckyBoxRandom(playerId, times, hawkProtocol.getType());
	}

	@ProtocolHandler(code = HP.code2.LUCK_BOX_REWARD_SELECT_REQ_VALUE)
	public void luckBoxSelectReward(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		LuckBoxSelectRewardReq req = hawkProtocol.parseProtocol(LuckBoxSelectRewardReq.getDefaultInstance());
		int cellId = req.getCellId();
		int rewardId = req.getRewardId();
		activity.selectReward(playerId, cellId, rewardId, hawkProtocol.getType());
	}

	@ProtocolHandler(code = HP.code2.LUCK_BOX_ITEM_EXCHANGE_TIP_REQ_VALUE)
	public void luckBoxChangeTips(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		LuckBoxItemExchangeTipReq req = hawkProtocol.parseProtocol(LuckBoxItemExchangeTipReq.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		activity.changeExchangeTips(playerId, id, tip);
	}

	@ProtocolHandler(code = HP.code2.LUCK_BOX_ITEM_EXCHANGE_REQ_VALUE)
	public void luckBoxChangeItems(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		LuckBoxItemExchangeReq req = hawkProtocol.parseProtocol(LuckBoxItemExchangeReq.getDefaultInstance());
		int exchangeId = req.getId();
		int exchangeCount = req.getNum();
		activity.itemExchange(playerId, exchangeId, exchangeCount, hawkProtocol.getType());
	}

	@ProtocolHandler(code = HP.code2.LUCK_BOX_NEED_ITEM_BUY_REQ_VALUE)
	public void luckBoxBuyNeedItems(HawkProtocol hawkProtocol, String playerId){
		LuckyBoxActivity activity = this.getActivity(ActivityType.LUCKY_BOX_ACTIVITY);
		if(activity == null){
			return;
		}
		LuckBoxNeedItemBuyReq req = hawkProtocol.parseProtocol(LuckBoxNeedItemBuyReq.getDefaultInstance());
		int count = req.getNum();
		activity.buyNeedItem(playerId, count, hawkProtocol.getType());
	}
}