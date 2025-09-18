package com.hawk.game.service.tblyTeam.state.impl.main;

import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYWarBattleState implements ITBLYWarState {
    @Override
    public void enter() {
        TBLYWarService.getInstance().onBattle();
    }

    @Override
    public void tick() {
        TiberiumTimeCfg cfg = TBLYWarService.getInstance().getTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getWarEndTimeValue()) {
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
        builder.setState(TWState.WAR_FINISH);
        TiberiumTimeCfg cfg = TBLYWarService.getInstance().getTimeCfg();
        if (cfg != null) {
            builder.setShowEndTime(cfg.getWarEndTimeValue());
        }else {
            builder.setShowEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
        }
        return builder;
    }
}
