package com.hawk.activity.type.impl.redPackage.cfg;

import java.util.Date;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/***
 * 抢红包信息
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/red_packet/red_packet_reward.xml")
public class RedPackageRewardCfg extends HawkConfigBase {

	@Id
	private final int id; //红包ID
	
	/** 开始时间 **/
	private final String startTime;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 天数*/
	private final int day;
	
	/** 时间*/
	private final int duration;
	
	private long start;
	
	private long end;
	
	
	public RedPackageRewardCfg(){
		this.id = 0;
		this.startTime = "";
		this.endTime = "";
		this.day = 0;
		this.duration = 10;
	}
	
	@Override
	protected boolean assemble() {
		start = changeTime(startTime);
		end = changeTime(endTime);
		return true;
	}
	
	private long changeTime(String timeStr){
		//HawkTime.parseTime(timeStr, "HH:mm:ss") + TimeZone.getDefault().getRawOffset()
		String[] arrs = timeStr.split(":");
		return (Integer.parseInt(arrs[0]) * 3600 + Integer.parseInt(arrs[1]) * 60 + Integer.parseInt(arrs[2])) * 1000L;
	}

	


	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}


	public long getStart(int termId) {
		return getPrefixTime(termId, start);
	}

	public long getEnd(int termId) {
		return getPrefixTime(termId, end);
	}

	public int getDay() {
		return day;
	}
	
	public long getPrefixTime(int termId, long hourTime){
		RedPackageTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RedPackageTimeCfg.class, termId);
		long st = HawkTime.getAM0Date(new Date(cfg.getStartTimeValue())).getTime();
		return st + (86400000L * (day - 1)) + hourTime;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public int getId() {
		return id;
	}

	public int getDuration() {
		return duration;
	}
	
	
}
