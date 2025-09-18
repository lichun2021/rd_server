package com.hawk.game.service.guildTeam.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.protocol.TiberiumWar;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;


public class GuildTeamData {
    public String id = "";//战队id
    public String guildId = "" ;//联盟id
    public String guildName = "";//联盟名字
    public String guildTag= "";//联盟简称
    public int guildFlag;//联盟旗帜
    public long creatTime;//创建时间
    public String name = "";//战队名字
    public String serverId = "";//服务器
    public long battlePoint;//参战人员总战力
    public int memberCnt;//参战人员数量
    public int timeIndex;//报名时间段
    public String oppTeamId = "";
    public int rank ;//比赛排行
    public long score;//比赛积分
    public long matchPower;//匹配战力
    public int openDayW;

    public GuildTeamData(){

    }

    public GuildTeamData(GuildInfoObject guildInfoObject, int index){
        this.id = guildInfoObject.getId() + ":" + index;
        this.guildId = guildInfoObject.getId();
        this.guildName = guildInfoObject.getName();
        this.guildTag = guildInfoObject.getTag();
        this.guildFlag = guildInfoObject.getFlagId();
        this.creatTime = HawkTime.getMillisecond();
        this.serverId = guildInfoObject.getServerId();
    }

    public GuildTeamInfo.Builder toPB(){
        GuildTeamInfo.Builder info = GuildTeamInfo.newBuilder();
        info.setId(id);
        info.setGuildId(guildId);
        info.setGuildName(guildName);
        info.setGuildTag(guildTag);
        info.setGuildFlag(guildFlag);
        info.setCreatTime(creatTime);
        info.setName(name);
        info.setServerId(serverId);
        info.setBattlePoint(battlePoint);
        info.setMemberCnt(memberCnt);
        info.setTimeIndex(timeIndex);
        info.setRank(rank);
        info.setScore(score);
        return info;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static GuildTeamData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        GuildTeamData teamData = JSON.parseObject(json, GuildTeamData.class);
        return teamData;
    }

    public TiberiumWar.TWGuildInfo.Builder toTWGuildInfo(){
        TiberiumWar.TWGuildInfo.Builder builder = TiberiumWar.TWGuildInfo.newBuilder();
        builder.setId(this.guildId);
        builder.setName(this.name);
        builder.setTag(this.guildTag);
        builder.setGuildFlag(this.guildFlag);
        builder.setServerId(this.serverId);
        builder.setBattlePoint(this.battlePoint);
        builder.setMemberCnt(this.memberCnt);
        builder.setScore(this.score);
        return builder;
    }

    public TiberiumWar.TLWGuildInfo.Builder toTLWGuildInfo(){
        TiberiumWar.TLWGuildInfo.Builder builder = TiberiumWar.TLWGuildInfo.newBuilder();
        builder.setBaseInfo(genBaseInfo());
        builder.setBattlePoint(this.battlePoint);
        builder.setMemberCnt(this.memberCnt);
        return builder;
    }

    public TiberiumWar.TLWGuildBaseInfo.Builder genBaseInfo(){
        TiberiumWar.TLWGuildBaseInfo.Builder builder = TiberiumWar.TLWGuildBaseInfo.newBuilder();
        builder.setId(this.guildId);
        builder.setName(this.guildName);
        builder.setTag(this.guildTag);
        builder.setGuildFlag(this.guildFlag);
        builder.setLeaderName(this.name);
        builder.setServerId(this.serverId);
        return builder;
    }
}
