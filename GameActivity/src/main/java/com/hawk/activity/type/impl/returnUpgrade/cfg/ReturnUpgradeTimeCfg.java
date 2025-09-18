package com.hawk.activity.type.impl.returnUpgrade.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.XmlResource(file = "activity/return_upgrade/return_upgrade_time.xml")
public class ReturnUpgradeTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
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

    private final String stopTrigger;

    /** 预览时间戳*/
    private long showTimeValue;

    /** 开启时间戳*/
    private long startTimeValue;

    /** 结束时间戳*/
    private long endTimeValue;

    /** 消失时间戳*/
    private long hiddenTimeValue;

    private long stopTriggerValue;

    public ReturnUpgradeTimeCfg(){
        termId = 0;
        showTime = "";
        startTime = "";
        endTime = "";
        hiddenTime = "";
        stopTrigger = "";
    }

    @Override
    protected boolean assemble() {
        showTimeValue = HawkTime.parseTime(showTime);
        startTimeValue = HawkTime.parseTime(startTime);
        endTimeValue = HawkTime.parseTime(endTime);
        hiddenTimeValue = HawkTime.parseTime(hiddenTime);
        stopTriggerValue = HawkTime.parseTime(stopTrigger);
        return true;
    }

    @Override
    public int getTermId() {
        return termId;
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

    public long getStopTriggerValue() {
        return stopTriggerValue;
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

    public String getStopTrigger(){
    return stopTrigger;
    }


    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }
}