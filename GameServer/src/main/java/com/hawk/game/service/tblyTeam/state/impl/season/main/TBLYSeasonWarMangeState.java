package com.hawk.game.service.tblyTeam.state.impl.season.main;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYSeasonWarMangeState implements ITBLYSeasonState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onWarManager();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getManageEndTimeValue()) {
                TBLYSeasonService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public TiberiumWar.TLWStateInfo.Builder  getStateInfo(Player player) {
        TiberiumWar.TLWStateInfo.Builder builder = TiberiumWar.TLWStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWState.TLW_WAR_MANGE);
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if (cfg != null){
            builder.setMangeEndTime(cfg.getManageEndTimeValue());
            builder.setWarStartTime(cfg.getWarStartTimeValue());
        }else {
            builder.setMangeEndTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1));
            builder.setWarStartTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(2));
        }
        return builder;
    }
}
