package com.hawk.activity.type.impl.shareGlory.cfg;

import com.hawk.log.Action;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 双十一联盟欢庆 配置
 *
 * @author richard
 */
@HawkConfigManager.KVResource(file = "activity/alliance_share_glory/alliance_share_glory_cfg.xml")
public class ShareGloryKVCfg extends HawkConfigBase {
    /**
     * 活动ID
     */
    private final int activityId;
    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;
    /**
     * 捐献道具A
     */
    private final int itemA;
    /**
     * A道具的捐献奖励
     */
    private final int itemAReward;
    /**
     * A能量等级-返送活动ID列表(需要监控的活动ID)
     */
    private final String rewardActivity;
    /**
     * 捐献道具B
     */
    private final int itemB;
    /**
     * B道具的捐献奖励
     */
    private final int itemBReward;
    /**
     * B能量等级-返送礼包类型列表(需要监控的活动ID)
     */
    private final String rewardPack;
    /**
     * 不参与礼包返送的道具ID列表(从rewardPack监控的活动中排除的礼包)
     */
    private final String noRewardPackItem;
    /**
     * 监控的所有道具的统一类型
     */
    private final int itemType;

    private final int donateAMax;
    private final int donateBMax;
    private final String getId242;

    private Set<Integer> actionsA = new HashSet<>();
    private Set<Integer> actionsB = new HashSet<>();
    private Set<Integer> id242s = new HashSet<>();
    public ShareGloryKVCfg() {
        serverDelay = 0;
        itemA = 0;
        itemAReward = 0;
        itemB = 0;
        itemBReward = 0;
        rewardActivity = "";
        rewardPack = "";
        noRewardPackItem = "";
        activityId = -1;
        itemType = 30000;
        donateAMax = 0;
        donateBMax = 0;
        getId242 = "";
    }

    public int getActivityId() {
        return activityId;
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    public int getItemA() {
        return itemA;
    }

    public int getItemAReward() {
        return itemAReward;
    }

    public String getRewardActivity() {
        return rewardActivity;
    }

    public int getItemB() {
        return itemB;
    }

    public int getItemBReward() {
        return itemBReward;
    }

    public String getRewardPack() {
        return rewardPack;
    }

    public String getNoRewardPackItem() {
        return noRewardPackItem;
    }

    public int getItemType() {
        return this.itemType;
    }

    @Override
    public boolean assemble() {
        actionsA.add(Action.RECHARGE_WELFARE_LOTTERY_REWARD.intItemVal());
        actionsA.add(Action.RED_BLUE_OPEN_TICKET_REWARD.intItemVal());
        actionsA.add(Action.HIDDEN_TREASURE_LOTTERY.intItemVal());
        actionsA.add(Action.MEDAL_ACTION_LOTTERY.intItemVal());
        actionsA.add(Action.CELEBRATION_SHOP_EXCHANGE_REWARD.intItemVal());
        actionsA.add(Action.DAILY_RECHARGE_BUY_REWARD.intItemVal());
        actionsA.add(Action.LUCKY_DISCOUNT_BUY_GAIN.intItemVal());
        actionsA.add(Action.LUCK_GET_GOLD_DRAW_REWARD.intItemVal());
        //
        actionsA.add(Action.GROUP_BUG_REWARD.intItemVal());
        actionsA.add(Action.PDD_GAIN.intItemVal());
        actionsA.add(Action.QUEST_TREASURE_RANDOM_WALK_REWARD.intItemVal());
        actionsA.add(Action.PLANET_EXPLORE.intItemVal());
        actionsA.add(Action.PLANT_SOLDIER_FACTORY_DRAW.intItemVal());
        actionsA.add(Action.PLANT_SOLDIER_FACTORY_AWARD.intItemVal());
        actionsA.add(Action.CORE_EXPLORE_SHOP_EXCHANGE.intItemVal());
        actionsA.add(Action.CORE_EXPLORE_TECH_AWARD.intItemVal());
        actionsA.add(Action.ROSE_GIFT_DRAW_REWARD.intItemVal());
        actionsA.add(Action.SUPPLY_CRATE_OPEN_GET.intItemVal());


        actionsB.add(Action.BUY_GIFT.intItemVal());
        actionsB.add(Action.TRAVEL_GIFT_BUY.intItemVal());
        actionsB.add(Action.PUSH_GIFT_BUY.intItemVal());
        String[] tmp = this.getId242.split("_");
        for(int i=0; i<tmp.length; ++i){
            this.id242s.add(Integer.valueOf(tmp[i]));
        }
        return true;
    }

    public DonateItemType getEnergyType(int activityId) {
        if (actionsA.contains(activityId)) {
            return DonateItemType.typeA;
        }
        if (actionsB.contains(activityId)) {
            return DonateItemType.typeB;
        }
        return DonateItemType.typeErr;
    }

    public int getDonateAMax() {
        return donateAMax;
    }

    public int getDonateBMax() {
        return donateBMax;
    }

    public Boolean isId242Contain(int id){
        return this.id242s.contains(id);
    }

    public enum DonateItemType {
        typeErr(0),
        typeA(1),
        typeB(2);
        public final int VAL;

        DonateItemType(int type) {
            this.VAL = type;
        }
    }
}
