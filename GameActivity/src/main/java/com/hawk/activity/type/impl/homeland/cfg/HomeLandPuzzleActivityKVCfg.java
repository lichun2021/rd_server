package com.hawk.activity.type.impl.homeland.cfg;

import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzlePoolItem;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzlePoolType;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.KVResource(file = "activity/act_home382/act_home382_const.xml")
public class HomeLandPuzzleActivityKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;
    //刮卡道具（不删除）
    private final String scratchCardItemSave;
    //兑换道具
    private final String exchangeItem;
    //活动期间刮卡数量
    private final int scratchCardTimesLimit;
    //每日免费刮卡次数
    private final int dailyFreeDrawLimit;
    //每次抽奖可获得道具,awardId
    private final int drawGetAward;
    //组合图案数组
    private final String groupID;
    //大奖图案数组
    private final int bigPrizeID;
    //组合图案奖励
    private final String groupItems;
    //抽奖上限展示数量
    private final int drawLogShowLimit;
    // 大奖在活动期间总共只能抽出5次
    private final int bigPrizeGetLimit;
    // 每10次必出组合奖
    private final int groupAwardMustGetValue;
    // 每100次必出大奖
    private final int bigPrizeMustGetValue;
    // 基地小于x等级不会开启
    private final int buildLevelLimit;

    public HomeLandPuzzleActivityKVCfg() {
        serverDelay = 0;
        scratchCardItemSave = "";
        exchangeItem = "";
        scratchCardTimesLimit = 0;
        dailyFreeDrawLimit = 0;
        drawGetAward = 0;
        groupID = "";
        bigPrizeID = 0;
        groupItems = "0";
        bigPrizeGetLimit = 0;
        groupAwardMustGetValue = 0;
        bigPrizeMustGetValue = 0;
        drawLogShowLimit = 0;
        buildLevelLimit = 0;
    }

    @Override
    protected boolean assemble() {
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getGrandPrizePityThreshold() {
        return bigPrizeMustGetValue;
    }

    public int getCombinationPityThreshold() {
        return groupAwardMustGetValue;
    }

    public int getGrandPrizeGlobalLimit() {
        return bigPrizeGetLimit;
    }

    public String getScratchCardItemSave() {
        return scratchCardItemSave;
    }

    public String getExchangeItem() {
        return exchangeItem;
    }

    public int getScratchCardTimesLimit() {
        return scratchCardTimesLimit;
    }

    public int getDailyFreeDrawLimit() {
        return dailyFreeDrawLimit;
    }

    public int getDrawGetAward() {
        return drawGetAward;
    }

    public String getGroupID() {
        return groupID;
    }

    public int getBigPrizeID() {
        return bigPrizeID;
    }

    public String getGroupItems() {
        return groupItems;
    }

    public int getDrawLogShowLimit() {
        return drawLogShowLimit;
    }

    public int getBuildLevelLimit() {
        return buildLevelLimit;
    }

    public List<HomeLandPuzzlePoolItem> getPuzzlePool(HomeLandPuzzlePoolType type) {
        List<HomeLandPuzzlePoolItem> homeLandPuzzlePoolItems = new ArrayList<>();
        List<HomeLandPuzzleActivityPoolCfg> poolCfgs = HomeLandPuzzleActivityPoolCfg.getConfigList(type.getPool());
        for (HomeLandPuzzleActivityPoolCfg cfg : poolCfgs) {
            HomeLandPuzzlePoolItem poolItem = new HomeLandPuzzlePoolItem(cfg.getId(), type, cfg.getWeight());
            poolItem.setPatternItem(cfg.getPattern());
            poolItem.setItemInfo(cfg.getRewards());
            homeLandPuzzlePoolItems.add(poolItem);
        }
        return homeLandPuzzlePoolItems;
    }
}
