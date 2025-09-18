package com.hawk.activity.type.impl.developFast.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 实力飞升 时间配置
 */
@HawkConfigManager.XmlResource(file = "activity/develop_fast/develop_fast_time.xml")
public class DevelopFastTimeCfg  extends HawkConfigBase implements IActivityTimeCfg {
    /** 活动期数*/
    @Id
    private final int termId;

    /** 预览时间*/
    private final long showTime;

    /** 开启时间*/
    private final long startTime;

    /** 结束时间*/
    private final long endTime;

    /** 消失时间*/
    private final long hiddenTime;

    /**
     * 构造函数
     */
    public DevelopFastTimeCfg() {
        termId = 0;
        showTime = 0;
        startTime = 0;
        endTime = 0;
        hiddenTime = 0;
    }

    /**
     * 获得期数
     * @return 期数
     */
    public int getTermId() {
        return termId;
    }

    /**
     * 展示时间
     * @return 展示时间
     */
    @Override
    public long getShowTimeValue() {
        return showTime * 1000;
    }

    @Override
    public long getStartTimeValue() {
        return startTime * 1000;
    }

    @Override
    public long getEndTimeValue() {
        return endTime * 1000;
    }

    @Override
    public long getHiddenTimeValue() {
        return hiddenTime * 1000;
    }

    @Override
    protected boolean assemble() {
        return true;
    }

    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }
}
