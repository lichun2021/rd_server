package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;

import com.hawk.activity.event.impl.SeasonOrderAuthBuyEvent;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;

/**
 * 赛事活动战令活动进阶礼包
 */
public class SeasonActivityRecharge extends AbstractGiftRecharge {
    /**
     * 检查礼包是否可以购买
     * @param player 玩家数据
     * @param giftCfg 礼包配置
     * @param req 前端请求
     * @param protocol 前端请求协议号
     * @return 购买状态
     */
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        return orderAuthBuyCheck(player, giftCfg.getId());
    }

    /**
     * 购买成功逻辑
     * @param player 玩家数据
     * @param giftCfg 礼包配置
     * @param rechargeEntity 充值数据实体
     * @return 发奖状态
     */
    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        ActivityManager.getInstance().postEvent(new SeasonOrderAuthBuyEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    /**
     * 获得充值类型
     * @return 充值类型
     */
    @Override
    public int getGiftType() {
        return RechargeType.SEASON_ACTIVITY;
    }

    /**
     * 判断是否能购买
     * @param player 玩家数据
     * @param payGifrId 礼包id
     * @return 购买状态
     */
    private boolean orderAuthBuyCheck(Player player, String payGifrId) {
        //获得活动
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.SEASON_ACTIVITY_VALUE);
        if (!opActivity.isPresent()) {
            return false;
        }
        SeasonActivity activity = opActivity.get();
        //进行购买校验
        if(!activity.checkAuthBuy(player.getId(), payGifrId)){
            return false;
        }
        return true;
    }
}
