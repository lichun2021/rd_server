package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import org.hawk.os.HawkOSOperator;

public class YQZZKickoutData implements IYQZZData {

    private static final String redisKey = "YQZZ_ACTIVITY_KICKOUT_DATA";

    private int termId;

    private String kickoutServer;

    private long kickoutTime;

    public YQZZKickoutData(){
        this.termId = 0;
        this.kickoutServer = "";
        this.kickoutTime = 0;
    }

    public YQZZKickoutData(int termId, String kickoutServer,long kickoutTime){
        this.termId = termId;
        this.kickoutServer = kickoutServer;
        this.kickoutTime = kickoutTime;
    }

    public boolean kickoutFinish(){
        if(!HawkOSOperator.isEmptyString(this.kickoutServer) &&
                this.kickoutTime >0 ){
            return true;
        }
        return false;
    }

    public static YQZZKickoutData loadData(int termId) {
        String key = redisKey + ":" + termId;
        String dataStr = RedisProxy.getInstance().getRedisSession()
                .getString(key, YQZZConst.REDIS_DATA_EXPIRE_TIME);
        StatisManager.getInstance().incRedisKey(redisKey);
        if (HawkOSOperator.isEmptyString(dataStr)) {
            return null;
        }
        YQZZKickoutData data = new YQZZKickoutData();
        data.mergeFrom(dataStr);
        return data;
    }

    public int getTermId() {
        return termId;
    }

    public String getKickoutServer() {
        return kickoutServer;
    }

    public long getKickoutTime() {
        return kickoutTime;
    }

    @Override
    public void saveRedis() {
        String key = redisKey  + ":" + termId;
        RedisProxy.getInstance().getRedisSession()
                .setString(key, this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("termId", this.termId);
        obj.put("kickoutServer", this.kickoutServer);
        obj.put("kickoutTime", this.kickoutTime);
        return obj.toString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if (HawkOSOperator.isEmptyString(serialiedStr)) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        if(obj.containsKey("termId")){
            this.termId = obj.getIntValue("termId");
        }
        if(obj.containsKey("kickoutServer")){
            this.kickoutServer = obj.getString("kickoutServer");
        }
        if(obj.containsKey("kickoutTime")){
            this.kickoutTime = obj.getLongValue("kickoutTime");
        }
    }
}
