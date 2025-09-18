package com.hawk.activity.type.impl.honourHeroReturn;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnExchangeReq;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnLotteryReq;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnTipsReq;
import com.hawk.game.protocol.HP;

/**
 * 荣耀英雄回归
 *
 * @author richard
 */
public class HonourHeroReturnHandler extends ActivityProtocolHandler {
    /**
     * 抽奖协议Handler
     */
    @ProtocolHandler(code = HP.code2.HONOUR_HERO_RETURN_LOTTERY_REQ_VALUE)
    public void lottery(HawkProtocol hawkProtocol, String playerId) {
        HonourHeroReturnActivity activity = this.getActivity(ActivityType.HONOUR_HERO_RETURN_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBHonourHeroReturnLotteryReq req = hawkProtocol.parseProtocol(
                PBHonourHeroReturnLotteryReq.getDefaultInstance());
        activity.lottery(playerId, req.getType());
    }

    @ProtocolHandler(code = HP.code2.HONOUR_HERO_RETURN_TIP_REQ_VALUE)
    public void updateCare(HawkProtocol hawkProtocol, String playerId) {
        HonourHeroReturnActivity activity = this.getActivity(ActivityType.HONOUR_HERO_RETURN_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBHonourHeroReturnTipsReq req = hawkProtocol.parseProtocol(
                PBHonourHeroReturnTipsReq.getDefaultInstance());

        if(req.getTipsCount() <= 0){
            return;
        }

        activity.updateActivityTips(playerId, req.getTipsList());
    }

    /**
     * 兑换协议Handler
     *
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HONOUR_HERO_RETURN_EXCHANGE_REQ_VALUE)
    public void itemExchange(HawkProtocol hawkProtocol, String playerId) {
        HonourHeroReturnActivity activity = this.getActivity(ActivityType.HONOUR_HERO_RETURN_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBHonourHeroReturnExchangeReq req = hawkProtocol.parseProtocol(
                PBHonourHeroReturnExchangeReq.getDefaultInstance());
        activity.itemExchange(playerId, req.getExchangeId(), req.getNum());
    }

    /**
     * 抽奖页选择协议Handler
     */
    @ProtocolHandler(code = HP.code2.HONOUR_HERO_RETURN_LOTTERY_PAGE_REQ_VALUE)
    public void lotteryPageSelect(HawkProtocol hawkProtocol, String playerId) {
        HonourHeroReturnActivity activity = this.getActivity(ActivityType.HONOUR_HERO_RETURN_ACTIVITY);
        if (activity == null) {
            return;
        }
        Activity.PBHonourHeroReturnLotteryPageReq req = hawkProtocol.parseProtocol(
                Activity.PBHonourHeroReturnLotteryPageReq.getDefaultInstance());
        activity.onSelectLotteryPage(playerId, req.getPageId());
    }
}