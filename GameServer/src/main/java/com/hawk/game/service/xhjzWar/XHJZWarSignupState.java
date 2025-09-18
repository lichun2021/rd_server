package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import com.hawk.game.config.XHJZWarTimeCfg;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;
import com.hawk.game.service.commonMatch.state.CMWStateEnum;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class XHJZWarSignupState implements IXHJZWarState {
    @Override
    public void enter() {
        XHJZWarService.getInstance().onSignup();
        if(XHJZSeasonManager.getInstance().getState() != CMWStateEnum.CLOSE){
            XHJZSeasonManager.getInstance().onSignup();
        }
    }

    @Override
    public void tick() {
        XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getTimeCfg();
        if(cfg != null){
            long now = HawkTime.getMillisecond();
            if(now >= cfg.getMatchWaitTime()){
                XHJZWarService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player) {
        XHJZWar.XWStateInfo.Builder stateInfo = XHJZWar.XWStateInfo.newBuilder();
        stateInfo.setState(XHJZWar.XWState.XW_SIGNUP);
        XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getTimeCfg();
        if(cfg != null){
            stateInfo.setEndTime(cfg.getMatchWaitTime());
        }else {
            stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
        }
        return stateInfo;
    }
}
