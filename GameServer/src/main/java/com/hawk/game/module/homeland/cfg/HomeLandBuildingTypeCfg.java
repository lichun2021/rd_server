package com.hawk.game.module.homeland.cfg;

import com.hawk.game.config.ItemCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 家园建筑类型配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_build_type.xml")
public class HomeLandBuildingTypeCfg extends HawkConfigBase {
    @Id
    protected final int buildType;

    protected final String cover;

    protected final int quality;

    protected final int maxNumber;

    protected final int maxSetNumber;

    protected int[] coverArea;
    // 建筑作用号

    public HomeLandBuildingTypeCfg() {
        quality = 0;
        maxNumber = 0;
        maxSetNumber = 0;
        cover = "";
        buildType = 0;
        coverArea = new int[2];
    }

    public int getBuildType() {
        return buildType;
    }

    @Override
    protected boolean assemble() {
        if (!HawkOSOperator.isEmptyString(cover)) {
            String[] covers = cover.split(",");
            coverArea[0] = Integer.parseInt(covers[0]);
            coverArea[1] = Integer.parseInt(covers[1]);
        }
        return true;
    }


    public int getWidth() {
        return coverArea[0];
    }

    public int getHeight() {
        return coverArea[1];
    }

    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public int getMaxSetNumber() {
        return maxSetNumber;
    }
}
