package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

public class TBLYWarBattleStateData {
    private int timeIndex;
    private TBLYWarStateEnum state = TBLYWarStateEnum.BATTLE_WAIT;
    private boolean isNext = false;

    public TBLYWarBattleStateData(int timeIndex){
        this.timeIndex = timeIndex;
    }

    @Override
    public String toString() {
        return "TBLYWarBattleStateData{" +
                "timeIndex=" + timeIndex +
                ", state=" + state +
                '}';
    }

    public void tick(){
        state.getBattleStateLogic().tick(timeIndex);
        if(isNext){
            try {
                next();
            }catch (Exception e) {
                HawkException.catchException(e);
            }
            isNext = false;
        }
    }

    public void next(){
        try {
            state.getBattleStateLogic().leave(timeIndex);
        }catch (Exception e){
            HawkException.catchException(e);
        }

        switch (state){
            case BATTLE_WAIT:{
                state = TBLYWarStateEnum.BATTLE_OPEN;
            }
            break;
            case BATTLE_OPEN:{
                state = TBLYWarStateEnum.BATTLE_END_WAIT;
            }
            break;
            case BATTLE_END_WAIT:{
                state = TBLYWarStateEnum.BATTLE_END;
            }
            break;
        }
        try {
            state.getBattleStateLogic().enter(timeIndex);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        HawkLog.logPrintln(toString());
        TBLYWarService.getInstance().syncAllPLayer();
    }

    public void toNext(){
        isNext = true;
    }

    public TBLYWarStateEnum getState() {
        return state;
    }

    public TWStateInfo.Builder getStateInfo(Player player){
        return state.getBattleStateLogic().getStateInfo(player, timeIndex);
    }
}
