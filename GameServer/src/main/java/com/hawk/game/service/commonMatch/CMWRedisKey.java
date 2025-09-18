package com.hawk.game.service.commonMatch;

public class CMWRedisKey {
    public static final String CMW_STATE = "%s:STATE";
    public static final String CMW_LOCK = "%s:BIG_LOCK:%d";
    public static final String CMW_DATA = "%s:DATA:%d";
    public static final String CMW_PLAYER_DATA = "%s:PLAYER:DATA:%d";

    public static final String CMW_RANK = "%s:RANK:%d";
    public static final String CMW_JOIN = "%s:JOIN:%d";
    public static final String CMW_BATTLE_QUALIFIER_SELF = "%s:BATTLE:QUALIFIER:%d:%s";
    public static final String CMW_BATTLE_RANKING_SELF = "%s:BATTLE:RANKING:%d:%s";
    public static final String CMW_BATTLE_GROUP = "%s:BATTLE:%d:%d:%d";

    public static final String CMW_RANK_NEW = "%s:RANK:NEW:%d";
    public static final String CMW_JOIN_NEW = "%s:JOIN:NEW:%d";
    public static final String CMW_BATTLE_QUALIFIER_SELF_NEW = "%s:BATTLE:QUALIFIER:NEW:%d:%s";
    public static final String CMW_BATTLE_RANKING_SELF_NEW = "%s:BATTLE:RANKING:NEW:%d:%s";
    public static final String CMW_BATTLE_GROUP_NEW = "%s:BATTLE:NEW:%d:%d:%d";

}
