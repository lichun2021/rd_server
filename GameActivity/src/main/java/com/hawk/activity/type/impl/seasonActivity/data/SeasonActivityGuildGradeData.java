package com.hawk.activity.type.impl.seasonActivity.data;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonGradeLevelCfg;
import com.hawk.activity.type.impl.seasonActivity.entity.SeasonActivityGuildGradeEntity;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.Map;

public class SeasonActivityGuildGradeData {
    private static final String redisKey = "SEASON_ACTIVITY_GUILD_GRADE";
    //数据存一年，一年开24期，数据量并不是很大
    private static int REDIS_DATA_EXPIRE_TIME = 3600 * 24 *360;
    private int termId;
    private String guildId = "";
    private int level = 1;
    private int exp = 0;
    private boolean isReward;

    public SeasonActivityGuildGradeData(){

    }

    public SeasonActivityGuildGradeData(String guildId, int termId){
        this.guildId = guildId;
        this.termId = termId;
    }

    public SeasonActivityGuildGradeData(SeasonActivityGuildGradeEntity entity){
        if(entity != null){
            this.termId = entity.getTermId();
            this.guildId = entity.getGuildId();
            this.level = entity.getLevel();
            this.exp = entity.getExp();
            this.isReward = entity.isReward();
        }
    }

    public int getTermId() {
        return termId;
    }

    public String getGuildId() {
        return guildId;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public boolean isReward() {
        return isReward;
    }

    public void setReward(boolean reward) {
        isReward = reward;
    }

    /**
     * 加经验
     * @param addExp 经验增加值
     */
    public void addExp(int addExp){
        //加经验
        this.exp += addExp;
        //算等级
        this.level = calLevel();
        //入库
        saveRedis();
    }

    //算等级
    public int calLevel(){
        //初始等级为1
        int level = 1;
        //找到当前配置
        ConfigIterator<SeasonGradeLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonGradeLevelCfg.class);
        for(SeasonGradeLevelCfg cfg : iterator){
            if(this.exp >= cfg.getLevelUpExp() && cfg.getGradeLevel() > level){
                level = cfg.getGradeLevel();
            }
        }
        //返回等级
        return level;
    }

    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("termId", this.termId);
        obj.put("guildId", this.guildId);
        obj.put("level", this.level);
        obj.put("exp", this.exp);
        obj.put("isReward", this.isReward);
        return obj.toJSONString();
    }

    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.termId = obj.getIntValue("termId");
        this.guildId = obj.getString("guildId");
        this.level = obj.getIntValue("level");
        this.exp = obj.getIntValue("exp");
        this.isReward = obj.getBooleanValue("isReward");
    }

    public String getRedisKey(){
        return redisKey + ":" + termId;
    }

    public void saveRedis(){
        ActivityGlobalRedis.getInstance().hset(getRedisKey(), this.guildId, this.serializ(), REDIS_DATA_EXPIRE_TIME);
    }

    public static Map<String, SeasonActivityGuildGradeData> loadAll(int termId){
        String key = redisKey + ":" + termId;
        Map<String, SeasonActivityGuildGradeData> rlt = new HashMap<>();
        Map<String,String> map = ActivityGlobalRedis.getInstance().hgetAll(key);
        for(Map.Entry<String, String> entry : map.entrySet()){
            String value = entry.getValue();
            if(HawkOSOperator.isEmptyString(value)){
                continue;
            }
            SeasonActivityGuildGradeData guildGradeData = new SeasonActivityGuildGradeData();
            guildGradeData.mergeFrom(value);
            rlt.put(guildGradeData.getGuildId(), guildGradeData);
        }
        return rlt;
    }

    public static SeasonActivityGuildGradeData loadByGuildId(int termId, String guildId){
        String key = redisKey + ":" + termId;
        String guildStr = ActivityGlobalRedis.getInstance().hget(key, guildId);
        if(HawkOSOperator.isEmptyString(guildStr)){
            return null;
        }
        SeasonActivityGuildGradeData guild = new SeasonActivityGuildGradeData();
        guild.mergeFrom(guildStr);
        return guild;
    }
}
