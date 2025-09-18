package com.hawk.activity.type.impl.newFirstRecharge.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 新首充奖励配置
 */
@HawkConfigManager.XmlResource(file = "activity/new_first_recharge/%s/new_first_recharge_reward.xml", autoLoad=false, loadParams="312")
public class NewFirstRechargeRewardCfg extends HawkConfigBase {
    @Id
    private final int id;

    /**
     * 等待时间
     */
    private final int waitTime;

    /**
     * 充值次数
     */
    private final int payCount;

    /**
     * 奖励
     */
    private final String commonAward;

    /**
     * 解析完的奖励
     */
    private List<Reward.RewardItem.Builder> commonAwardList;

    public NewFirstRechargeRewardCfg(){
        this.id = 0;
        this.waitTime = 0;
        this.payCount = 0;
        this.commonAward = "";
    }

    public int getId() {
        return id;
    }

    public long getWaitTime() {
        return TimeUnit.HOURS.toMillis(waitTime);
    }

    public int getPayCount() {
        return payCount;
    }

    public String getCommonAward() {
        return commonAward;
    }

    @Override
    protected boolean assemble() {
        try {
            commonAwardList = RewardHelper.toRewardItemImmutableList(commonAward);
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

    @Override
    protected boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(commonAward);
        if (!valid) {
            throw new InvalidParameterException(String.format("FirstRechargeRewardCfg reward error, id: %s , commonAward: %s", id, commonAward));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getCommonAwardList() {
        return commonAwardList;
    }
}
