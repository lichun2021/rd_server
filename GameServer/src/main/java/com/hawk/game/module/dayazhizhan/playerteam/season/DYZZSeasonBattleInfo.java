package com.hawk.game.module.dayazhizhan.playerteam.season;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZ;

public class DYZZSeasonBattleInfo implements SerializJsonStrAble {
    private String playerId;

    private String guildId;

    private boolean win;

    private boolean mvp;

    private int kda;

    private int baseHp;

    private int seasonAddScore;

    private boolean isSeason;

    public DYZZSeasonBattleInfo(){

    }

    public DYZZSeasonBattleInfo(int winCamp, DYZZ.PBPlayerInfo pinfo, DYZZ.PBGuildInfo ginfo, boolean isSeason){
        this.playerId = pinfo.getPlayerId();
        this.guildId = ginfo.getGuildId();
        this.win = winCamp == pinfo.getCamp();
        this.mvp = pinfo.getMvp() == 1;
        this.kda = pinfo.getKda();
        this.baseHp = ginfo.getBaseHP();
        this.seasonAddScore = pinfo.getSeasonScoreAdd();
        this.isSeason = isSeason;
    }


    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("playerId", playerId);
        obj.put("guildId", guildId);
        obj.put("win", win);
        obj.put("mvp", mvp);
        obj.put("kda", kda);
        obj.put("baseHp", baseHp);
        obj.put("seasonAddScore", seasonAddScore);
        obj.put("isSeason", isSeason);
        return obj.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        JSONObject obj = JSON.parseObject(serialiedStr);
        this.playerId = obj.getString("playerId");
        this.guildId = obj.getString("guildId");
        this.win = obj.getBooleanValue("win");
        this.mvp = obj.getBooleanValue("mvp");
        this.kda = obj.getIntValue("kda");
        this.baseHp = obj.getIntValue("baseHp");
        this.seasonAddScore = obj.getIntValue("seasonAddScore");
        this.isSeason = obj.getBooleanValue("isSeason");
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getGuildId() {
        return guildId;
    }

    public boolean isWin() {
        return win;
    }

    public boolean isMvp() {
        return mvp;
    }

    public int getKda() {
        return kda;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getSeasonAddScore() {
        return seasonAddScore;
    }

    public boolean isSeason() {
        return isSeason;
    }
}
