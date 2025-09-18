package com.hawk.activity.type.impl.guildBack.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.security.InvalidParameterException;
import java.util.List;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/guild_back/guild_back_shop.xml")
public class GuildBackShopCfg extends AExchangeTipConfig {
    @Id
    private final int id;
    private final String gainItem;
    private final String needItem;
    private final int times;

    /** 每份获得物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    /** 每份消耗物品 */
    private List<Reward.RewardItem.Builder> needItemList;


    public GuildBackShopCfg(){
        this.id = 0;
        this.gainItem = "";
        this.needItem = "";
        this.times = 0;
    }

    public int getId(){
        return this.id;
    }

    public String getGainItem(){
        return this.gainItem;
    }

    public String getNeedItem(){
        return this.needItem;
    }

    public int getTimes(){
        return this.times;
    }

    /**
     * 解析配置
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
            this.needItemList = RewardHelper.toRewardItemImmutableList(this.needItem);
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1);
            return false;
        }
    }

    /**
     * 检查道具合法性
     * @return 检查结果
     */
    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("GuildBackShopCfg reward error, id: %s , needItem: %s", id, needItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("GuildBackShopCfg reward error, id: %s , gainItem: %s", id, gainItem));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getNeedItemList() {
        return needItemList;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }
}