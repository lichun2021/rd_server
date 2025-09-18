package com.hawk.game.service.tblyTeam.model;

import com.alibaba.fastjson.JSON;
import org.hawk.os.HawkOSOperator;

import java.util.HashSet;
import java.util.Set;

public class TBLYSeasonPlayerData {
    public String id;
    public long score;
    public long guildscore;
    // 已领取的个人积分奖励
    public Set<Integer> playerRewarded;
    // 已领取的联盟积分奖励
    public Set<Integer> guildRewarded;

    public TBLYSeasonPlayerData(){

    }

    public TBLYSeasonPlayerData(String plyerId) {
        this.id = plyerId;
        this.playerRewarded = new HashSet<>();
        this.guildRewarded = new HashSet<>();
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

    public long getGuildscore() {
        return guildscore;
    }

    public void setGuildscore(long guildscore) {
        this.guildscore = guildscore;
    }

    public Set<Integer> getPlayerRewarded() {
        return playerRewarded;
    }

    public void setPlayerRewarded(Set<Integer> playerRewarded) {
        this.playerRewarded = playerRewarded;
    }

    public Set<Integer> getGuildRewarded() {
        return guildRewarded;
    }

    public void setGuildRewarded(Set<Integer> guildRewarded) {
        this.guildRewarded = guildRewarded;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static TBLYSeasonPlayerData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        return JSON.parseObject(json, TBLYSeasonPlayerData.class);
    }
}
