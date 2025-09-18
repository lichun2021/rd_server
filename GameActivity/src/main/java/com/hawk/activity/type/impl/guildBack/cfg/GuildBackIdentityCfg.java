package com.hawk.activity.type.impl.guildBack.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/guild_back/guild_back_identity.xml")
public class GuildBackIdentityCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int type;
    private final int goldRatio;
    private final int vitRatio;
    private final int dayDivideStaminaTimesMax;
    private final int dayDivideStaminaCoinsValueMax;
    private final int dayDivideStaminaVitValueMax;

    public GuildBackIdentityCfg(){
        this.id = 0;
        this.type = 0;
        this.goldRatio = 0;
        this.vitRatio = 0;
        this.dayDivideStaminaTimesMax = 0;
        this.dayDivideStaminaCoinsValueMax = 0;
        this.dayDivideStaminaVitValueMax = 0;
    }

    public int getId(){
        return this.id;
    }

    public int getGoldRatio(){
        return this.goldRatio;
    }

    public int getVitRatio(){
        return this.vitRatio;
    }

    public int getDayDivideStaminaTimesMax() {
        return dayDivideStaminaTimesMax;
    }

    public int getDayDivideStaminaCoinsValueMax() {
        return dayDivideStaminaCoinsValueMax;
    }

    public int getDayDivideStaminaVitValueMax() {
        return dayDivideStaminaVitValueMax;
    }
}