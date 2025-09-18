package com.hawk.game.service.xhjzWar;

import com.alibaba.fastjson.JSON;
import com.hawk.game.global.RedisProxy;
import org.hawk.os.HawkOSOperator;

public class XHJZWarRoomData {
    public String id;
    public String campA;
    public long scoreA;
    public String campB;
    public long scoreB;
    public String roomServerId = "";
    public int termId;
    public int timeIndex;
    public String winnerId = "";
    public int roomState;
    public int winCount;
    public boolean isNew;
    public int group;

    public XHJZWarRoomData(){

    }

    public XHJZWarRoomData(int termId, int timeIndex, XHJZWarTeamData campA, XHJZWarTeamData campB){
        this.termId = termId;
        this.timeIndex = timeIndex;
        this.id = campA.id + "_" + campB.id;
        this.campA = campA.id;
        this.campB = campB.id;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static XHJZWarRoomData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        XHJZWarRoomData roomData = JSON.parseObject(json, XHJZWarRoomData.class);
        return roomData;
    }

    public void update(){

        RedisProxy.getInstance().getRedisSession().hSet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), id, serialize());
    }
}
