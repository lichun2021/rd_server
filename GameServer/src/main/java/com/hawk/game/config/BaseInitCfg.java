package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 创建新号初始化数据
 * 
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/base_initialization.xml")
public class BaseInitCfg extends HawkConfigBase {
    protected final int buildId;
    protected final int type;
    protected final int posX;
    protected final int posY;
    protected final String index;
    protected final int trigTask;

	public int getBuildId() {
        return buildId;
    }

    public int getType() {
        return type;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public String getIndex() {
        return index;
    }
    
    public boolean needTrigTask() {
		return trigTask > 0;
	}

    public BaseInitCfg() {
        buildId = 0;
        type = 0;
        posX = 0;
        posY = 0;
        index = "1";
        trigTask = 0;
    }

    @Override
    protected boolean checkValid() {
        if (HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildId) == null) {
            return false;
        }
        return true;
    }
}
