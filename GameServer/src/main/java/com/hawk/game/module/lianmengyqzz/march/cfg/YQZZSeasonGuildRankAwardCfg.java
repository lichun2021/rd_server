package com.hawk.game.module.lianmengyqzz.march.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/moon_war_guild_rank_award.xml")
public class YQZZSeasonGuildRankAwardCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final String range;

    private final String award;

    private int min;

    private int max;

    private List<Reward.RewardItem.Builder> rewardList;

    public YQZZSeasonGuildRankAwardCfg(){
        id = 0;
        range = "";
        award = "";
    }

    @Override
    protected boolean assemble() {
        try {
            if(!HawkOSOperator.isEmptyString(this.range)){
                String arr[] = this.range.split("_");
                if(arr.length < 2){
                    min = Integer.parseInt(arr[0]);
                    max = Integer.parseInt(arr[0]);
                }else {
                    min = Integer.parseInt(arr[0]);
                    max = Integer.parseInt(arr[1]);
                }
            }
            this.rewardList = RewardHelper.toRewardItemImmutableList(this.award);
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1);
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getAward() {
        return award;
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }
}
