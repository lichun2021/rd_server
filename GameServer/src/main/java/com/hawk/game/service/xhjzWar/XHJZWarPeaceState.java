package com.hawk.game.service.xhjzWar;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import com.hawk.game.config.XHJZConstCfg;
import com.hawk.game.config.XHJZWarTimeCfg;
import org.hawk.config.HawkConfigManager;

public class XHJZWarPeaceState implements IXHJZWarState {
    @Override
    public void enter() {

    }

    @Override
    public void tick() {
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(!constCfg.getOpenServerIdSet().isEmpty() && !constCfg.getOpenServerIdSet().contains(GsConfig.getInstance().getServerId())){
            return;
        }
        long openTime = GsApp.getInstance().getServerOpenTime();
        XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getCurTimeCfg();
        if(cfg != null && (openTime + constCfg.getServerDelay() >= cfg.getSignupTime())){
            return;
        }
        String serverId = GsConfig.getInstance().getServerId();
        // 本期活动和合服时间重叠,本期不开启
        if (cfg != null && AssembleDataManager.getInstance().isCrossOverMergeServerCfg(cfg.getSignupTime(), cfg.getEndTime(), serverId)) {
            return;
        }
        if(cfg != null){
            XHJZWarService.getInstance().onOpen(cfg.getTermId());
        }
    }

    @Override
    public void leave() {
        if(GsConfig.getInstance().isDebug()){
            int termId = XHJZWarService.getInstance().getTermId();
            if(termId == 0){
                String serverId = GsConfig.getInstance().getServerId();
                for(int i = 1; i <= 4; i++){
                    RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, i));
                }
                RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_SERVER, termId, serverId));
                RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_MATCH, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_TEAM_AWARD, termId));
                RedisProxy.getInstance().getRedisSession().del(String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_AWARD, termId));
            }
        }
    }

    @Override
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player) {
        XHJZWar.XWStateInfo.Builder stateInfo = XHJZWar.XWStateInfo.newBuilder();
        stateInfo.setState(XHJZWar.XWState.XW_PEACE);
        stateInfo.setEndTime(Long.MAX_VALUE);
        return stateInfo;
    }
}
