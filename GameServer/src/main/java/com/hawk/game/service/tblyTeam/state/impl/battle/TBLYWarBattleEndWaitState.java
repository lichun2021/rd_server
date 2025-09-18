package com.hawk.game.service.tblyTeam.state.impl.battle;

import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarBattleState;
import org.hawk.os.HawkTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TBLYWarBattleEndWaitState implements ITBLYWarBattleState {

    @Override
    public void enter(int timeIndex) {

    }

    @Override
    public void tick(int timeIndex) {
        List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
        if(timeList.size() < timeIndex){
            return;
        }
        WarTimeChoose choose = timeList.get(timeIndex - 1);
        long warStartTime = choose.getTime();
        long now = HawkTime.getMillisecond();
        if(now >= warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime() + TimeUnit.MINUTES.toMillis(10)){
            TBLYWarService.getInstance().toBattleNext(timeIndex);
        }
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
