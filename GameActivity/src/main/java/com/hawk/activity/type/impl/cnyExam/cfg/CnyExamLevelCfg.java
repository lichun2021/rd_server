package com.hawk.activity.type.impl.cnyExam.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigBase.CombineId;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/cny_exam/cny_exam_level.xml")
@CombineId(fields = {"day", "dayLevel"})
public class CnyExamLevelCfg extends HawkConfigBase{
    @Id
    private final int level;
    private final int day;
    private final int dayLevel;
    private final int score;
    private final String gainItem;
    private final String chooseItem1;
    private final String chooseItem2;
    private final int android;
    private final int ios;

    private List<Reward.RewardItem.Builder> gainList;

    private List<Reward.RewardItem.Builder> chooseList1;

    private List<Reward.RewardItem.Builder> chooseList2;

    private static Map<Integer, CnyExamLevelCfg> payIdToCfg = new HashMap<>();

    public CnyExamLevelCfg(){
        this.level = 0;
        this.score = 0;
        this.day = 0;
        this.gainItem = "";
        this.chooseItem1 = "";
        this.chooseItem2 = "";
        this.android = 0;
        this.ios = 0;
        this.dayLevel = 0;
    }


    @Override
    protected boolean assemble() {
        try {
            //解析奖励
            gainList = RewardHelper.toRewardItemImmutableList(gainItem);
            chooseList1 = RewardHelper.toRewardItemImmutableList(chooseItem1);
            chooseList2 = RewardHelper.toRewardItemImmutableList(chooseItem2);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public static boolean doAssemble() {
        Map<Integer, CnyExamLevelCfg> tmp = new HashMap<>();
        ConfigIterator<CnyExamLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CnyExamLevelCfg.class);
        for(CnyExamLevelCfg cfg : iterator){
            if(cfg.getIos() != 0){
                tmp.put(cfg.getIos(), cfg);
            }
            if(cfg.getAndroid() != 0){
                tmp.put(cfg.getAndroid(), cfg);
            }
        }
        payIdToCfg = tmp;
        return true;
     }

     public static CnyExamLevelCfg getCfgByPayId(int payId){
        return payIdToCfg.get(payId);
     }

    public int getLevel() {
        return level;
    }

    public int getDay() {
        return day;
    }

    public int getScore() {
        return score;
    }

    public String getGainItem() {
        return gainItem;
    }

    public String getChooseItem1() {
        return chooseItem1;
    }

    public String getChooseItem2() {
        return chooseItem2;
    }

    public int getAndroid() {
        return android;
    }

    public int getIos() {
        return ios;
    }

    public List<Reward.RewardItem.Builder> getGainList() {
        return RewardHelper.toRewardItemImmutableList(gainItem);
    }

    public List<Reward.RewardItem.Builder> getChooseList1() {
        return RewardHelper.toRewardItemImmutableList(chooseItem1);
    }

    public List<Reward.RewardItem.Builder> getChooseList2() {
        return RewardHelper.toRewardItemImmutableList(chooseItem2);
    }
    
    public int getDayLevel() {
		return dayLevel;
	}
}