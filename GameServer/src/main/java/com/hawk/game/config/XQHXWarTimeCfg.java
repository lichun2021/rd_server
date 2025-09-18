package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.Date;

@HawkConfigManager.XmlResource(file = "xml/xqhx_war_time.xml")
public class XQHXWarTimeCfg extends HawkConfigBase {
    /** 活动期数*/
    @Id
    private final int termId;
    /** 报名时间*/
    private final String signupTime;
    /** 匹配时间*/
    private final String matchTime;
    /** 战斗时间*/
    private final String battleTime;
    /** 结算时间*/
    private final String settleTime;
    /** 结束时间*/
    private final String endTime;
    /** 报名时间戳*/
    private long signupTimeValue;
    /** 匹配等待时间戳*/
    private long matchWaitTimeValue;
    /** 匹配时间戳*/
    private long matchTimeValue;
    /** 匹配结束时间戳*/
    private long matchEndTimeValue;
    /** 战斗时间戳*/
    private long battleTimeValue;
    /** 结算时间戳*/
    private long settleTimeValue;
    /** 结束时间戳*/
    private long endTimeValue;
    /** 战斗日零点时间戳*/
    private long battleTimeZeroValue;

    public XQHXWarTimeCfg(){
        termId = 0;
        signupTime = "";
        matchTime = "";
        battleTime = "";
        settleTime = "";
        endTime = "";
    }

    @Override
    protected boolean assemble() {
        signupTimeValue = HawkTime.parseTime(signupTime);
        matchWaitTimeValue = HawkTime.parseTime(matchTime);
        battleTimeValue = HawkTime.parseTime(battleTime);
        settleTimeValue = HawkTime.parseTime(settleTime);
        endTimeValue = HawkTime.parseTime(endTime);
        long matchDur = (battleTimeValue - matchWaitTimeValue)/4;
        matchTimeValue = matchWaitTimeValue + matchDur;
        matchEndTimeValue = matchTimeValue + matchDur;
        battleTimeZeroValue = HawkTime.getAM0Date(new Date(battleTimeValue)).getTime();
        return true;
    }

    public int getTermId() {
        return termId;
    }

    public long getSignupTime() {
        return signupTimeValue;
    }

    public long getMatchWaitTime() {
        return matchWaitTimeValue;
    }

    public long getMatchTime() {
        return matchTimeValue;
    }

    public long getMatchEndTime() {
        return matchEndTimeValue;
    }

    public long getBattleTime() {
        return battleTimeValue;
    }

    public long getSettleTime() {
        return settleTimeValue;
    }

    public long getEndTime() {
        return endTimeValue;
    }

    public long getBattleTimeZero() {
        return battleTimeZeroValue;
    }
}
