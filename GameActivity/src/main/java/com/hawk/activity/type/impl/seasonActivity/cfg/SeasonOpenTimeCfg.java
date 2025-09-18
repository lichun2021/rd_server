package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.Map;

/**
 * 赛事活动时间配置
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_open_time.xml")
public class SeasonOpenTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
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

    /** 赛事类型对应期数 */
    private final String matchSeason;

    /** 消失时间*/
    private final String seperateTime;

    /** 赛事类型对应期数 */
    private final String mergerTime;

    private final int showServerLevel;

    /** 预览时间戳*/
    private long showTimeValue;

    /** 开启时间戳*/
    private long startTimeValue;

    /** 结束时间戳*/
    private long endTimeValue;

    /** 消失时间戳*/
    private long hiddenTimeValue;

    /**
     * 赛事类型对应期数
     * 主键：赛事类型 值：赛事期数
     */
    private Map<Integer, Integer> matchSeasonMap = new HashMap<>();

    /**
     * 配置构造函数
     */
    public SeasonOpenTimeCfg(){
        termId = 0;
        showTime = "";
        startTime = "";
        endTime = "";
        hiddenTime = "";
        matchSeason = "";
        seperateTime = "";
        mergerTime = "";
        showServerLevel = 0;
    }

    /**
     * 解析配置
     * @return 解析是否成功
     */
    @Override
    protected boolean assemble() {
        showTimeValue = HawkTime.parseTime(showTime);
        startTimeValue = HawkTime.parseTime(startTime);
        endTimeValue = HawkTime.parseTime(endTime);
        hiddenTimeValue = HawkTime.parseTime(hiddenTime);
        if (!HawkOSOperator.isEmptyString(matchSeason)) {
            Map<Integer, Integer> tmp = new HashMap<>();
            String[] timeStrs = matchSeason.split(";");
            for (String timeStr : timeStrs) {
                String[] strs = timeStr.split(",");
                int matchType = Integer.valueOf(strs[0]);
                int termId = Integer.valueOf(strs[1]);
                tmp.put(matchType, termId);
            }
            matchSeasonMap = ImmutableMap.copyOf(tmp);
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

    public String getSeperateTime() {
        return seperateTime;
    }

    public String getMergerTime() {
        return mergerTime;
    }

    public int getShowServerLevel() {
        return showServerLevel;
    }

    /**
     * 通过赛事期数获得赛事类型
     * @param matchType 赛事类型
     * @return 赛事期数
     */
    public int getMatchTermId(int matchType){
        return matchSeasonMap.getOrDefault(matchType, 0);
    }

    /**
     * 检查时间是否配置错误
     * @return
     */
    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }
}
