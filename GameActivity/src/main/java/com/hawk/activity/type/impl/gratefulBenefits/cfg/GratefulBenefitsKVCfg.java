package com.hawk.activity.type.impl.gratefulBenefits.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 感恩福利配置
 */
@HawkConfigManager.KVResource(file = "activity/grateful_benefits/grateful_benefits_cfg.xml")
public class GratefulBenefitsKVCfg extends HawkConfigBase {
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;
    /**
     * 冷却时间(小时)
     */
    private final int cdTime;
    /**
     * 至多邀请玩家
     */
    private final int maxPlayer;
    /**
     * 基础金币数
     */
    private final int baseGold;
    /**
     * 玩家邀请权重
     */
    private final float playerWeight;
    /**
     * 玩家签到权重
     */
    private final float punchWeight;
    /**
     * 玩家可分享次数
     */
    private final int shareCount;

    /**
     * 分享奖励
     */
    private final String shareReward;

    /**
     * 玩家注册截止天数
     */
    private final String registrationline;

    private long registrationlineValue;

    public GratefulBenefitsKVCfg() {
        serverDelay = 0;
        cdTime = 0;
        maxPlayer = 0;
        baseGold = 0;
        playerWeight = 0;
        punchWeight = 0;
        shareCount = 0;
        registrationline = "";
        shareReward = "";
    }

    @Override
    protected boolean assemble() {
        registrationlineValue = HawkTime.parseTime(registrationline);
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    public int getCdTime() {
        return cdTime;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public int getBaseGold() {
        return baseGold;
    }

    public float getPlayerWeight() {
        return playerWeight;
    }

    public float getPunchWeight() {
        return punchWeight;
    }

    public int getShareCount() {
        return shareCount;
    }

    public String getRegistrationDeadline() {
        return registrationline;
    }

    public long getRegistrationlineValue() {
        return registrationlineValue;
    }

    public String getShareReward() {
        return shareReward;
    }
}
