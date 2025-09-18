package com.hawk.game.service.xhjzWar;

import com.hawk.game.config.XHJZWarTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import java.util.concurrent.TimeUnit;

public class XHJZWarBattleOpenState implements IXHJZWarBattleState {
    @Override
    public void enter(int timeIndex) {
        XHJZWarService.getInstance().onBattleOpen(timeIndex);
    }

    @Override
    public void tick(int timeIndex) {
        HawkTuple2<Long, Long> battleTime = XHJZWarService.getInstance().getBattleTime(timeIndex);
        if(battleTime != null){
            long now = HawkTime.getMillisecond();
            if(now >= battleTime.second){
                XHJZWarService.getInstance().toBattleNext(timeIndex);
            }
        }
    }

    @Override
    public void leave(int timeIndex) {

    }

    @Override
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player, int timeIndex) {
        XHJZWar.XWStateInfo.Builder stateInfo = XHJZWar.XWStateInfo.newBuilder();
        stateInfo.setState(XHJZWar.XWState.XW_BATTLE);
        HawkTuple2<Long, Long> battleTime = XHJZWarService.getInstance().getBattleTime(timeIndex);
        if(battleTime != null){
            stateInfo.setEndTime(battleTime.second);
        }else {
            stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1));
        }
        return stateInfo;
    }
}
