package com.hawk.activity.type.impl.cnyExam.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/cny_exam/cny_exam_kv_cfg.xml")
public class CnyExamKvCfg extends HawkConfigBase{
    private final int activityId;
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;
    private final int scoreItem;


    public CnyExamKvCfg(){
        this.activityId = 0;
        this.serverDelay = 0;
        this.scoreItem = 0;

    }

    public int getActivityId(){
        return this.activityId;
    }

    public long getServerDelay(){
        return this.serverDelay * 1000l;
    }

    public int getScoreItem(){
        return this.scoreItem;
    }

    @Override
    protected boolean assemble() {
        try {
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

}