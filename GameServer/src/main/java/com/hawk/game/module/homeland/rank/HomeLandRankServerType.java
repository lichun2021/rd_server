package com.hawk.game.module.homeland.rank;

public enum HomeLandRankServerType {
    NULL(0, HomeLandLocalRankProvider.class),
    LOCAL(1, HomeLandLocalRankProvider.class),
    GUILD(2, HomeLandGuildRankProvider.class),
    CROSS(3, HomeLandCrossRankProvider.class),
    ;
    private final int type;
    private final Class<?> providerClass;

    HomeLandRankServerType(int type, Class<?> providerClass) {
        this.type = type;
        this.providerClass = providerClass;
    }

    public Class<?> getProviderClass() {
        return providerClass;
    }

    public int getType() {
        return type;
    }

    public static HomeLandRankServerType getValue(int type) {
        for (HomeLandRankServerType action : values()) {
            if (action.type == type) {
                return action;
            }
        }
        return HomeLandRankServerType.NULL;
    }
}
