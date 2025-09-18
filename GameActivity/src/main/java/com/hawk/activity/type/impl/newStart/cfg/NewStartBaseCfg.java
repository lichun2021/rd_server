package com.hawk.activity.type.impl.newStart.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/new_start/new_start_base.xml")
public class NewStartBaseCfg extends HawkConfigBase{
    private final int openLimit;
    private final int intervalTime;
    private final int vipLimit;
    private final int lastTime;
    private final int serverDelay;
    private final int baseLimit;
    private final int timeLimit;
    private final int defaultHero;
    private final int defaultMecha;
    private final int defaultTech;

    public NewStartBaseCfg(){
        this.openLimit = 0;
        this.intervalTime = 0;
        this.vipLimit = 0;
        this.lastTime = 0;
        this.serverDelay = 0;
        this.baseLimit = 0;
        this.timeLimit = 0;
        this.defaultHero = 0;
        this.defaultMecha = 0;
        this.defaultTech = 0;
    }

    public int getOpenLimit(){
        return this.openLimit;
    }

    public long getIntervalTime(){
        return this.intervalTime * 1000L;
    }

    public int getVipLimit(){
        return this.vipLimit;
    }

    public long getLastTime(){
        return this.lastTime * 1000L;
    }

    public long getServerDelay(){
        return this.serverDelay * 1000L;
    }

    public int getBaseLimit(){
        return this.baseLimit;
    }

    public int getTimeLimit(){
        return this.timeLimit;
    }

    public int getDefaultHero() {
        return defaultHero;
    }

    public int getDefaultMecha() {
        return defaultMecha;
    }

    public int getDefaultTech() {
        return defaultTech;
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