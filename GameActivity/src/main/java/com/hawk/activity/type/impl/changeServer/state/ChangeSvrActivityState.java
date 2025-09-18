package com.hawk.activity.type.impl.changeServer.state;

import com.hawk.game.protocol.Activity;

public enum ChangeSvrActivityState {
    INIT(0),
    RANK(1),
    APPLY(2),
    SHOW(3),
    CHANGE(4),
    ;


    private int index;

    ChangeSvrActivityState(int index){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Activity.ChangeServerActivityState getClientState(){
        switch (this){
            case INIT:return Activity.ChangeServerActivityState.CHANGE_SVR_RANK;
            case RANK:return Activity.ChangeServerActivityState.CHANGE_SVR_RANK;
            case APPLY:return Activity.ChangeServerActivityState.CHANGE_SVR_APPLY;
            case SHOW:return Activity.ChangeServerActivityState.CHANGE_SVR_SHOW;
            case CHANGE:return Activity.ChangeServerActivityState.CHANGE_SVR_CHANGE;
            default:return Activity.ChangeServerActivityState.CHANGE_SVR_RANK;
        }
    }

    public static ChangeSvrActivityState getStateByValue(int value){
        switch (value){
            case 0:return INIT;
            case 1:return RANK;
            case 2:return APPLY;
            case 3:return SHOW;
            case 4:return CHANGE;
            default:return INIT;
        }
    }
}
