package com.hawk.activity.type.impl.luckGetGold.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/luck_get_gold/luck_get_gold_num.xml")
public class LuckGetGoldNumCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int divide;
    private final String range;
    private final int upWeight;
    private final int weight;


    public LuckGetGoldNumCfg(){
        this.id = 0;
        this.divide = 0;
        this.range = "";
        this.upWeight = 0;
        this.weight = 0;
    }

    public int getId(){
        return this.id;
    }

    public int getDivide(){
        return this.divide;
    }

    public String getRange(){
        return this.range;
    }

    public int getUpWeight(){
        return this.upWeight;
    }

    public int getWeight(){
        return this.weight;
    }

}