package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
/**
 * 活动道具消耗事件
 *
 * @author che
 */
public class GrowUpBoostItemConsumeEvent extends ActivityEvent {
    /**
     * 奖励的物品ID
     */
    private int itemId;
    /**
     * 奖励的物品数量
     */
    private int number;
    
    private int action;
   
    
    public GrowUpBoostItemConsumeEvent(){ super(null);}
    
    public GrowUpBoostItemConsumeEvent(String playerId,int itemId,int number,int action) {
        super(playerId);
        this.itemId = itemId;
        this.number = number;
        this.action = action;
    }

   

    public int getItemId() {
        return itemId;
    }

    public int getNumber() {
        return number;
    }
    
    public int getAction() {
		return action;
	}

    
}
