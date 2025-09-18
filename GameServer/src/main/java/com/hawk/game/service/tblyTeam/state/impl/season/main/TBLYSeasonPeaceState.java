package com.hawk.game.service.tblyTeam.state.impl.season.main;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonState;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYSeasonPeaceState implements ITBLYSeasonState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onPeace();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getMatchStartTimeValue()) {
                TBLYSeasonService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {
        TBLYSeasonService.getInstance().leavePeace();
    }

    @Override
    public TiberiumWar.TLWStateInfo.Builder  getStateInfo(Player player) {
        TiberiumWar.TLWStateInfo.Builder builder = TiberiumWar.TLWStateInfo.newBuilder();
        TBLYWarStateEnum bigState = TBLYSeasonService.getInstance().getBigState();
        if(bigState == TBLYWarStateEnum.SEASON_BIG_FINAL){
            builder.setState(TiberiumWar.TLWState.TLW_MATCH);
        }else {
            builder.setState(TiberiumWar.TLWState.TLW_PEACE);
        }

        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if (cfg != null){
            builder.setMatchStartTime(cfg.getMatchStartTimeValue());
        }else {
            builder.setMatchStartTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1));
        }
        return builder;
    }
}
