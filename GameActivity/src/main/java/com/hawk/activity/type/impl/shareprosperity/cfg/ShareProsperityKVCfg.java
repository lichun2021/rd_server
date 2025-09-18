package com.hawk.activity.type.impl.shareprosperity.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/share_prosperity/%s/share_prosperity_const.xml", autoLoad=false, loadParams="376")
public class ShareProsperityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间；单位：秒 */
    private final int serverDelay;
    
    /** 老角色基地等级要求 */
    private final int baseLimit;
    
    /** 新角色和老角色注册时间的时间间隔（天） */
    private final int timeLimit;
    
    /** 活动触发后持续x天 */
    private final int lastTime;
    
    /** 触发基地等级 */
    private final int openLimit;
    
    /** 活动触发间隔（秒） */
    private final int intervalTime;
    
    /** 新服创建角色后，在新服持续时间内x天内可触发该活动 */
    private final int openDay;
    
    /** 大于x天判断为老服 */
    private final int OldOrNewServer;
    
    /** 返利上限 */
    private final String rewardLimit;
    
    /** 开服时间在此时间后的区服可见此活动  */
    private final String openTimeLimit;
    
    /** 金条充值返利万分比：专服_非专服  */
    private final String payBack;
    
    /** 0=都开,1=专服开，2=非专服开 */
    private final int specialServerOpen;
    
    private RewardItem.Builder rewardLimitBuilder;
    private long openTimeLimitValue;
    
    private int commServerRatio, specialServerRatio;
    private static ShareProsperityKVCfg instance;

    public ShareProsperityKVCfg(){
    	this.serverDelay = 0;
    	this.baseLimit = 0;
    	this.timeLimit = 0;
    	this.lastTime = 0;
        this.openLimit = 0;
        this.intervalTime = 0;
        this.openDay = 0;
        this.OldOrNewServer = 0;
        this.rewardLimit = "";
        this.openTimeLimit = "";
        this.payBack = "";
        this.specialServerOpen = 0;
    }
    
    public static ShareProsperityKVCfg getInstance() {
    	return instance;
    } 
    
    @Override
    protected boolean assemble() {
    	if (HawkOSOperator.isEmptyString(openTimeLimit)) {
    		return false;
    	}
    	if (HawkOSOperator.isEmptyString(payBack)) {
    		return false;
    	}
    	openTimeLimitValue = HawkTime.parseTime(openTimeLimit);
    	rewardLimitBuilder = RewardHelper.toRewardItem(rewardLimit);
    	if (rewardLimitBuilder == null) {
    		return false;
    	}
    	
    	String[] strs = payBack.split("_");
    	specialServerRatio = Integer.parseInt(strs[0]);
    	commServerRatio = Integer.parseInt(strs[1]);
    	
    	instance = this;
        return true;
    }

    public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getBaseLimit() {
		return baseLimit;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public int getLastTime() {
		return lastTime;
	}
	
	public long getLastTimeLong() {
		return HawkTime.DAY_MILLI_SECONDS * lastTime;
	}

	public int getOpenLimit() {
		return openLimit;
	}

	public long getIntervalTime() {
		return intervalTime * 1000L;
	}

	public int getOpenDay() {
		return openDay;
	}

	public int getOldOrNewServer() {
		return OldOrNewServer;
	}

	public String getRewardLimit() {
		return rewardLimit;
	}

	public String getOpenTimeLimit() {
		return openTimeLimit;
	}

	public RewardItem.Builder getRewardLimitBuilder() {
		return rewardLimitBuilder;
	}
	
	public long getRewardLimitCount() {
		return rewardLimitBuilder.getItemCount();
	}

	public long getOpenTimeLimitValue() {
		return openTimeLimitValue;
	}
	
	public String getPayBack() {
		return payBack;
	}

	public int getRebateRatio(boolean specialServer) {
		return specialServer ? specialServerRatio : commServerRatio;
	}

	public int getSpecialServerOpen() {
		return specialServerOpen;
	}
	
}