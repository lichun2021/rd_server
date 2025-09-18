package com.hawk.game.service.tblyTeam.state.impl.battle;

import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarBattleState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYWarBattleEndState implements ITBLYWarBattleState {

    @Override
    public void enter(int timeIndex) {
        TBLYWarService.getInstance().onBattleEnd(timeIndex);
    }

    @Override
    public void tick(int timeIndex) {

    }

    @Override
    public void leave(int timeIndex) {

    }

    @Override
    public TWStateInfo.Builder getStateInfo(Player player, int timeIndex) {
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
