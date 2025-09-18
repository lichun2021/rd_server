package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class RechargeAllRmbEvent extends ActivityEvent {
    private int rmb;
    public RechargeAllRmbEvent(){ super(null);}
    public RechargeAllRmbEvent(String playerId, int rmb) {
        super(playerId, true);
        this.rmb = rmb;
    }

    public int getRmb() {
        return rmb;
    }
    
    @Override
	public boolean isSkip() {
		return true;
	}
}
