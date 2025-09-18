package com.hawk.game.service.xqhxWar;

public class XQHXWarResidKey {
    //状态机
    public static final String XQHX_WAR_STATE = "XQHX_WAR:STATE";
    //本服报名小队 参数1：期数，参数2：服务器id
    public static final String XQHX_WAR_SIGNUP_SERVER = "XQHX_WAR:SIGNUP:SERVER:%d:%s";
    //对应时间报名小队 参数1：期数，参数2：时间索引
    public static final String XQHX_WAR_SIGNUP_TIME = "XQHX_WAR:SIGNUP:TIME:%d:%d";
    //小队出战玩家 参数1：期数，参数2：小队id
    public static final String XQHX_WAR_SIGNUP_PLAYER = "XQHX_WAR:SIGNUP:PLAYER:%d:%s";
    //跨服联盟信息 参数1：联盟id
    public static final String XQHX_WAR_GUILD = "XQHX_WAR:GUILD:%d";
    //匹配逻辑锁 参数1：期数
    public static final String XQHX_WAR_MATCH = "XQHX_WAR:MATCH:%d";
    //房间数据 参数1：期数
    public static final String XQHX_WAR_ROOM = "XQHX_WAR:ROOM:%d";
    //服务器关联房间id，小队参与和战场相关 参数1：期数 参数2：服务器id
    public static final String XQHX_WAR_ROOM_SERVER = "XQHX_WAR:ROOM:SERVER:%d:%s";
    //小队进入人数 参数1：期数 参数2：小队id
    public static final String XQHX_WAR_TEAM_ENTER = "XQHX_WAR:TEAM_ENTER:%d:%s";
    //玩家分数 参数1：期数
    public static final String XQHX_WAR_PLAYER_SCORE = "XQHX_WAR:PLAYER_SCORE:%d";
    //历史记录 参数1：小队id
    public static final String XQHX_WAR_HISTORY = "XQHX_WAR:HISTORY:%s";
    //小队发奖标志 参数1：期数
    public static final String XQHX_WAR_TEAM_AWARD = "XQHX_WAR:TEAM_AWARD:%d";
    //玩家发奖标志 参数1：期数
    public static final String XQHX_WAR_PLAYER_AWARD = "XQHX_WAR:PLAYER_AWARD:%d";
}
