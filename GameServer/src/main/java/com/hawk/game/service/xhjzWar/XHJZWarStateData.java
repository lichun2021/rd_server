package com.hawk.game.service.xhjzWar;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

public class XHJZWarStateData {
    private int termId;
    private XHJZWarStateEnum state = XHJZWarStateEnum.PEACE;
    private boolean isNext = false;

    public void load(){
        String value = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_STATE , GsConfig.getInstance().getServerId());
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        termId = obj.getIntValue("termId");
        state = XHJZWarStateEnum.valueOf(obj.getIntValue("state"));
    }

    public void update(){
        JSONObject obj = new JSONObject();
        obj.put("termId", termId);
        obj.put("state", state.getIndex());
        RedisProxy.getInstance().getRedisSession().hSet(XHJZRedisKey.XHJZ_WAR_STATE, GsConfig.getInstance().getServerId(), obj.toJSONString());
    }

    @Override
    public String toString() {
        return "XHJZWarStateData{" +
                "termId=" + termId +
                ", state=" + state +
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
                state = XHJZWarStateEnum.SIGNUP;
            }
            break;
            case SIGNUP:{
                state = XHJZWarStateEnum.MATCH_WAIT;
            }
            break;
            case MATCH_WAIT:{
                state = XHJZWarStateEnum.MATCH;
            }
            break;
            case MATCH:{
                state = XHJZWarStateEnum.MATCH_END;
            }
            break;
            case MATCH_END:{
                state = XHJZWarStateEnum.BATTLE;
            }
            break;
            case BATTLE:{
                state = XHJZWarStateEnum.FINISH;
            }
            break;
            case FINISH:{
                state = XHJZWarStateEnum.PEACE;
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
        XHJZWarService.getInstance().syncAllPLayer();
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

    public XHJZWarStateEnum getState() {
        return state;
    }

    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player){
        return state.getStateLogic().getStateInfo(player);
    }
}
