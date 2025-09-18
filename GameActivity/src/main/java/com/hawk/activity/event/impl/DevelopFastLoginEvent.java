package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DevelopFastLoginEvent extends ActivityEvent {
    /** 累计登录天数*/
    private int loginDays;

    public DevelopFastLoginEvent() {
        super(null);
    }

    public DevelopFastLoginEvent(String playerId, int loginDays) {
        super(playerId, true);
        this.loginDays = loginDays;
    }

    public int getLoginDays() {
        return loginDays;
    }
}
