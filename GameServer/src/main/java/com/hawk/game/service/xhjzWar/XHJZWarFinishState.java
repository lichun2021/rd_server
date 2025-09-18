package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import com.hawk.game.config.XHJZWarTimeCfg;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class XHJZWarFinishState implements IXHJZWarState {
    @Override
    public void enter() {
        XHJZSeasonManager.getInstance().onEnd();
    }

    @Override
    public void tick() {
        XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getTimeCfg();
        if(cfg != null){
            long now = HawkTime.getMillisecond();
            if(now >= cfg.getEndTime()){
                XHJZWarService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {
        XHJZWarService.getInstance().onEnd();
    }

    @Override
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player) {
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
