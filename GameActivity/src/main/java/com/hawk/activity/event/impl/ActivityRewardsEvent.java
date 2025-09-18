package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
/**
 * 活动奖励事件
 *
 * @author Richard
 */
public class ActivityRewardsEvent extends ActivityEvent {
    /**
     * 所属能量类型
     */
    private ShareGloryKVCfg.DonateItemType donateItemType;
    /**
     * 活动ID
     */
    private int activityId;
    /**
     * 物品类型
     */
    private int itemType;
    /**
     * 奖励的物品ID
     */
    private int itemId;
    /**
     * 奖励的物品数量
     */
    private int number;
    /**
     * 处理周年兑换专用CELEBRATION_SHOP_EXCHANGE_REWARD
     * 因为需要在荣耀共享活动逻辑里读取周年庆活动配置，所以在
     * 周年兑换活动逻辑里携带此信息
     */
    private int exchangeId;

    public ActivityRewardsEvent(){ super(null);}
    public ActivityRewardsEvent(String playerId, int activityId,
                                int itemId, int itemType,int number, ShareGloryKVCfg.DonateItemType type) {
        super(playerId);
        this.activityId = activityId;
        this.itemId = itemId;
        this.number = number;
        this.donateItemType = type;
        this.itemType = itemType;
        this.exchangeId = -1;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getNumber() {
        return number;
    }

    public ShareGloryKVCfg.DonateItemType getDonateItemType() {
        return donateItemType;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(int exchangeId) {
        this.exchangeId = exchangeId;
    }
}
