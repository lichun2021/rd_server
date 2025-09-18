package com.hawk.activity.type.impl.recoveryExchange.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @author richard
 * 道具回收活动精炼配置
 */
@HawkConfigManager.XmlResource(file = "activity/recovery_exchange/recovery_exchange_redundantItem.xml")
public class RecoveryExchangeRedundantItemCfg extends HawkConfigBase {
    /**
     * id
     */
    @Id
    private final int id;

    /**
     * 目标物品
     */
    private final String obtainedItem;

    /**
     * 需要的物品，这里是所有需要的物品列表
     * 实际使用时任选其一
     */
    private final String requiredItem;
    /**
     * 一般兑换的消耗物品，这里是requiredItem带具体数量
     */
    private final String ordinaryExchange;

    /**
     * 一般兑换的次数限制
     */
    private final int ordinaryTimes;

    /**
     * 高级兑换的消耗物品，这里是requiredItem带具体数量
     * 还包括其它货币，requiredItem任选其一，并且包含对、
     * 应的货币
     */
    private final String highExchange;

    /**
     * 高级兑换的次数限制
     */
    private final int highTimes;

    private List<RewardItem.Builder> requiredItemList;
    private List<RewardItem.Builder> ordinaryExchangeList;
    private Map<Integer, List<RewardItem.Builder>> highExchangeMap;

    public RecoveryExchangeRedundantItemCfg() {
        id = 0;
        ordinaryTimes = 0;
        highTimes = 0;
        obtainedItem = "";
        requiredItem = "";
        ordinaryExchange = "";
        highExchange = "";
    }

    @Override
    protected boolean assemble() {
        //需要的物品列表
        requiredItemList = RewardHelper.toRewardItemImmutableList(requiredItem);
        //普通兑换需要的物品
        ordinaryExchangeList = RewardHelper.toRewardItemImmutableList(ordinaryExchange);

        highExchangeMap = new HashMap<>();
        String[] str = SerializeHelper.split(highExchange, SerializeHelper.SEMICOLON_ITEMS);
        for (int i = 0; i < str.length; ++i) {
            //这里是带着分隔符的物品列表
            String[] strTmp = SerializeHelper.split(str[i], SerializeHelper.BETWEEN_ITEMS);
            RewardItem.Builder builder = RewardHelper.toRewardItem(strTmp[0]);

            highExchangeMap.put(builder.getItemId(), RewardHelper.toRewardItemImmutableList(str[i]));
        }
        return super.assemble();
    }

    public List<RewardItem.Builder> getOrdinaryItem(int itemId) {
        for (RewardItem.Builder builder : ordinaryExchangeList) {
            if (builder.getItemId() == itemId) {
                String itemStr = RewardHelper.toItemString(builder.build());
                return RewardHelper.toRewardItemImmutableList(itemStr);
            }
        }
        return null;
    }

    public List<RewardItem.Builder> getHighExchangeItems(int itemId) {
        return highExchangeMap.get(itemId);
    }

    public List<RewardItem.Builder> getObtainedItem() {
        return RewardHelper.toRewardItemImmutableList(obtainedItem);
    }

    public String getRequiredItem() {
        return requiredItem;
    }

    public int getOrdinaryTimes() {
        return ordinaryTimes;
    }

    public int getHighTimes() {
        return highTimes;
    }

    public int getId() {
        return this.id;
    }
}
