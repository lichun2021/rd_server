package com.hawk.game.service.tblyTeam.model;

import com.alibaba.fastjson.JSON;
import org.hawk.os.HawkOSOperator;


public class TBLYSrasonGuildData {
    public String id;
    public long score;

    public TBLYSrasonGuildData() {
    }

    public TBLYSrasonGuildData(String guildId) {
        this.id = guildId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static TBLYSrasonGuildData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        return JSON.parseObject(json, TBLYSrasonGuildData.class);
    }
}
