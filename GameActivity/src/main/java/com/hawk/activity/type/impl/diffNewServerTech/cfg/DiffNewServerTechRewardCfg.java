package com.hawk.activity.type.impl.diffNewServerTech.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/service_bonus/%s/bonusr_reward.xml", autoLoad=false, loadParams="323")
public class DiffNewServerTechRewardCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final int service;

    private final String day;

    private final String buffId;

    private final String reward;

    private int min;

    private int max;

    /** 奖励物品 */
    private List<Reward.RewardItem.Builder> rewardList;
    
    private List<Integer> buffList;

    public DiffNewServerTechRewardCfg(){
        id = 0;
        service = 0;
        day = "";
        buffId = "";
        reward = "";
    }

    @Override
    protected boolean assemble() {
        try {
            if(!HawkOSOperator.isEmptyString(this.day)){
                String arr[] = this.day.split("-");
                if(arr.length < 2){
                    min = Integer.parseInt(arr[0]);
                    max = Integer.parseInt(arr[0]);
                }else {
                    min = Integer.parseInt(arr[0]);
                    max = Integer.parseInt(arr[1]);
                }
            }
            this.rewardList = RewardHelper.toRewardItemImmutableList(this.reward);
            this.buffList = SerializeHelper.stringToList(Integer.class, buffId, ",");
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1);
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public boolean isService(){
        return service == 1;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getBuffId() {
        return buffId;
    }

    public String getReward() {
        return reward;
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }
    
    public List<Integer> getBuffList() {
    	return buffList;
    }
}
