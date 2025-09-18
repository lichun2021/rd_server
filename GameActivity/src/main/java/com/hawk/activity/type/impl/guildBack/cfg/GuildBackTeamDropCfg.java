package com.hawk.activity.type.impl.guildBack.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/guild_back/guild_back_drop.xml")
public class GuildBackTeamDropCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int count;
    private final int probability;
    private final String rewards;


    public GuildBackTeamDropCfg(){
        this.id = 0;
        this.count = 0;
        this.probability = 0;
        this.rewards = "";
    }

    public int getId(){
        return this.id;
    }

    public int getCount(){
        return this.count;
    }

    public int getProbability(){
        return this.probability;
    }

    public String getRewards(){
        return this.rewards;
    }

}