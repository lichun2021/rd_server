package com.hawk.game.module.lianmengtaiboliya.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 泰伯利亚名人堂分享配置
 */
@HawkConfigManager.XmlResource(file = "xml/tbly_hall_of_fame.xml")
public class TBLYFameHallCfg extends HawkConfigBase {
    @Id
    protected final int id;

    /** 赛季*/
    protected final int season;

    /** 分享奖励*/
    protected final String shareReward;

    /** 解析后的奖励*/
    private List<Reward.RewardItem.Builder> rewardList;

    /**
     * 构造函数
     */
    public TBLYFameHallCfg(){
        id = 0;
        season = 0;
        shareReward = "";
    }

    /**
     * 解析
     * @return
     */
    @Override
    protected boolean assemble() {
        try {
            rewardList = RewardHelper.toRewardItemImmutableList(shareReward);
        }catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    /**
     * 奖励
     * @return
     */
    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    /**
     * 主键
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * 赛季
     * @return
     */
    public int getSeason() {
        return season;
    }
}
