package com.hawk.game.service.tblyTeam.state;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class TBLYWarStateData  {
    private int termId;
    private TBLYWarStateEnum state = TBLYWarStateEnum.PEACE;
    private boolean isNext = false;

    public void load(){
        String value = RedisProxy.getInstance().getRedisSession().hGet(TBLYWarResidKey.TBLY_WAR_STATE , GsConfig.getInstance().getServerId());
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        termId = obj.getIntValue("termId");
        state = TBLYWarStateEnum.valueOf(obj.getIntValue("state"));
    }

    public void update(){
        JSONObject obj = new JSONObject();
        obj.put("termId", termId);
        obj.put("state", state.getIndex());
        RedisProxy.getInstance().getRedisSession().hSet(TBLYWarResidKey.TBLY_WAR_STATE, GsConfig.getInstance().getServerId(), obj.toJSONString());
    }

    @Override
    public String toString() {
        return "TBLYWarStateData{" +
                "state=" + state +
                ", termId=" + termId +
                '}';
    }

    public void tick(){
        state.getStateLogic().tick();
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
            state.getStateLogic().leave();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        switch (state){
            case PEACE:{
                state = TBLYWarStateEnum.SIGNUP;
            }
            break;
            case SIGNUP:{
                state = TBLYWarStateEnum.MATCH_WAIT;
            }
            break;
            case MATCH_WAIT:{
                state = TBLYWarStateEnum.MATCH;
            }
            break;
            case MATCH:{
                state = TBLYWarStateEnum.MATCH_END;
            }
            break;
            case MATCH_END:{
                state = TBLYWarStateEnum.BATTLE;
            }
            break;
            case BATTLE:{
                state = TBLYWarStateEnum.FINISH;
            }
            break;
            case FINISH:{
                state = TBLYWarStateEnum.PEACE;
            }
            break;
        }
        try {
            state.getStateLogic().enter();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        update();
        HawkLog.logPrintln(toString());
        TBLYWarService.getInstance().syncAllPLayer();
    }

    public void toNext(){
        isNext = true;
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

    public TWStateInfo.Builder getStateInfo(Player player){
        return state.getStateLogic().getStateInfo(player);
    }
}
