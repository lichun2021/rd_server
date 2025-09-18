package com.hawk.activity.type.impl.seasonActivity.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
 * 赛事活动排名段位得分表
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_grade_point.xml")
public class SeasonGradePointCfg extends HawkConfigBase {
    @Id
    private final int id;

    //排名配置
    private final String range;

    //增加点数
    private final int point;

    //赛事类型
    private final int match;
    
    //分区ID
    private final int zone;

    //最小排名
    private int min;

    //最大排名
    private int max;

    public SeasonGradePointCfg(){
        id = 0;
        range = "";
        point = 0;
        match = 0;
        zone = 0;
    }

    /**
     * 解析
     * @return
     */
    @Override
    protected boolean assemble() {
        try {
            String [] rangArr = range.split("_");
            //如果只配了一个值则最大最小值一样
            if(rangArr.length == 1){
                min = Integer.parseInt(rangArr[0]);
                max = Integer.parseInt(rangArr[0]);
            }else {
                min = Integer.parseInt(rangArr[0]);
                max = Integer.parseInt(rangArr[1]);
            }
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getMatch() {
        return match;
    }

    public int getPoint() {
        return point;
    }
    
    public int getZone() {
		return zone;
	}
}
