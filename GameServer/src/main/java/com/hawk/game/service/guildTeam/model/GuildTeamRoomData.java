package com.hawk.game.service.guildTeam.model;

import com.alibaba.fastjson.JSON;
import org.hawk.os.HawkOSOperator;

public class GuildTeamRoomData {
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
    public int matchType;
    public int groupType;
    public int battleType;
    public int groupId;

    public GuildTeamRoomData(){

    }

    public GuildTeamRoomData(int termId, int timeIndex, GuildTeamData campA, GuildTeamData campB){
        this.termId = termId;
        this.timeIndex = timeIndex;
        this.id = campA.id + "_" + campB.id;
        this.campA = campA.id;
        this.campB = campB.id;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static GuildTeamRoomData unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        GuildTeamRoomData roomData = JSON.parseObject(json, GuildTeamRoomData.class);
        return roomData;
    }
}
