package com.hawk.activity.type.impl.luckyBox.cfg;

import com.hawk.activity.config.IActivityTimeCfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


/**
 * 装扮投放系列活动三:重燃战火
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "activity/lucky_box/lucky_box_time.xml")
public class LuckyBoxTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 预览时间*/
	private final String showTime; 
	
	/** 开启时间*/
	private final String startTime;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 消失时间*/
	private final String hiddenTime;
	
	/** 商店兑换时间*/
	private final String exchangestartTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	private long exchangestartTimeValue;
	
	public LuckyBoxTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		exchangestartTime = "";
	}
	@Override
	public int getTermId() {
		return termId;
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

	public String getHiddenTime() {
		return hiddenTime;
	}
	
	public long getExchangestartTimeValue() {
		return exchangestartTimeValue;
	}

	@Override
	public long getShowTimeValue() {
		return showTimeValue;
	}
	
	@Override
	public long getStartTimeValue() {
		return startTimeValue;
	}
	
	@Override
	public long getEndTimeValue() {
		return endTimeValue;
	}
	
	@Override
	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}

	@Override
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		exchangestartTimeValue = HawkTime.parseTime(exchangestartTime);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		List<LuckyBoxTimeCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(LuckyBoxTimeCfg.class).toList();
		for(LuckyBoxTimeCfg cfg :cfgs){
			if(cfg.getExchangestartTimeValue() > cfg.getEndTimeValue()){
				throw new RuntimeException("LuckyBoxTimeCfg check valid failed,exchangestartTime err");
			}
		}
		return checkTimeCfgValid(this.getClass());
	}
}
