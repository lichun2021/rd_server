package com.hawk.game.service.commonMatch.state;

public enum CMWStateEnum {
    CLOSE(1),
    SIGNUP(2),
    QUALIFIER(3),
    RANKING(4),
    END_SHOW(5),
    ;
    private int index;

    CMWStateEnum(int index){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static CMWStateEnum valueOf(int index){
        switch (index){
            case 1:return CLOSE;
            case 2:return SIGNUP;
            case 3:return QUALIFIER;
            case 4:return RANKING;
            case 5:return END_SHOW;
            default:return CLOSE;
        }
    }
}
