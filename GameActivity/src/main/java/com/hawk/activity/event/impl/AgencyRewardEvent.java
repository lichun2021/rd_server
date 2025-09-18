package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class AgencyRewardEvent extends ActivityEvent {
    private int agencyId;
    public AgencyRewardEvent(){ super(null);}
    public AgencyRewardEvent(String playerId, int agencyId) {
        super(playerId);
        this.agencyId = agencyId;
    }

    public int getAgencyId() {
        return agencyId;
    }
}
