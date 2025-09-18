package com.hawk.game.service.guildTeam.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.entity.GuildInfoObject;
import org.hawk.os.HawkOSOperator;

public class GuildTeamGuildData {
    public String id;
    public String serverId;
    public String name;
    public String tag;
    public int flag;
    public String leaderId;
    public String leaderName;
    public long power;

    public GuildTeamGuildData(){

    }

    public GuildTeamGuildData(GuildInfoObject guildInfoObject){
        this.id = guildInfoObject.getId();
        this.name = guildInfoObject.getName();
        this.tag = guildInfoObject.getTag();
        this.flag = guildInfoObject.getFlagId();
        this.leaderId = guildInfoObject.getLeaderId();
        this.leaderName = guildInfoObject.getLeaderName();
        this.power = guildInfoObject.getLastRankPower();
        this.serverId = guildInfoObject.getServerId();
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static GuildTeamGuildData unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        GuildTeamGuildData guildData = JSON.parseObject(json, GuildTeamGuildData.class);
        return guildData;
    }

    public String getId() {
        return id;
    }

    public String getServerId() {
        return serverId;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public int getFlag() {
        return flag;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public long getPower() {
        return power;
    }
}
