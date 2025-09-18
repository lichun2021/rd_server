package com.hawk.activity.type.impl.roseGift.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/rose_gift/rose_gift_draw.xml")
public class RoseGiftDrawCfg extends HawkConfigBase {
    @Id
    private final int id;

    /** 权重 */
    private final int weight;

    /** 奖励 */
    private final String gainItem;

    /** 可被抽出次数 */
    private final int times;

    /** 是否广播通知 */
    private final int notice;

    /** 奖励物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    public RoseGiftDrawCfg(){
        id = 0;
        weight = 0;
        gainItem = "";
        times = 0;
        notice = 0;
    }

    public int getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public String getGainItem() {
        return gainItem;
    }

    public int getTimes() {
        return times;
    }

    public int getNotice() {
        return notice;
    }

    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1, new Object[0]);
            return false;
        }
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("RoseGiftDrawCfg reward error, id: %s , gainItem: %s", id, gainItem));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

    public boolean isNotice(){
        return notice == 1;
    }
}
