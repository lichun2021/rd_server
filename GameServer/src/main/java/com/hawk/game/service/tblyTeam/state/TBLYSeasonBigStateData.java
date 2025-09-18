package com.hawk.game.service.tblyTeam.state;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class TBLYSeasonBigStateData {
    private int season;
    private TBLYWarStateEnum state = TBLYWarStateEnum.SEASON_BIG_NOT_OPEN;
    private boolean isNext = false;

    public void load(){
        String value = RedisProxy.getInstance().getRedisSession().hGet(TBLYWarResidKey.TBLY_WAR_SEASON_BIG_STATE , GsConfig.getInstance().getServerId());
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        season = obj.getIntValue("season");
        state = TBLYWarStateEnum.seasonBigValueOf(obj.getIntValue("state"));
    }

    public void update(){
        JSONObject obj = new JSONObject();
        obj.put("season", season);
        obj.put("state", state.getIndex());
        RedisProxy.getInstance().getRedisSession().hSet(TBLYWarResidKey.TBLY_WAR_SEASON_BIG_STATE, GsConfig.getInstance().getServerId(), obj.toJSONString());
    }

    @Override
    public String toString() {
        return "TBLYSeasonBigStateData{" +
                "season=" + season +
                ", state=" + state +
                '}';
    }

    public void tick(){
        state.getSeasonBigStateLogic().tick();
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
            state.getSeasonBigStateLogic().leave();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        switch (state){
            case SEASON_BIG_NOT_OPEN:{
                state = TBLYWarStateEnum.SEASON_BIG_SIGNUP;
            }
            break;
            case SEASON_BIG_SIGNUP:{
                state = TBLYWarStateEnum.SEASON_BIG_GROUP_WAIT;
            }
            break;
            case SEASON_BIG_GROUP_WAIT:{
                state = TBLYWarStateEnum.SEASON_BIG_GROUP;
            }
            break;
            case SEASON_BIG_GROUP:{
                state = TBLYWarStateEnum.SEASON_BIG_KICK_OUT;
            }
            break;
            case SEASON_BIG_KICK_OUT:{
                state = TBLYWarStateEnum.SEASON_BIG_FINAL;
            }
            break;
            case SEASON_BIG_FINAL:{
                state = TBLYWarStateEnum.SEASON_BIG_END_SHOW;
            }
            break;
            case SEASON_BIG_END_SHOW:{
                state = TBLYWarStateEnum.SEASON_BIG_NOT_OPEN;
            }
            break;
        }
        try {
            state.getSeasonBigStateLogic().enter();
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

    public TBLYWarStateEnum getState() {
        return state;
    }

    public TiberiumWar.TLWBigStateInfo.Builder getStateInfo(Player player) {
        return state.getSeasonBigStateLogic().getStateInfo(player);
    }
}
