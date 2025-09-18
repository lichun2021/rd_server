package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

public class XHJZWarBattleStateData {
    private int timeIndex;
    private XHJZWarStateEnum state = XHJZWarStateEnum.BATTLE_WAIT;
    private boolean isNext = false;

    public XHJZWarBattleStateData(int timeIndex){
        this.timeIndex = timeIndex;
    }

    @Override
    public String toString() {
        return "XHJZWarBattleStateData{" +
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
                state = XHJZWarStateEnum.BATTLE_OPEN;
            }
            break;
            case BATTLE_OPEN:{
                state = XHJZWarStateEnum.BATTLE_END_WAIT;
            }
            break;
            case BATTLE_END_WAIT:{
                state = XHJZWarStateEnum.BATTLE_END;
            }
            break;
        }
        try {
            state.getBattleStateLogic().enter(timeIndex);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        HawkLog.logPrintln(toString());
        XHJZWarService.getInstance().syncAllPLayer();
    }

    public void toNext(){
        isNext = true;
    }

    public XHJZWarStateEnum getState() {
        return state;
    }

    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player){
        return state.getBattleStateLogic().getStateInfo(player, timeIndex);
    }
}
