package com.hawk.activity.type.impl.fristRechagerThree.cfg;


import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 新首充活动总配置
 */
@HawkConfigManager.KVResource(file = "activity/first_recharge_three/first_recharge_three_cfg.xml")
public class FirstRechargeThreeKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;


    private final String serverOpenTime;
    private final String serverEndOpenTime;

    
    
    
    private long serverOpenTimeValue = 0;
    private long serverEndOpenTimeValue = 0;
    
    
    
    public FirstRechargeThreeKVCfg(){
        this.serverDelay = 0;
        this.serverOpenTime = "";
        this.serverEndOpenTime = "";
    }


    @Override
    protected boolean assemble() {
        this.serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
        this.serverEndOpenTimeValue = HawkTime.parseTime(serverEndOpenTime);
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public long getServerOpenTimeValue() {
		return serverOpenTimeValue;
	}
    
    public long getServerEndOpenTimeValue() {
		return serverEndOpenTimeValue;
	}
    
    
}
