package com.hawk.game.service.tblyTeam.state;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class TBLYSeasonStateData {
    private int season;
    private int termId;
    private TBLYWarStateEnum state = TBLYWarStateEnum.SEASON_NOT_OPEN;
    private boolean isNext = false;

    public void load(){
        String value = RedisProxy.getInstance().getRedisSession().hGet(TBLYWarResidKey.TBLY_WAR_SEASON_STATE , GsConfig.getInstance().getServerId());
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        season = obj.getIntValue("season");
        termId = obj.getIntValue("termId");
        state = TBLYWarStateEnum.seasonValueOf(obj.getIntValue("state"));
    }

    public void update(){
        JSONObject obj = new JSONObject();
        obj.put("season", season);
        obj.put("termId", termId);
        obj.put("state", state.getIndex());
        RedisProxy.getInstance().getRedisSession().hSet(TBLYWarResidKey.TBLY_WAR_SEASON_STATE, GsConfig.getInstance().getServerId(), obj.toJSONString());
    }

    @Override
    public String toString() {
        return "TBLYSeasonStateData{" +
                "season=" + season +
                ", termId=" + termId +
                ", state=" + state +
                '}';
    }

    public void tick(){
        state.getSeasonStateLogic().tick();
        if(isNext){
            try {
                next();
            }catch (Exception e){
                HawkException.catchException(e);
            }
            isNext = false;
        }
    }

    public void next(){
        try {
            state.getSeasonStateLogic().leave();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        switch (state){
            case SEASON_NOT_OPEN:{
                state = TBLYWarStateEnum.SEASON_PEACE;
            }
            break;
            case SEASON_PEACE:{
                state = TBLYWarStateEnum.SEASON_MATCH;
            }
            break;
            case SEASON_MATCH:{
                state = TBLYWarStateEnum.SEASON_WAR_MANGE;
            }
            break;
            case SEASON_WAR_MANGE:{
                state = TBLYWarStateEnum.SEASON_WAR_WAIT;
            }
            break;
            case SEASON_WAR_WAIT:{
                state = TBLYWarStateEnum.SEASON_WAR_OPEN;
            }
            break;
            case SEASON_WAR_OPEN:{
                if(termId >= TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
                    state = TBLYWarStateEnum.SEASON_NOT_OPEN;
                }else {
                    state = TBLYWarStateEnum.SEASON_PEACE;
                }

            }
            break;
        }
        try {
            state.getSeasonStateLogic().enter();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        update();
        HawkLog.logPrintln(toString());
        TBLYSeasonService.getInstance().syncAllPLayer();
    }

    public void toNext(){
        isNext = true;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public TBLYWarStateEnum getState() {
        return state;
    }

    public TiberiumWar.TLWStateInfo.Builder  getStateInfo(Player player) {
        return state.getSeasonStateLogic().getStateInfo(player);
    }
}
