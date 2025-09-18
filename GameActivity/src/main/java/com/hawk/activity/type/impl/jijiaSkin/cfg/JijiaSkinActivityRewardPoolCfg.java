package com.hawk.activity.type.impl.jijiaSkin.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/jijia_skin_activity/jijia_skin_activity_reward_pool.xml")
public class JijiaSkinActivityRewardPoolCfg extends HawkConfigBase implements HawkRandObj {
    @Id
    private final int id;
    private final int weight;


    public JijiaSkinActivityRewardPoolCfg(){
        this.id = 0;
        this.weight = 0;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public int getWeight(){
        return this.weight;
    }

}