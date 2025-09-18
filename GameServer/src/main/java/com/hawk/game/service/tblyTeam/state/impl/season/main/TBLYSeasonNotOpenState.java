package com.hawk.game.service.tblyTeam.state.impl.season.main;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonState;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import org.hawk.os.HawkTime;

public class TBLYSeasonNotOpenState implements ITBLYSeasonState {
    @Override
    public void enter() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void leave() {

    }

    @Override
    public TiberiumWar.TLWStateInfo.Builder  getStateInfo(Player player) {
        TiberiumWar.TLWStateInfo.Builder builder = TiberiumWar.TLWStateInfo.newBuilder();
        TBLYWarStateEnum bigState = TBLYSeasonService.getInstance().getBigState();
        if(bigState == TBLYWarStateEnum.SEASON_BIG_FINAL ||
        		bigState == TBLYWarStateEnum.SEASON_BIG_END_SHOW){
            builder.setState(TiberiumWar.TLWState.TLW_PEACE);
            TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getCurrTimeCfg();
            if (cfg != null){
                builder.setWarEndTime(cfg.getWarEndTimeValue());
            }else {
                builder.setWarEndTime(HawkTime.getMillisecond());
            }
        }else {
            builder.setState(TiberiumWar.TLWState.TLW_NOT_OPEN);
        }
        return builder;
    }
}
