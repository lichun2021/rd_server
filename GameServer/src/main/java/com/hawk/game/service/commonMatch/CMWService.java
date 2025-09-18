package com.hawk.game.service.commonMatch;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.PBCommonMatch;
import com.hawk.game.service.commonMatch.manager.CMWManagerBase;
import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkException;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CMWService extends HawkAppObj {
    Map<PBCommonMatch.PBCMWMatchType, CMWManagerBase> managerMap = new ConcurrentHashMap<>();
    public static CMWService instance = null;

    public static CMWService getInstance() {
        return instance;
    }

    public CMWService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    public boolean init(){
        for(CMWMatchTypeEnmu matchTypeEnmu : CMWMatchTypeEnmu.values()){
            try {
                managerMap.put(matchTypeEnmu.getType(), matchTypeEnmu.getManager());
                matchTypeEnmu.getManager().init();
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        addTickable(new HawkPeriodTickable(1000) {
            @Override
            public void onPeriodTick() {
                onTickPerOneSecond();
            }
        });
        addTickable(new HawkPeriodTickable(TimeUnit.MINUTES.toMillis(10)) {
            @Override
            public void onPeriodTick() {
                onTickPerTenMinute();
            }
        });
        return true;
    }

    public void onTickPerOneSecond(){
        for(CMWMatchTypeEnmu matchTypeEnmu : CMWMatchTypeEnmu.values()){
            try {
                matchTypeEnmu.getManager().onTickPerOneSecond();
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    public void onTickPerTenMinute(){
        for(CMWMatchTypeEnmu matchTypeEnmu : CMWMatchTypeEnmu.values()){
            try {
                matchTypeEnmu.getManager().onTickPerTenMinute();
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    public void pageInfo(Player player, PBCommonMatch.PBCMWPageInfoReq req){
        CMWManagerBase manager = managerMap.get(req.getMatchType());
        if(manager == null){
            return;
        }
        manager.pageInfo(player, req);
    }

    public void rankInfo(Player player, PBCommonMatch.PBCMWRankInfoReq req){
        CMWManagerBase manager = managerMap.get(req.getMatchType());
        if(manager == null){
            return;
        }
        manager.rankInfo(player, req);
    }

    public void battleInfo(Player player, PBCommonMatch.PBCMWBattleInfoReq req){
        CMWManagerBase manager = managerMap.get(req.getMatchType());
        if(manager == null){
            return;
        }
        manager.battleInfo(player, req);
    }

    public void timeInfo(Player player, PBCommonMatch.PBCMWBattleTimeReq req){
        CMWManagerBase manager = managerMap.get(req.getMatchType());
        if(manager == null){
            return;
        }
        manager.timeInfo(player, req);
    }

    public void targetInfo(Player player, PBCommonMatch.PBCMWBattleTargetReq req){
        CMWManagerBase manager = managerMap.get(req.getMatchType());
        if(manager == null){
            return;
        }
        manager.targetInfo(player, req);
    }


    /**
     * gm入口
     * @param map gm参数
     * @return 活动信息
     */
    public String gm(Map<String, String> map){
        String type = map.getOrDefault("type", "");
        CMWMatchTypeEnmu matchTypeEnmu = CMWMatchTypeEnmu.valueOf(type);
        CMWManagerBase manager = managerMap.get(matchTypeEnmu.getType());
        if(manager == null){
            return "类型不存在";
        }
        return manager.gm(map);
    }
}
