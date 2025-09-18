package com.hawk.activity.type.impl.newStart;

public enum NewStartEnum {
    PLAYER_LEVEL(1),
    VIP_LEVEL(2),
    BASE_LEVEL(3),
    HERO_COUNT(4),
    EQUIP_TECH_LEVEL(5),
    JIJIA_LEVEL(6),
    ;

    private int type;

    NewStartEnum(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
