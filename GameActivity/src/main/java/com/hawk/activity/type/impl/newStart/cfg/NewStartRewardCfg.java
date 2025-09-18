package com.hawk.activity.type.impl.newStart.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/new_start/new_start_reward.xml")
public class NewStartRewardCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int day;
    private final String range;
    private final int type;
    private final String reward;
    private final String reward1;
    private final String reward2;
    private final String reward3;
    private final String reward4;
    private final String reward5;
    private final String reward6;
    private final String reward7;


    public NewStartRewardCfg(){
        this.id = 0;
        this.day = 0;
        this.range = "";
        this.type = 0;
        this.reward = "";
        this.reward1 = "";
        this.reward2 = "";
        this.reward3 = "";
        this.reward4 = "";
        this.reward5 = "";
        this.reward6 = "";
        this.reward7 = "";
    }

    public int getId(){
        return this.id;
    }

    public int getDay(){
        return this.day;
    }

    public String getRange(){
        return this.range;
    }

    public int getType(){
        return this.type;
    }

    public String getReward(int getCount){
        switch (getCount){
            case 1:return reward1;
            case 2:return reward2;
            case 3:return reward3;
            case 4:return reward4;
            case 5:return reward5;
            case 6:return reward6;
            case 7:return reward7;
        }
        return "";
    }

    public String getReward1() {
        return reward1;
    }

    public String getReward2() {
        return reward2;
    }

    public String getReward3() {
        return reward3;
    }

    public String getReward4() {
        return reward4;
    }

    public String getReward5() {
        return reward5;
    }

    public String getReward6() {
        return reward6;
    }

    public String getReward7() {
        return reward7;
    }
}