package com.hawk.activity.type.impl.guildBack.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/guild_back/guild_back_partition.xml")
public class GuildBackPartitionCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final float ratio;
    private final String rewards;
    private final int type;
    private final int weight;


    public GuildBackPartitionCfg(){
        this.id = 0;
        this.ratio = 0;
        this.rewards = "";
        this.type = 0;
        this.weight = 0;
    }

    public int getId(){
        return this.id;
    }

    public float getRatio(){
        return this.ratio;
    }

    public String getRewards(){
        return this.rewards;
    }

    public int getType(){
        return this.type;
    }

    public int getWeight(){
        return this.weight;
    }

}