package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 洪福天降激活事件
 */
public class HeavenBlessingActiveEvent extends ActivityEvent {
    private int vip;//vip等级
    private int money;//近期付费

	public HeavenBlessingActiveEvent(){ super(null);}
    public HeavenBlessingActiveEvent(String playerId, int vip, int money) {
        super(playerId, true);
        this.vip = vip;
        this.money = money;
    }

    public int getVip() {
        return vip;
    }

    public int getMoney() {
        return money;
    }
}
