package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class BuyDirectGiftEvent extends ActivityEvent {
	
    private static final long serialVersionUID = 1L;
    
    private String goodsId;
    private int times;
    private boolean rewardDeliver;
    
    public BuyDirectGiftEvent(){ 
    	super(null);
    	this.rewardDeliver = true;
    	this.times = 1;
    }
    
    public BuyDirectGiftEvent(String playerId, String giftId) {
        super(playerId, true);
        this.goodsId = giftId;
        this.rewardDeliver = true;
        this.times = 1;
    }

    public String getGoodsId() {
        return goodsId;
    }

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public boolean isRewardDeliver() {
		return rewardDeliver;
	}

	public void setRewardDeliver(boolean rewardDeliver) {
		this.rewardDeliver = rewardDeliver;
	}
    
}
