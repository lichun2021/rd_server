package com.hawk.game.service.xhjzWar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.XHJZWar;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import java.util.concurrent.TimeUnit;

public class XHJZWarTeamData {
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
    public boolean isNew;
    public int openDayW;
    public long seasonScore;

    public XHJZWarTeamData(){

    }

    public XHJZWarTeamData(GuildInfoObject guildInfoObject, int index){
        this.id = guildInfoObject.getId() + ":" + index;
        this.guildId = guildInfoObject.getId();
        this.guildName = guildInfoObject.getName();
        this.guildTag = guildInfoObject.getTag();
        this.guildFlag = guildInfoObject.getFlagId();
        this.creatTime = HawkTime.getMillisecond();
        this.serverId = guildInfoObject.getServerId();

    }

    public void refreshInfo(){
        HawkTuple2<Long, Integer> powerAndCount = XHJZWarService.getInstance().getTeamPowerAndCnt(id);
        this.battlePoint = powerAndCount.first;
        this.memberCnt = powerAndCount.second;
        this.rank = XHJZWarService.getInstance().getTeamPowerRank(id);
    }

    public XHJZWar.XWTeamInfo.Builder toPB(){
        XHJZWar.XWTeamInfo.Builder info = XHJZWar.XWTeamInfo.newBuilder();
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
        HawkTuple2<Long, Long> battleTime = XHJZWarService.getInstance().getBattleTime(timeIndex);
        if(battleTime != null){
            info.setBattleStartTime(battleTime.first);
            info.setBattleEndtime(battleTime.second);
        }else {
            long startTime = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(30);
            long endTime = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(60);
            info.setBattleStartTime(startTime);
            info.setBattleEndtime(endTime);
        }
        info.setRank(rank);
        info.setScore(score);
        return info;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static XHJZWarTeamData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        XHJZWarTeamData teamData = JSON.parseObject(json, XHJZWarTeamData.class);
        return teamData;
    }

    public void update(){
        RedisProxy.getInstance().getRedisSession().hSet(XHJZRedisKey.XHJZ_WAR_TEAM, id, serialize());
    }

    public void remove(){
        RedisProxy.getInstance().getRedisSession().hDel(XHJZRedisKey.XHJZ_WAR_TEAM, id);
    }
}
