package com.hawk.game.module.homeland.rank;

public enum HomeLandRankType {
    NULL(0),
    PROSPERITY(1),
    LIKE(2),
    ;
    private int type;

    HomeLandRankType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static  HomeLandRankType getValue(int type) {
        for (HomeLandRankType action : values()) {
            if (action.type == type) {
                return action;
            }
        }
        return HomeLandRankType.NULL;
    }
}
