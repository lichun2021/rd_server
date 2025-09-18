package com.hawk.game.service.tblyTeam.state.impl.season.main;

import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkTime;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonState;

public class TBLYSeasonMatchState implements ITBLYSeasonState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onMatch();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getMatchEndTimeValue()) {
                TBLYSeasonService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {
        TBLYSeasonService.getInstance().leaveMatch();
    }

    @Override
    public TiberiumWar.TLWStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWStateInfo.Builder builder = TiberiumWar.TLWStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWState.TLW_MATCH);
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if (cfg != null){
            builder.setMatchEndTime(cfg.getManageEndTimeValue());
        }else {
            builder.setMatchEndTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1));
        }
        return builder;
    }
}
