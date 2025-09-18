package com.hawk.game.service.tblyTeam.state.impl.season.big;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonBigState;
import org.hawk.os.HawkTime;

public class TBLYSeasonBigEndShowState implements ITBLYSeasonBigState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onBigEndShow();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getFinalTimeCfg();
        if(cfg != null){
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getSeasonEndTimeValue()) {
                TBLYSeasonService.getInstance().toBigNext();
            }
        }
    }

    @Override
    public void leave() {
    	TBLYSeasonService.getInstance().cleanSeasonData();
    }

    @Override
    public TLWBigStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
        builder.setState(TLWBigState.TLW_BIG_FINAL);
        return builder;
    }
}
