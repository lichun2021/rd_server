package com.hawk.activity.type.impl.backToNewFly.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/back_to_new_fly/back_to_new_fly_build.xml")
public class BackToNewFlyBuildCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int buildId;
    private final int buildType;
    private final int isDIY;


    public BackToNewFlyBuildCfg(){
        this.id = 0;
        this.buildId = 0;
        this.buildType = 0;
        this.isDIY = 0;
    }

    public int getId() {
        return id;
    }

    public int getBuildId() {
        return buildId;
    }

    public int getBuildType() {
        return buildType;
    }

    public boolean isDIY() {
        return isDIY == 1;
    }

}