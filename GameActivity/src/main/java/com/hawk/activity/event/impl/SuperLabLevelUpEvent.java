package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SuperLabLevelUpEvent extends ActivityEvent {
    private int totalLevel;

    public SuperLabLevelUpEvent(){
        super(null);
    }

    public SuperLabLevelUpEvent(String playerId, int totalLevel){
        super(playerId, true);
        this.totalLevel = totalLevel;
    }

    public int getTotalLevel() {
        return totalLevel;
    }
}
