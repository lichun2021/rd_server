package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DYZZWinEvent extends ActivityEvent {
    private boolean win;
    public DYZZWinEvent(){ super(null);}
    public DYZZWinEvent(String playerId, boolean win) {
        super(playerId);
        this.win = win;
    }

    public boolean isWin() {
        return win;
    }
}
