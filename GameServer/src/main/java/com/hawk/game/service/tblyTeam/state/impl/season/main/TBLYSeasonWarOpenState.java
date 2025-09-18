package com.hawk.game.service.tblyTeam.state.impl.season.main;

import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYSeasonWarOpenState implements ITBLYSeasonState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onWarOpen();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if(cfg != null) {
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getWarEndTimeValue()) {
                TBLYSeasonService.getInstance().toNext();
            }
        }
    }

    @Override
    public void leave() {
        TBLYSeasonService.getInstance().leaveWarOpen();
    }

    @Override
    public TiberiumWar.TLWStateInfo.Builder  getStateInfo(Player player) {
        TiberiumWar.TLWStateInfo.Builder builder = TiberiumWar.TLWStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWState.TLW_WAR_OPEN);
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
        if (cfg != null){
            builder.setWarStartTime(cfg.getWarStartTimeValue());
            builder.setWarFinishTime(cfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
            builder.setWarEndTime(cfg.getWarEndTimeValue());
        }else {
            builder.setWarStartTime(HawkTime.getMillisecond());
            builder.setWarFinishTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1));
            builder.setWarEndTime(HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(2));
        }
        return builder;
    }
}
