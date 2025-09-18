package com.hawk.activity.type.impl.pddActivity.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

@HawkConfigManager.XmlResource(file = "activity/pdd/pdd_time.xml")
public class PDDTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
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

    private final String serverDownStart;

    private final String serverDownEnd;

    private final int delayTime;

    /** 预览时间戳*/
    private long showTimeValue;

    /** 开启时间戳*/
    private long startTimeValue;

    /** 结束时间戳*/
    private long endTimeValue;

    /** 消失时间戳*/
    private long hiddenTimeValue;

    private long serverDownStartValue;
    private long serverDownEndValue;

    public PDDTimeCfg(){
        termId = 0;
        showTime = "";
        startTime = "";
        endTime = "";
        hiddenTime = "";
        serverDownStart = "";
        serverDownEnd = "";
        delayTime = 0;
    }

    @Override
    protected boolean assemble() {
        showTimeValue = HawkTime.parseTime(showTime);
        startTimeValue = HawkTime.parseTime(startTime);
        endTimeValue = HawkTime.parseTime(endTime);
        hiddenTimeValue = HawkTime.parseTime(hiddenTime);
        if(!HawkOSOperator.isEmptyString(serverDownStart)){
            serverDownStartValue = HawkTime.parseTime(serverDownStart);
        }
        if(!HawkOSOperator.isEmptyString(serverDownEnd)){
            serverDownEndValue = HawkTime.parseTime(serverDownEnd);
        }
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

    public long getServerDownStartValue() {
        return serverDownStartValue;
    }

    public long getServerDownEndValue() {
        return serverDownEndValue;
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

    public String getServerDownStart() {
        return serverDownStart;
    }

    public String getServerDownEnd() {
        return serverDownEnd;
    }

    public long getDelayTime() {
        return delayTime * 1000l;
    }

    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }
}
