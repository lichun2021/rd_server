package com.hawk.game.service.tblyTeam.state.impl.battle;

import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarBattleState;
import org.hawk.os.HawkTime;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TBLYWarBattleOpenState implements ITBLYWarBattleState {

    @Override
    public void enter(int timeIndex) {
        TBLYWarService.getInstance().onBattleOpen(timeIndex);
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
        if(now >= warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime()){
            TBLYWarService.getInstance().toBattleNext(timeIndex);
        }
    }

    @Override
    public void leave(int timeIndex) {

    }

    @Override
    public TWStateInfo.Builder getStateInfo(Player player, int timeIndex) {
        TWStateInfo.Builder builder = TWStateInfo.newBuilder();
        builder.setState(TWState.WAR_OPEN);
        List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
        if(timeList.size() < timeIndex){
            builder.setWarEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
        }else {
            WarTimeChoose choose = timeList.get(timeIndex - 1);
            long warStartTime = choose.getTime();
            builder.setWarEndTime(warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime());
        }
        return builder;
    }
}
