package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GuildBackBoxAddEvent extends ActivityEvent {
    public GuildBackBoxAddEvent(){
        super(null);
    }
    public GuildBackBoxAddEvent(String playerId){
        super(playerId, true);
    }
}
