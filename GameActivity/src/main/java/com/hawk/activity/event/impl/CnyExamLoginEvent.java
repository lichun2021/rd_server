package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 新春试炼累计登录
 */
public class CnyExamLoginEvent extends ActivityEvent {
    /** 累计登录天数*/
    private int loginDays;

    public CnyExamLoginEvent() {
        super(null);
    }

    public CnyExamLoginEvent(String playerId, int loginDays) {
        super(playerId, true);
        this.loginDays = loginDays;
    }

    public int getLoginDays() {
        return loginDays;
    }
}
