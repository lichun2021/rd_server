package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class CommonActivityLoginEvent extends ActivityEvent {
    //活动id
    private int activityId;

    public CommonActivityLoginEvent(){
        super(null);
    }

    public CommonActivityLoginEvent(String playerId, int activityId){
        super(playerId, true);
        this.activityId = activityId;
    }

    public int getActivityId() {
        return activityId;
    }
}
