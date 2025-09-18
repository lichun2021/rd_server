package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.XmlResource(file = "xml/xhjz_league_time.xml")
public class XHJZSeasonTimeCfg extends HawkConfigBase {
    /** 活动期数*/
    @Id
    private final int termId;
    /** 赛季开始时间*/
    private final String seasonStartTime;
    /** 入围赛结束时间*/
    private final String qualifierEndTime;
    /** 排名赛结束时间*/
    private final String rankingEndTime;
    /** 赛季结束时间*/
    private final String seasonEndTime;

    private final int qualifierTermId;
    private final int rankingTermId;
    private final int endTermId;

    /** 赛季开始时间戳*/
    private long seasonStartTimeValue;
    /** 入围赛结束时间戳*/
    private long qualifierEndTimeValue;
    /** 排名赛结束时间戳*/
    private long rankingEndTimeValue;
    /** 赛季结束时间戳*/
    private long seasonEndTimeValue;

    public XHJZSeasonTimeCfg(){
        termId = 0;
        seasonStartTime = "";
        qualifierEndTime = "";
        rankingEndTime = "";
        seasonEndTime = "";
        qualifierTermId = 0;
        rankingTermId = 0;
        endTermId = 0;
    }

    @Override
    protected boolean assemble() {
        seasonStartTimeValue = HawkTime.parseTime(seasonStartTime);
        qualifierEndTimeValue = HawkTime.parseTime(qualifierEndTime);
        rankingEndTimeValue = HawkTime.parseTime(rankingEndTime);
        seasonEndTimeValue = HawkTime.parseTime(seasonEndTime);
        return true;
    }

    public int getTermId() {
        return termId;
    }

    public int getQualifierTermId() {
        return qualifierTermId;
    }

    public int getRankingTermId() {
        return rankingTermId;
    }

    public int getEndTermId() {
        return endTermId;
    }

    public long getSeasonStartTimeValue() {
        return seasonStartTimeValue;
    }

    public long getQualifierEndTimeValue() {
        return qualifierEndTimeValue;
    }

    public long getRankingEndTimeValue() {
        return rankingEndTimeValue;
    }

    public long getSeasonEndTimeValue() {
        return seasonEndTimeValue;
    }
}
