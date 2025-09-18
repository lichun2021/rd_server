package com.hawk.game.service.tblyTeam.state.impl.main;

import com.hawk.game.GsConfig;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.ITBLYWarState;
import com.hawk.game.util.GameUtil;
import org.hawk.os.HawkTime;

import java.util.Date;
import java.util.List;

public class TBLYWarPeaceState implements ITBLYWarState {
    @Override
    public void enter() {


    }

    @Override
    public void tick() {
        TiberiumTimeCfg cfg = TBLYWarService.getInstance().getCurTimeCfg();
        if(cfg == null){
            return;
        }
        String serverId = GsConfig.getInstance().getServerId();
        List<String> limitServerLimit = cfg.getLimitServerList();
        List<String> forbidServerLimit = cfg.getForbidServerList();

        if (!limitServerLimit.isEmpty() && !limitServerLimit.contains(serverId)){
            return;
        }
        if(!forbidServerLimit.isEmpty() && forbidServerLimit.contains(serverId)) {
            return;
        }
        long openTime = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
        long serverDelay = TiberiumConstCfg.getInstance().getServerDelay();
        if(openTime + serverDelay >= cfg.getSignStartTimeValue()){
            return;
        }
        // 本期活动和合服时间重叠,本期不开启
        if(AssembleDataManager.getInstance().isCrossOverMergeServerCfg(cfg.getSignStartTimeValue(), cfg.getWarEndTimeValue(), serverId)) {
            return;
        }
        TBLYWarService.getInstance().onOpen(cfg.getTermId());
    }

    @Override
    public void leave() {
        if(GsConfig.getInstance().isDebug()){
            int termId = TBLYWarService.getInstance().getTermId();
            if(termId == 0) {
                String serverId = GsConfig.getInstance().getServerId();
                for(int i = 1; i <= 4; i++){
                    RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, termId, i));
                }
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_SERVER, termId, serverId));
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_ROOM_SERVER, termId, serverId));
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_MATCH, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_TEAM_AWARD, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(TBLYWarResidKey.TBLY_WAR_PLAYER_AWARD, termId));
            }
        }
    }

    @Override
    public TWStateInfo.Builder getStateInfo(Player player) {
        TWStateInfo.Builder builder = TWStateInfo.newBuilder();
        builder.setState(TWState.NOT_OPEN);
        return builder;
    }
}
