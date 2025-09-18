package com.hawk.game.service.xhjzWar;

public enum XHJZWarStateEnum {
    PEACE(1, new XHJZWarPeaceState()),
    SIGNUP(2,new XHJZWarSignupState()),
    MATCH_WAIT(3, new XHJZWarMatchWaitState()),
    MATCH(4, new XHJZWarMatchState()),
    MATCH_END(5, new XHJZWarMatchEndState()),
    BATTLE(6, new XHJZWarBattleState()),
    FINISH(7, new XHJZWarFinishState()),

    BATTLE_WAIT(601, new XHJZWarBattleWaitState()),
    BATTLE_OPEN(602, new XHJZWarBattleOpenState()),
    BATTLE_END_WAIT(603, new XHJZWarBattleEndWaitState()),
    BATTLE_END(604, new XHJZWarBattleEndState()),
    ;

    private int index;
    private IXHJZWarState stateLogic;
    private IXHJZWarBattleState battleStateLogic;


    XHJZWarStateEnum(int index, IXHJZWarState stateLogic){
        this.index = index;
        this.stateLogic = stateLogic;
    }

    XHJZWarStateEnum(int index, IXHJZWarBattleState battleStateLogic){
        this.index = index;
        this.battleStateLogic = battleStateLogic;
    }

    public int getIndex() {
        return index;
    }

    public IXHJZWarState getStateLogic() {
        return stateLogic;
    }

    public IXHJZWarBattleState getBattleStateLogic() {
        return battleStateLogic;
    }

    public static XHJZWarStateEnum valueOf(int index){
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
