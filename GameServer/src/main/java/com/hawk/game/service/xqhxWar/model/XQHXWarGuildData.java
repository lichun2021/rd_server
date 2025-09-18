package com.hawk.game.service.xqhxWar.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.entity.GuildInfoObject;
import org.hawk.os.HawkOSOperator;

/**
 * 先驱回响跨服联盟数据
 */
public class XQHXWarGuildData {
    public String id;//联盟id
    public String serverId;//联盟服务器id
    public String name;//联盟名字
    public String tag;//联盟简称
    public int flag;//联盟旗帜
    public String leaderId;//盟主id
    public String leaderName;//盟主名字
    public long power;//联盟战力

    public XQHXWarGuildData(){

    }

    public XQHXWarGuildData(GuildInfoObject guildInfoObject){
        this.id = guildInfoObject.getId();
        this.name = guildInfoObject.getName();
        this.tag = guildInfoObject.getTag();
        this.flag = guildInfoObject.getFlagId();
        this.leaderId = guildInfoObject.getLeaderId();
        this.leaderName = guildInfoObject.getLeaderName();
        this.power = guildInfoObject.getLastRankPower();
        this.serverId = guildInfoObject.getServerId();
    }

    //编码
    public String serialize() {
        return JSON.toJSONString(this);
    }

    //解码
    public static XQHXWarGuildData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        XQHXWarGuildData guildData = JSON.parseObject(json, XQHXWarGuildData.class);
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
