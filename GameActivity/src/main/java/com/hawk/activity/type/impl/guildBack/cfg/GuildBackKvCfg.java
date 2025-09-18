package com.hawk.activity.type.impl.guildBack.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/guild_back/guild_back_kv.xml")
public class GuildBackKvCfg extends HawkConfigBase{
    private final int activityId;
    private final int serverDelay;
    private final int inviteCount;
    private final int boxMax;
    private final String partitionCost;
    private final int boxRewards;
    private final int luckDrawCD;
    private final int divideJackpotCD;
    private final String valueProp;
    private final int dropMax;
    private final double startCoinsValue;
    private final double startVitValue;
    private final int baseLevel;
    private final int coinMax;
    private final int vitMax;

    public GuildBackKvCfg(){
        this.activityId = 359;
        this.serverDelay = 0;
        this.inviteCount = 0;
        this.boxMax = 0;
        this.partitionCost = "";
        this.boxRewards = 0;
        this.luckDrawCD = 0;
        this.divideJackpotCD = 0;
        this.valueProp = "";
        this.dropMax = 0;
        this.startCoinsValue = 0;
        this.startVitValue = 0;
        this.baseLevel = 0;
        this.coinMax = 0;
        this.vitMax = 0;
    }

    public int getBoxRewards(){
        return this.boxRewards;
    }

    public String getPartitionCost(){
        return this.partitionCost;
    }

    public int getActivityId(){
        return this.activityId;
    }

    public int getBoxMax(){
        return this.boxMax;
    }

    public int getDropMax() {
        return dropMax;
    }

    public int getServerDelay(){
        return this.serverDelay;
    }

    public int getInviteCount(){
        return this.inviteCount;
    }

    public long getDivideJackpotCD() {
        return divideJackpotCD * 1000L;
    }

    public long getLuckDrawCD() {
        return luckDrawCD * 1000L;
    }

    public double getStartCoinsValue() {
        return startCoinsValue;
    }

    public double getStartVitValue() {
        return startVitValue;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public int getCoinMax() {
        return coinMax;
    }

    public int getVitMax() {
        return vitMax;
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