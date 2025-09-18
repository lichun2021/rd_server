package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import com.hawk.game.config.XHJZWarTimeCfg;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class XHJZWarBattleEndState implements IXHJZWarBattleState {
    @Override
    public void enter(int timeIndex) {
        XHJZWarService.getInstance().onBattleEnd(timeIndex);
    }

    @Override
    public void tick(int timeIndex) {

    }

    @Override
    public void leave(int timeIndex) {

    }

    @Override
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player, int timeIndex) {
        XHJZWar.XWStateInfo.Builder stateInfo = XHJZWar.XWStateInfo.newBuilder();
        stateInfo.setState(XHJZWar.XWState.XW_FINISH);
        XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getTimeCfg();
        if(cfg != null){
            stateInfo.setEndTime(cfg.getEndTime());
        }else {
            stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
        }
        return stateInfo;
    }
}
