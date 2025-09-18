package com.hawk.activity.type.impl.newFirstRecharge.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 新首充时间配置
 */
@HawkConfigManager.XmlResource(file = "activity/new_first_recharge/%s/new_first_recharge_time.xml", autoLoad=false, loadParams="312")
public class NewFirstRechargeTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
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

    private long showTimeValue;
    private long startTimeValue;
    private long endTimeValue;
    private long hiddenTimeValue;

    public NewFirstRechargeTimeCfg(){
        termId = 0;
        showTime = "";
        startTime = "";
        endTime = "";
        hiddenTime = "";
    }

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
        return true;
    }

    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }
}
