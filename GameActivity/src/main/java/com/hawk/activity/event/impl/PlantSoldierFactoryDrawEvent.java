package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PlantSoldierFactoryDrawEvent extends ActivityEvent {
    private int drawConsume;

    public PlantSoldierFactoryDrawEvent(){
        super(null);
    }

    public PlantSoldierFactoryDrawEvent(String playerId, int drawConsume) {
        super(playerId);
        this.drawConsume = drawConsume;
    }

    public int getDrawConsume() {
        return drawConsume;
    }
}
