package com.hawk.game.module.lianmengyqzz.march.data.local;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.protocol.YQZZWar;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.Map;

public class YQZZSeasonGiftRecordData implements IYQZZData {
    private static final String redisKey = "YQZZ_ACTIVITY_AWARD_RECORD_DATA";
    private int season = 0;
    private String serverId = "";//收礼包玩家区分id

    private long sendTime = 0L; // 礼包发放时间
    private int giftId = 0; // 礼包ID
    private String sendPlayerName = ""; // 发礼包玩家名字
    private String playerName = ""; // 收礼包玩家名字
    private String playerId = ""; // 收礼包玩家ID
    private String guildTag = ""; // 收礼包玩家工会名称
    private String sendPlayerTag = ""; // 发礼包玩家名字

    public String getRedisKey() {
        return redisKey + ":" + this.season + ":" + this.serverId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public String getSendPlayerName() {
        return sendPlayerName;
    }

    public void setSendPlayerName(String sendPlayerName) {
        this.sendPlayerName = sendPlayerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGuildTag() {
        return guildTag;
    }

    public void setGuildTag(String guildTag) {
        this.guildTag = guildTag;
    }

    public String getSendPlayerTag() {
        return sendPlayerTag;
    }

    public void setSendPlayerTag(String sendPlayerTag) {
        this.sendPlayerTag = sendPlayerTag;
    }

    public static Map<String, YQZZSeasonGiftRecordData> loadAll(int season, String serverId){
        String key = redisKey + ":" + season + ":" + serverId;
        Map<String, YQZZSeasonGiftRecordData> rlt = new HashMap<>();
        Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key, YQZZConst.REDIS_DATA_EXPIRE_TIME);
        for(Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (HawkOSOperator.isEmptyString(value)) {
                continue;
            }
            YQZZSeasonGiftRecordData recordData = new YQZZSeasonGiftRecordData();
            recordData.mergeFrom(value);
            rlt.put(recordData.getPlayerId(), recordData);
        }
        return rlt;
    }

    @Override
    public void saveRedis() {
        RedisProxy.getInstance().getRedisSession().hSet(getRedisKey(), playerId, this.serializ());
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("season", this.season);
        obj.put("serverId", this.serverId);
        obj.put("sendTime", this.sendTime);
        obj.put("giftId", this.giftId);
        obj.put("sendPlayerName", this.sendPlayerName);
        obj.put("playerName", this.playerName);
        obj.put("playerId", this.playerId);
        obj.put("guildTag", this.guildTag);
        obj.put("sendPlayerTag", this.sendPlayerTag);
        return obj.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.season = obj.getIntValue("season");
        this.serverId = obj.getString("serverId");
        this.sendTime = obj.getLongValue("sendTime");
        this.giftId = obj.getIntValue("giftId");
        this.sendPlayerName = obj.getString("sendPlayerName");
        this.playerName = obj.getString("playerName");
        this.playerId = obj.getString("playerId");
        this.guildTag = obj.getString("guildTag");
        this.sendPlayerTag = obj.getString("sendPlayerTag");
    }

    public YQZZWar.YQZZGiftRecord.Builder toPB(){
        YQZZWar.YQZZGiftRecord.Builder record = YQZZWar.YQZZGiftRecord.newBuilder();
        record.setSendTime(this.sendTime);
        record.setGiftId(this.giftId);
        record.setSendPlayerName(this.sendPlayerName);
        record.setSendPlayerTag(this.sendPlayerTag);
        record.setPlayerName(this.playerName);
        record.setGuildTag(guildTag);
        record.setPlayerId(this.playerId);
        record.setServerId(this.getServerId());
        return record;
    }
}
