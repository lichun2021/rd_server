package com.hawk.game.service.commonMatch.data;

import com.alibaba.fastjson.JSON;
import com.hawk.game.service.xhjzWar.XHJZWarRoomData;
import com.hawk.game.service.xhjzWar.XHJZWarTeamData;
import org.hawk.os.HawkOSOperator;

public class CMWBattleInfo {
    public String roomId;
    public int termId;
    public int timeIndex;
    public String teamIdA;
    public String teamNameA;
    public String guildNameA;
    public String guildTagA;
    public String serverIdA;
    public String teamIdB;
    public String teamNameB;
    public String guildNameB;
    public String guildTagB;
    public String serverIdB;
    public String winnerId;
    public int winCount;
    public boolean isNew;
    public int group;

    public CMWBattleInfo(){

    }

    public CMWBattleInfo(XHJZWarTeamData teamData1, XHJZWarTeamData teamData2, XHJZWarRoomData roomData){
        this.roomId = roomData.id;
        this.termId = roomData.termId;
        this.timeIndex = roomData.timeIndex;
        this.teamIdA = teamData1.id;
        this.teamNameA = teamData1.name;
        this.guildNameA = teamData1.guildName;
        this.guildTagA = teamData1.guildTag;
        this.serverIdA = teamData1.serverId;
        this.teamIdB = teamData2.id;
        this.teamNameB = teamData2.name;
        this.guildNameB = teamData2.guildName;
        this.guildTagB = teamData2.guildTag;
        this.serverIdB = teamData2.serverId;
        this.winnerId = roomData.winnerId;
        this.winCount = roomData.winCount;
        this.isNew = roomData.isNew;
        this.group = roomData.group;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static CMWBattleInfo unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        return JSON.parseObject(json, CMWBattleInfo.class);
    }
}
