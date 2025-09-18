package com.hawk.activity.type.impl.redEnvelope.cfg;

import java.util.Date;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/***
 * 抢红包信息
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/redPacket_achieve/redPacket_achieve.xml")
public class RedEnvelopeAchieveCfg extends HawkConfigBase {

	@Id
	private final int stageID; //红包ID
	
	/** 对应reward.xml表id **/
	private final int rewardsID;
	
	/** 红包数量 **/
	private final int num;
	
	/** 中了此道具广播 **/
	private final String broadcastItemID;
	
	/** 推送时间 **/
	private final String showTime;
	
	/** 开始时间 **/
	private final String startTime;
	
	private final String endTime;
	
	private long show;
	
	private long start;
	
	private long end;
	
	private final int day;
	
	public RedEnvelopeAchieveCfg(){
		this.stageID = 0;
		this.rewardsID = 0;
		this.num = 0;
		this.broadcastItemID = "";
		this.showTime = "";
		this.startTime = "";
		this.endTime = "";
		this.day = 0;
	}
	
	@Override
	protected boolean assemble() {
		show = changeTime(showTime);
		start = changeTime(startTime);
		end = changeTime(endTime);
		return true;
	}
	
	private long changeTime(String timeStr){
		//HawkTime.parseTime(timeStr, "HH:mm:ss") + TimeZone.getDefault().getRawOffset()
		String[] arrs = timeStr.split(":");
		return (Integer.parseInt(arrs[0]) * 3600 + Integer.parseInt(arrs[1]) * 60 + Integer.parseInt(arrs[2])) * 1000L;
	}

	public int getStageID() {
		return stageID;
	}

	public int getRewardsID() {
		return rewardsID;
	}

	public int getNum() {
		return num;
	}

	public String getBroadcastItemID() {
		return broadcastItemID;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public long getShow(int termId) {
		return getPrefixTime(termId, show);
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
		RedEnvelopeTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RedEnvelopeTimeCfg.class, termId);
		long st = HawkTime.getAM0Date(new Date(cfg.getStartTimeValue())).getTime();
		return st + (86400000L * (day - 1)) + hourTime;
	}
}
