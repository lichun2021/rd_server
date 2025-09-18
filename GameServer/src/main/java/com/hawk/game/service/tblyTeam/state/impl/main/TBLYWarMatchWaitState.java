package com.hawk.game.service.tblyTeam.state.impl.main;

import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYWarMatchWaitState implements ITBLYWarState {
    @Override
    public void enter() {
        TBLYWarService.getInstance().onMatchWait();
    }

    @Override
    public void tick() {
        TiberiumTimeCfg cfg = TBLYWarService.getInstance().getTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            long matchWaitEndTime = cfg.getSignEndTimeValue() + (cfg.getMatchEndTimeValue() - cfg.getSignEndTimeValue())/4;
            if (now >= matchWaitEndTime) {
                TBLYWarService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public TWStateInfo.Builder getStateInfo(Player player) {
        TWStateInfo.Builder builder = TWStateInfo.newBuilder();
        builder.setState(TWState.MATCH_WAIT);
        TiberiumTimeCfg cfg = TBLYWarService.getInstance().getTimeCfg();
        if (cfg != null) {
            builder.setWarStartTime(cfg.getMatchEndTimeValue());
        }else {
            builder.setWarStartTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
        }
        return builder;
    }
}
