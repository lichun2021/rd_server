package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.service.tblyTeam.state.impl.battle.*;
import com.hawk.game.service.tblyTeam.state.impl.main.*;
import com.hawk.game.service.tblyTeam.state.impl.season.big.*;
import com.hawk.game.service.tblyTeam.state.impl.season.main.*;

public enum TBLYWarStateEnum {
    PEACE(1, new TBLYWarPeaceState()),
    SIGNUP(2,new TBLYWarSignupState()),
    MATCH_WAIT(3, new TBLYWarMatchWaitState()),
    MATCH(4, new TBLYWarMatchState()),
    MATCH_END(5, new TBLYWarMatchEndState()),
    BATTLE(6, new TBLYWarBattleState()),
    FINISH(7, new TBLYWarFinishState()),

    BATTLE_WAIT(601, new TBLYWarBattleWaitState()),
    BATTLE_OPEN(602, new TBLYWarBattleOpenState()),
    BATTLE_END_WAIT(603, new TBLYWarBattleEndWaitState()),
    BATTLE_END(604, new TBLYWarBattleEndState()),


    SEASON_BIG_NOT_OPEN(10001, new TBLYSeasonBigNotOpenState()),
    SEASON_BIG_SIGNUP(10002,new TBLYSeasonBigSignupState()),
    SEASON_BIG_GROUP_WAIT(10003, new TBLYSeasonBigGroupWaitState()),
    SEASON_BIG_GROUP(10004, new TBLYSeasonBigGroupState()),
    SEASON_BIG_KICK_OUT(10005, new TBLYSeasonBigKickOutState()),
    SEASON_BIG_FINAL(10006, new TBLYSeasonBigFinalState()),
    SEASON_BIG_END_SHOW(10007, new TBLYSeasonBigEndShowState()),

    SEASON_NOT_OPEN(11001, new TBLYSeasonNotOpenState()),
    SEASON_PEACE(11002,new TBLYSeasonPeaceState()),
    SEASON_MATCH(11003, new TBLYSeasonMatchState()),
    SEASON_WAR_MANGE(11004, new TBLYSeasonWarMangeState()),
    SEASON_WAR_WAIT(11005, new TBLYSeasonWarWaitState()),
    SEASON_WAR_OPEN(11006, new TBLYSeasonWarOpenState()),

    ;

    private int index;
    private ITBLYWarState stateLogic;
    private ITBLYWarBattleState battleStateLogic;
    private ITBLYSeasonBigState seasonBigStateLogic;
    private ITBLYSeasonState seasonStateLogic;

    TBLYWarStateEnum(int index, ITBLYWarState stateLogic){
        this.index = index;
        this.stateLogic = stateLogic;
    }

    TBLYWarStateEnum(int index, ITBLYWarBattleState battleStateLogic){
        this.index = index;
        this.battleStateLogic = battleStateLogic;
    }

    TBLYWarStateEnum(int index, ITBLYSeasonBigState seasonBigStateLogic){
        this.index = index;
        this.seasonBigStateLogic = seasonBigStateLogic;
    }

    TBLYWarStateEnum(int index, ITBLYSeasonState seasonStateLogic){
        this.index = index;
        this.seasonStateLogic = seasonStateLogic;
    }

    public int getIndex() {
        return index;
    }

    public ITBLYWarState getStateLogic() {
        return stateLogic;
    }

    public ITBLYWarBattleState getBattleStateLogic() {
        return battleStateLogic;
    }

    public ITBLYSeasonBigState getSeasonBigStateLogic() {
        return seasonBigStateLogic;
    }

    public ITBLYSeasonState getSeasonStateLogic() {
        return seasonStateLogic;
    }

    public static TBLYWarStateEnum valueOf(int index){
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

    public static TBLYWarStateEnum seasonBigValueOf(int index){
        switch (index){
            case 10001:return SEASON_BIG_NOT_OPEN;
            case 10002:return SEASON_BIG_SIGNUP;
            case 10003:return SEASON_BIG_GROUP_WAIT;
            case 10004:return SEASON_BIG_GROUP;
            case 10005:return SEASON_BIG_KICK_OUT;
            case 10006:return SEASON_BIG_FINAL;
            case 10007:return SEASON_BIG_END_SHOW;
            default:return SEASON_BIG_NOT_OPEN;
        }
    }

    public static TBLYWarStateEnum seasonValueOf(int index){
        switch (index){
            case 11001:return SEASON_NOT_OPEN;
            case 11002:return SEASON_PEACE;
            case 11003:return SEASON_MATCH;
            case 11004:return SEASON_WAR_MANGE;
            case 11005:return SEASON_WAR_WAIT;
            case 11006:return SEASON_WAR_OPEN;
            default:return SEASON_NOT_OPEN;
        }
    }
}
