package com.hawk.game.service.xqhxWar.state;

public enum XQHXWarStateEnum {
    //主状态机使用
    PEACE(1),//和平阶段
    SIGNUP(2),//报名阶段
    MATCH_WAIT(3),//匹配等待
    MATCH(4),//匹配
    MATCH_END(5),//匹配结束
    BATTLE(6),//战斗
    FINISH(7),//结束展示

    //战斗状态机使用
    BATTLE_WAIT(601),//战斗等待
    BATTLE_OPEN(602),//战斗开始
    BATTLE_END_WAIT(603),//战斗结束等待
    BATTLE_END(604),//战斗结束
    ;

    //索引
    private int index;

    XQHXWarStateEnum(int index){
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    //通过索引查状态
    public static XQHXWarStateEnum valueOf(int index){
        switch (index){
            case 1:return PEACE;
            case 2:return SIGNUP;
            case 3:return MATCH_WAIT;
            case 4:return MATCH;
            case 5:return MATCH_END;
            case 6:return BATTLE;
            case 7:return FINISH;
            default:return PEACE;
        }
    }
}
