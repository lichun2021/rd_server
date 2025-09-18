package com.hawk.activity.type.impl.newFirstRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 新首充活动总配置
 */
@HawkConfigManager.KVResource(file = "activity/new_first_recharge/%s/new_first_recharge_cfg.xml", autoLoad=false, loadParams="312")
public class NewFirstRechargeKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;

    /**
     * 等级限制
     */
    private final int levelLimit;

    /**
     * 推荐的礼包id
     */
    private final String recommendId;

    /**
     * 弹窗等级
     */
    private final String ejectLevel;

    private final String serverOpen;

    private long serverOpenValue = 0;
    private final long resetTime;
    public NewFirstRechargeKVCfg(){
        this.serverDelay = 0;
        this.levelLimit = 0;
        this.recommendId = "";
        this.ejectLevel = "";
        this.serverOpen = "";
        resetTime = 0;
    }


    @Override
    protected boolean assemble() {
        this.serverOpenValue = HawkTime.parseTime(serverOpen);
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public long getServerOpenValue() {
        return serverOpenValue;
    }

    public int getLevelLimit() {
        return levelLimit;
    }


	public long getResetTime() {
		return resetTime*1000L;
	}
    
    
}
