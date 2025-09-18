package com.hawk.activity.type.impl.seasonActivity.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.Map;

@HawkConfigManager.KVResource(file = "activity/season/season_const.xml")
public class SeasonConstCfg extends HawkConfigBase {
    private final String season_const;

    private Map<Integer, String> timeMap = new HashMap<>();

    public SeasonConstCfg(){
        season_const = "";
    }

    @Override
    protected boolean assemble() {
        try {
            Map<Integer, String> timeMap = new HashMap<>();
            for(String timeStr : season_const.split(";")){
                if(HawkOSOperator.isEmptyString(timeStr)){
                    continue;
                }
                String [] arr = timeStr.split(",");
                if(arr.length !=2){
                    continue;
                }
                timeMap.put(Integer.valueOf(arr[0]), arr[1]);
            }
            this.timeMap = timeMap;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public String getTime(int termId){
        return timeMap.getOrDefault(termId, "");
    }
}
