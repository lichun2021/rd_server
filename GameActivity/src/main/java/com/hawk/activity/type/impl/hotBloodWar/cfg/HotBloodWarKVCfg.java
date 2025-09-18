package com.hawk.activity.type.impl.hotBloodWar.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.collect.ImmutableList;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 
 * @author che
 */
@HawkConfigManager.KVResource(file = "activity/hot_blood_war/%s/hot_blood_war_cfg.xml", autoLoad=false, loadParams="378")
public class  HotBloodWarKVCfg extends HawkConfigBase {
	
    /**
     * 服务器开服延时开启活动时间；单位:秒
     */
    private final int serverDelay;
	
	/** # 恢复时间系数，万分比，10000表示不变*/
	private final int recover;
	/** 单抽消耗*/
	private final String endTime;
	/** 10抽消耗*/
	private final String startTime;
	
	private final String generalSpeedItems;
	
	private List<Integer> speedItemList;
	
	private long endTimeValue;
	private long startTimeValue;

	public HotBloodWarKVCfg(){
		serverDelay = 0;
		recover = 0;
		endTime = "";
		startTime = "";
		generalSpeedItems = "";
	}
	
	
	
	@Override
	protected boolean assemble() {
		List<Integer> speedItemListTemp = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.generalSpeedItems)){
			SerializeHelper.stringToList(Integer.class, this.generalSpeedItems, SerializeHelper.BETWEEN_ITEMS,speedItemListTemp);
		}
		this.speedItemList = ImmutableList.copyOf(speedItemListTemp);
		
		
		if(!HawkOSOperator.isEmptyString(this.endTime)){
			this.endTimeValue = HawkTime.parseTime(this.endTime);
		}
		if(!HawkOSOperator.isEmptyString(this.startTime)){
			this.startTimeValue = HawkTime.parseTime(this.startTime);
		}
		return super.assemble();
	}
	public int getServerDelay() {
		return serverDelay *  1000;
	}
	
	
	public int getRecover() {
		return recover;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public String getEndTime() {
		return endTime;
	}
	
	public List<Integer> getSpeedItemList() {
		return speedItemList;
	}
	
	
	public long getEndTimeValue() {
		return endTimeValue;
	}
	
	public long getStartTimeValue() {
		return startTimeValue;
	}
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}


	
	
		
}