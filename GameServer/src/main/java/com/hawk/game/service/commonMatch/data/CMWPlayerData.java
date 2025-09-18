package com.hawk.game.service.commonMatch.data;

import com.alibaba.fastjson.JSON;
import org.hawk.os.HawkOSOperator;

import java.util.HashSet;
import java.util.Set;

public class CMWPlayerData {
    public String playerId;
    public long teamScore;
    public Set<Integer> teamRewarded;

    public CMWPlayerData(){

    }

    public CMWPlayerData(String plyerId){
        this.playerId = plyerId;
        this.teamRewarded = new HashSet<>();
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static CMWPlayerData unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        return JSON.parseObject(json, CMWPlayerData.class);
    }
}
